/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.butlerupdater;

/**
 * Web Response for butler application.
 *
 * @since 1.1.0
 */
public class ButlerUpdateCheckResponse {
    private final boolean newVersionAvailable;
    private final String latestVersion;
    private final String hash;

    /**
     * Create an ew Update check response.
     *
     * @param newVersionAvailable whether a new version is available or not
     * @param latestVersion       latest available version
     * @param hash                hash of the latest version
     */
    public ButlerUpdateCheckResponse(boolean newVersionAvailable, String latestVersion, String hash) {
        this.newVersionAvailable = newVersionAvailable;
        this.latestVersion = latestVersion;
        this.hash = hash;
    }

    public boolean isNewVersionAvailable() {
        return newVersionAvailable;
    }

    public String latestVersion() {
        return latestVersion;
    }

    public String hash() {
        return hash;
    }
}
