/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

/**
 * Web Response for butler application.
 *
 * @since 1.1.0
 */
public class LynaUpdateCheckResponse {
    private final boolean update;
    private final String latest;

    /**
     * Create a new Update check response.
     *
     * @param update whether a new version is available or not
     * @param latest latest available version
     */
    public LynaUpdateCheckResponse(boolean update, String latest) {
        this.update = update;
        this.latest = latest;
    }

    public boolean isUpdate() {
        return update;
    }

    public String latestVersion() {
        return latest;
    }
}
