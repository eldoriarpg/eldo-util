/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.butlerupdater;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import de.eldoria.eldoutilities.updater.DefaultUpdateResponse;
import de.eldoria.eldoutilities.updater.Updater;
import de.eldoria.eldoutilities.utils.Plugins;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Updater implementation for butler application.
 *
 * @since 1.1.0
 */
public class ButlerUpdateChecker extends Updater<DefaultUpdateResponse, ButlerUpdateData> {
    private ButlerUpdateCheckResponse response;

    public ButlerUpdateChecker(ButlerUpdateData data) {
        super(data);
    }

    @Override
    protected Optional<DefaultUpdateResponse> checkUpdate(ButlerUpdateData data) {
        var plugin = data.plugin();
        var request = HttpRequest.newBuilder()
                .uri(URI.create("%s/api/v1/update/check?version%s&id=%s"
                        .formatted(data.host(), plugin.getDescription().getVersion(), data.butlerId())))
                .GET()
                .header("Content-Type", "application/json; utf-8")
                .header("Accept", "application/json")
                .header("User-Agent", "EldoUtilities/ButlerUpdater")
                .build();

        String body;
        try {
            var response = client().send(request, HttpResponse.BodyHandlers.ofString());
            body = response.body();
            if (response.statusCode() != 200) {
                plugin.getLogger().log(Level.FINEST, "Received non 200 request.");
                return Optional.empty();
            }
        } catch (IOException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            return Optional.empty();
        }
        response = new Gson().fromJson(body, ButlerUpdateCheckResponse.class);

        return Optional.of(new DefaultUpdateResponse(response.isNewVersionAvailable(), response.latestVersion()));
    }

    @Override
    protected boolean update() {
        if (response == null) return false;
        var plugin = getData().plugin();
        plugin.getLogger().info("§2>------------------------<");
        plugin.getLogger().info("§2> Performing auto update <");
        plugin.getLogger().info("§2>------------------------<");

        plugin.getLogger().info("§2Performing auto update.");
        URL url;
        try {
            url = new URL(getData().host() + "/download?id=" + getData().butlerId() + "&version=" + response.latestVersion());
        } catch (MalformedURLException e) {
            plugin.getLogger().log(Level.CONFIG, "Could not create download url.", e);
            plugin.getLogger().warning("§cAborting Update.");
            return false;
        }

        plugin.getLogger().info("§2Downloaded new file.");

        var plugins = plugin.getDataFolder().getParent();
        var updateDirectory = new File(Paths.get(plugins, "update").toString());
        if (!updateDirectory.exists()) {
            if (!updateDirectory.mkdirs()) {
                plugin.getLogger().warning("§cCould not create update directory.");
                plugin.getLogger().warning("§cAborting Update.");
                return false;
            }
        }

        var pluginFile = Plugins.getPluginFile(plugin);
        if (pluginFile.isEmpty()) {
            plugin.getLogger().log(Level.WARNING, "§cCould not find plugin file");
            return false;
        }

        var updateFile = Paths.get(updateDirectory.getAbsolutePath(), pluginFile.get().getName()).toFile();
        try (var input = url.openStream()) {
            Files.copy(input, updateFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not create update file.", e);
            plugin.getLogger().warning("§cAborting Update.");
            return false;
        }

        String hash;
        plugin.getLogger().info("§2Calculate checksum.");
        try {
            hash = Hashing.sha256().hashBytes(Files.readAllBytes(updateFile.toPath())).toString();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to create hash from update file.", e);
            plugin.getLogger().warning("§cAborting Update.");
            updateFile.delete();
            return false;
        }

        if (!hash.equals(response.hash())) {
            plugin.getLogger().warning("§cChecksums of update file is not as expected.");
            plugin.getLogger().warning("§cAborting Update.");
            updateFile.delete();
            return false;
        }
        plugin.getLogger().info("§2Checksums of update file is correct.");

        plugin.getLogger().info("§2File " + pluginFile.get().getName() + " will be replaced with the new version on next startup.");

        plugin.getLogger().info("§2>----------------------------------------------------<");
        plugin.getLogger().info("§2> Update downloaded. Please restart to apply update. <");
        plugin.getLogger().info("§2>----------------------------------------------------<");
        return true;
    }
}
