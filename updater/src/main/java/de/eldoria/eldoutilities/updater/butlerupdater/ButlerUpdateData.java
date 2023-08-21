/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.butlerupdater;

import de.eldoria.eldoutilities.updater.DefaultUpdateResponse;
import de.eldoria.eldoutilities.updater.UpdateData;
import org.bukkit.plugin.Plugin;

/**
 * Update Data implementation for butler application.
 *
 * @since 1.1.0
 */
public class ButlerUpdateData extends UpdateData<DefaultUpdateResponse> {
    /**
     * Default adress to submit debug data and update checks
     */
    public static final String HOST = "https://plugins.eldoria.de";
    private final int butlerId;
    private final String host;

    /**
     * Create a new update data object to pass to the updat butler.
     *
     * @param plugin           plugin instance
     * @param notifyPermission permission which will be required for update notification
     * @param notifyUpdate     true if users with permission should be notified
     * @param autoUpdate       true if an auto update should be performed
     * @param butlerId         id of buttler application
     * @param host             host of butler instance
     */
    ButlerUpdateData(Plugin plugin, String notifyPermission, boolean notifyUpdate, boolean autoUpdate, int butlerId, String host, String updateUrl, String updateMessage) {
        super(plugin, notifyPermission, notifyUpdate, autoUpdate, updateUrl, updateMessage);
        this.butlerId = butlerId;
        this.host = host;
    }

    public static ButlerUpdateDataBuilder builder(Plugin plugin, int butlerId) {
        return new ButlerUpdateDataBuilder(plugin, butlerId);
    }

    public int butlerId() {
        return butlerId;
    }

    public String host() {
        return host;
    }
}
