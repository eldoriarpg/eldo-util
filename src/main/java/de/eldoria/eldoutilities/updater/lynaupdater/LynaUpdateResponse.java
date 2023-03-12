/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import de.eldoria.eldoutilities.updater.UpdateResponse;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Web Response for butler application.
 *
 * @since 1.1.0
 */
public class LynaUpdateResponse implements UpdateResponse {
    private final boolean update;
    private final String latest;
    /**
     * The publishing date as unix timestamp
     */
    private final long published;

    /**
     * Create a new Update check response.
     *
     * @param update    whether a new version is available or not
     * @param latest    latest available version
     * @param published
     */
    public LynaUpdateResponse(boolean update, String latest, long published) {
        this.update = update;
        this.latest = latest;
        this.published = published;
    }

    public boolean isUpdate() {
        return update;
    }

    @Override
    public boolean isOutdated() {
        return update;
    }

    @Override
    public String latestVersion() {
        return latest;
    }

    public String published() {
        var duration = Duration.ofSeconds(Instant.ofEpochSecond(published).until(Instant.now(), ChronoUnit.SECONDS));

        if (duration.toDays() > 0) {
            if (duration.toDays() == 1) {
                return "1 day";
            }
            return "%d days".formatted(duration.toDays());
        }

        if (duration.toHours() > 0) {
            if (duration.toHours() == 1) {
                return "1 hour";
            }
            return "%d hours".formatted(duration.toHours());
        }

        return "%d minutes".formatted(duration.toMinutes());
    }
}
