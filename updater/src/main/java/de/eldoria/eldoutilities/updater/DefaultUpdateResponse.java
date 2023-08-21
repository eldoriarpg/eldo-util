/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import org.bukkit.plugin.Plugin;

public class DefaultUpdateResponse implements UpdateResponse {
    private final boolean update;
    private final String latest;

    /**
     * Create a new Update check response.
     *
     * @param update whether a new version is available or not
     * @param latest latest available version
     */
    public DefaultUpdateResponse(boolean update, String latest) {
        this.update = update;
        this.latest = latest;
    }

    public static DefaultUpdateResponse create(String latest, Plugin plugin) {
        return new DefaultUpdateResponse(!plugin.getDescription().getVersion().equalsIgnoreCase(latest), latest);
    }

    @Override
    public boolean isOutdated() {
        return update;
    }

    @Override
    public String latestVersion() {
        return latest;
    }
}
