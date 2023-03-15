/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import com.google.gson.Gson;
import de.eldoria.eldoutilities.updater.UpdateResponse;
import de.eldoria.eldoutilities.debug.DefaultProperties;
import de.eldoria.eldoutilities.updater.Updater;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Updater implementation for butler application.
 *
 * @since 1.1.0
 */
public class LynaUpdateChecker extends Updater<LynaUpdateResponse, LynaUpdateData> {

    public LynaUpdateChecker(LynaUpdateData data) {
        super(data);
    }

    @Override
    protected Optional<LynaUpdateResponse> checkUpdate(LynaUpdateData data) {
        var plugin = data.plugin();
        Map<String, Object> queryParams = new LinkedHashMap<>();
        var userData = data.userData();
        queryParams.put("id", data.productId());

        userData.property(DefaultProperties.ARTIFACT_VERSION)
                .or(() -> Optional.of(plugin.getDescription().getVersion()))
                .ifPresent(version -> queryParams.put("version", version));

        userData.property(DefaultProperties.ARTIFACT)
                .ifPresent(artifact -> queryParams.put("artifact", artifact));

        userData.property(DefaultProperties.UNIX)
                .ifPresent(unix -> queryParams.put("unix", unix));

        var request = HttpRequest.newBuilder()
                .uri(URI.create("%s/api/v1/update/check?%s"
                        .formatted(data.host(),
                                queryParams.entrySet().stream().map(e -> "%s=%s".formatted(e.getKey(), e.getValue())).collect(Collectors.joining("&")))))
                .GET()
                .header("Content-Type", "application/json; utf-8")
                .header("Accept", "application/json")
                .header("User-Agent", "EldoUtilities/LynaUpdater")
                .build();

        try {
            var response = client().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                plugin.getLogger().log(Level.FINEST, "Received non 200 request.");
                return Optional.empty();
            }
            return Optional.of(new Gson().fromJson(response.body(), LynaUpdateResponse.class));
        } catch (IOException | InterruptedException e) {
            return Optional.empty();
        }

    }
}
