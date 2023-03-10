/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import de.eldoria.eldoutilities.debug.UserData;
import de.eldoria.eldoutilities.updater.UpdateData;
import org.bukkit.plugin.Plugin;

/**
 * Update Data implementation for butler application.
 *
 * @since 1.1.0
 */
public class LynaUpdateData extends UpdateData {
    /**
     * Default adress to submit debug data and update checks
     */
    public static final String HOST = "https://lyna.eldoria.de";

    private final int productId;
    private final String host;
    private final UserData userData;

    /**
     * Create a new update data object to pass to lyna.
     *
     * @param plugin           plugin instance
     * @param notifyPermission permission which will be required for update notification
     * @param notifyUpdate     true if users with permission should be notified
     * @param productId        id of lyna product
     * @param host             host of lyna instance
     */
    LynaUpdateData(Plugin plugin, String notifyPermission, boolean notifyUpdate, int productId, String host, String updateUrl, String updateMessage) {
        super(plugin, notifyPermission, notifyUpdate, false, updateUrl, updateMessage);
        this.productId = productId;
        this.host = host;
        userData = UserData.get(plugin);
    }

    public static LynaUpdateDataBuilder builder(Plugin plugin, int productId) {
        return new LynaUpdateDataBuilder(plugin, productId);
    }

    public int productId() {
        return productId;
    }

    public String host() {
        return host;
    }

    public UserData userData() {
        return userData;
    }
}
