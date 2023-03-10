/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import com.google.gson.Gson;
import de.eldoria.eldoutilities.debug.DefaultProperties;
import de.eldoria.eldoutilities.updater.UpdateResponse;
import de.eldoria.eldoutilities.updater.Updater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Updater implementation for butler application.
 *
 * @since 1.1.0
 */
public class LynaUpdateChecker extends Updater<LynaUpdateData> {

    public LynaUpdateChecker(LynaUpdateData data) {
        super(data);
    }

    @Override
    protected Optional<UpdateResponse> checkUpdate(LynaUpdateData data) {
        var plugin = data.plugin();
        var request = HttpRequest.newBuilder()
                .uri(URI.create("%s/api/v1/update/check?version%s&id=%s"
                        .formatted(data.host(), data.userData().property(DefaultProperties.ARTIFACT_VERSION), data.productId())))
                .GET()
                .header("Content-Type", "application/json; utf-8")
                .header("Accept", "application/json")
                .header("User-Agent", "EldoUtilities/LynaUpdater")
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
        var response = new Gson().fromJson(body, LynaUpdateCheckResponse.class);

        return Optional.of(new UpdateResponse(response.isUpdate(), response.latestVersion()));
    }
}
