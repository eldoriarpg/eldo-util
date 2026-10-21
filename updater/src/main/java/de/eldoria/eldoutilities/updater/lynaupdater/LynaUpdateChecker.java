/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import com.google.gson.Gson;
import de.eldoria.eldoutilities.debug.DefaultProperties;
import de.eldoria.eldoutilities.updater.Updater;
import de.eldoria.eldoutilities.utils.Plugins;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Update checks against a <a href="https://github.com/rainbowdashlabs/lyna">Lyna</a> instance. Query values are URL
 * encoded and a check gives up after {@value #TIMEOUT_SECONDS} seconds.
 * <p>
 * Only stable releases within the installed major version are installed automatically. The download button offers stable releases, and dev or snapshot
 * builds only to a server already running a dev or snapshot build.
 * <p>
 * A free product is downloaded from the public address the update check answers with. A premium product trades the
 * upgrade token of its {@link UpgradeTokenStore} at {@code /api/v1/update/token} for a short-lived download address
 * and the next token, which is stored before the download starts. Downloads go into the server's update folder under
 * the name of the running plugin file, where the server picks them up on the next start.
 *
 * @since 1.1.0
 */
public class LynaUpdateChecker extends Updater<LynaUpdateResponse, LynaUpdateData> {
    private static final int TIMEOUT_SECONDS = 10;
    private static final String USER_AGENT = "EldoUtilities/LynaUpdater";
    private final Gson gson = new Gson();
    private LynaUpdateResponse latest;

    public LynaUpdateChecker(LynaUpdateData data) {
        super(data);
    }

    @Override
    protected Optional<LynaUpdateResponse> checkUpdate(LynaUpdateData data) {
        var plugin = data.plugin();
        Map<String, Object> queryParams = new LinkedHashMap<>();
        var userData = data.userData();
        queryParams.put("id", data.productId());

        queryParams.put("version", currentVersion());

        userData.property(DefaultProperties.ARTIFACT)
                .ifPresent(artifact -> queryParams.put("artifact", artifact));

        userData.property(DefaultProperties.UNIX)
                .ifPresent(unix -> queryParams.put("unix", unix));

        var request = HttpRequest.newBuilder()
                .uri(URI.create("%s/api/v1/update/check?%s"
                        .formatted(data.host(),
                                queryParams.entrySet().stream()
                                        .map(e -> "%s=%s".formatted(e.getKey(), URLEncoder.encode(String.valueOf(e.getValue()), StandardCharsets.UTF_8)))
                                        .collect(Collectors.joining("&")))))
                .GET()
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .build();

        try {
            var response = client().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                plugin.getLogger().log(Level.FINEST, "Received non 200 request.");
                return Optional.empty();
            }
            latest = gson.fromJson(response.body(), LynaUpdateResponse.class);
            return Optional.of(latest);
        } catch (IOException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    @Override
    protected boolean update() {
        if (latest == null) return false;
        var log = getData().plugin().getLogger();
        log.info("§2Performing auto update to " + latest.latestVersion() + ".");
        Optional<Build> build;
        try {
            build = latest.download().map(Build::unsized).or(this::exchangeToken);
        } catch (UncheckedIOException e) {
            log.log(Level.SEVERE, "Could not store the new upgrade token. Reissue the token at %s/account/updaters."
                    .formatted(getData().host()), e);
            return false;
        }
        if (build.isEmpty()) {
            log.warning("§cAborting update.");
            return false;
        }
        return install(build.get());
    }

    /**
     * Stable releases within the installed major version are installed automatically. A new major version may break
     * compatibility, so it is only announced and offered for download.
     */
    @Override
    protected boolean updatesAutomatically() {
        return latest != null && latest.isStable() && LynaUpdateResponse.sameMajor(currentVersion(), latest.latestVersion());
    }

    @Override
    protected boolean offersDownload() {
        return latest != null && (latest.isStable() || !LynaUpdateResponse.isStable(currentVersion()));
    }

    private String currentVersion() {
        return getData().userData().property(DefaultProperties.ARTIFACT_VERSION)
                .orElseGet(() -> getData().plugin().getDescription().getVersion());
    }

    private Optional<Build> exchangeToken() {
        var data = getData();
        var log = data.plugin().getLogger();
        var token = data.tokenStore().token();
        if (token.isEmpty()) {
            log.warning("No upgrade token configured for automatic updates. Create one at %s/account/updaters and put it into %s."
                    .formatted(data.host(), data.tokenStore().location()));
            return Optional.empty();
        }

        var request = HttpRequest.newBuilder()
                .uri(URI.create(data.host() + "/api/v1/update/token"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(new TokenRequest(latest.latestVersion(), data.downloadType()))))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("Authorization", "Bearer " + token.get())
                .header("Content-Type", "application/json; utf-8")
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .build();

        HttpResponse<String> response;
        try {
            response = client().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            log.log(Level.WARNING, "Could not reach lyna to exchange the upgrade token.", e);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }

        if (response.statusCode() != 201) {
            log.warning(refusal(response));
            return Optional.empty();
        }

        var exchange = gson.fromJson(response.body(), TokenExchange.class);
        data.tokenStore().token(exchange.upgradeToken());
        return Optional.of(new Build(exchange.downloadUrl(), exchange.sizeBytes()));
    }

    private String refusal(HttpResponse<String> response) {
        var data = getData();
        return switch (response.statusCode()) {
            case 401 -> "The upgrade token in %s is unknown or was already used. Reissue it at %s/account/updaters."
                    .formatted(data.tokenStore().location(), data.host());
            case 403 -> "The account of the upgrade token has no access to this product anymore.";
            case 404 -> "Lyna has no build of version %s for this upgrade token: %s"
                    .formatted(latest.latestVersion(), response.body());
            case 429 -> "The upgrade token was exchanged too recently. Retrying on the next check.";
            default -> "Lyna refused the upgrade token exchange with status %d: %s"
                    .formatted(response.statusCode(), response.body());
        };
    }

    private boolean install(Build build) {
        var plugin = getData().plugin();
        var log = plugin.getLogger();
        var pluginFile = Plugins.getPluginFile(plugin);
        if (pluginFile.isEmpty()) {
            log.warning("§cCould not find plugin file. Aborting update.");
            return false;
        }

        Path updateFolder = plugin.getServer().getUpdateFolderFile().toPath();
        Path target = updateFolder.resolve(pluginFile.get().getName());
        Path partial = updateFolder.resolve(pluginFile.get().getName() + ".part");
        var request = HttpRequest.newBuilder()
                .uri(URI.create(build.url()))
                .GET()
                .header("User-Agent", USER_AGENT)
                .build();

        try {
            Files.createDirectories(updateFolder);
            var response = client().send(request, HttpResponse.BodyHandlers.ofFile(partial));
            if (response.statusCode() != 200) {
                log.warning("§cDownload of the update failed with status %d. Aborting update.".formatted(response.statusCode()));
                Files.deleteIfExists(partial);
                return false;
            }
            if (build.size() >= 0 && Files.size(partial) != build.size()) {
                log.warning("§cDownloaded update has %d bytes instead of %d. Aborting update.".formatted(Files.size(partial), build.size()));
                Files.deleteIfExists(partial);
                return false;
            }
            Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.log(Level.WARNING, "Could not download update. Aborting update.", e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        log.info("§2File " + pluginFile.get().getName() + " will be replaced with the new version on next startup.");
        log.info("§2>----------------------------------------------------<");
        log.info("§2> Update downloaded. Please restart to apply update. <");
        log.info("§2>----------------------------------------------------<");
        return true;
    }

    /**
     * A build to download.
     *
     * @param url  address serving the build
     * @param size expected size in bytes, or a negative value when unknown
     */
    private record Build(String url, long size) {
        static Build unsized(String url) {
            return new Build(url, -1);
        }
    }
}
