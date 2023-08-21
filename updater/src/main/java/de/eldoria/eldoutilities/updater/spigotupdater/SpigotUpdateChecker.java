/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.spigotupdater;

import de.eldoria.eldoutilities.updater.DefaultUpdateResponse;
import de.eldoria.eldoutilities.updater.Updater;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * Updater implementation for spigot update check.
 *
 * @since 1.0.0
 */
public final class SpigotUpdateChecker extends Updater<DefaultUpdateResponse, SpigotUpdateData> {
    public SpigotUpdateChecker(SpigotUpdateData data) {
        super(data);
    }

    @Override
    protected Optional<DefaultUpdateResponse> checkUpdate(SpigotUpdateData data) {
        var get = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spigotmc.org/legacy/update.php?resource=" + data.getSpigotId()))
                .GET()
                .build();

        try {
            var response = client().send(get, HttpResponse.BodyHandlers.ofString());
            return Optional.of(DefaultUpdateResponse.create(response.body(), data.plugin()));
        } catch (IOException | InterruptedException e) {
            return Optional.empty();
        }
    }
}
