/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.spigotupdater;

import de.eldoria.eldoutilities.updater.UpdateResponse;
import de.eldoria.eldoutilities.updater.Updater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Updater implementation for spigot update check.
 *
 * @since 1.0.0
 */
public final class SpigotUpdateChecker extends Updater<SpigotUpdateData> {
    public SpigotUpdateChecker(SpigotUpdateData data) {
        super(data);
    }

    @Override
    protected Optional<UpdateResponse> checkUpdate(SpigotUpdateData data) {
        var get = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spigotmc.org/legacy/update.php?resource=" + data.getSpigotId()))
                .GET()
                .build();

        try {
            var response = client().send(get, HttpResponse.BodyHandlers.ofString());
        return Optional.of(UpdateResponse.create(response.body(), data.plugin()));
        } catch (IOException | InterruptedException e) {
            return Optional.empty();
        }
    }
}
