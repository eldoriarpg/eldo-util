/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.spigotupdater;

import de.eldoria.eldoutilities.updater.DefaultUpdateResponse;
import de.eldoria.eldoutilities.updater.UpdateData;
import de.eldoria.eldoutilities.messages.MessageSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * Updater implementation for spigot update check.
 *
 * @since 1.0.0
 * @deprecated Use the Lyna updater from {@link de.eldoria.eldoutilities.updater.Updater#lyna}. Removed in 4.0.
 */
@Deprecated(since = "3.6.0", forRemoval = true)
public class SpigotUpdateData extends UpdateData<DefaultUpdateResponse> {
    private final int spigotId;

    /**
     * Creates a new Spigot Update Data.
     *
     * @param plugin           plugin to update
     * @param notifyPermission permission to receive notification
     * @param notifyUpdate     set to true to notify admins on login
     * @param spigotId         spigot id of the plugin
     */
    SpigotUpdateData(Plugin plugin, String notifyPermission, boolean notifyUpdate, int spigotId, String updateUrl, String updateMessage,
                     @Nullable MessageSender messageSender) {
        super(plugin, notifyPermission, notifyUpdate, false, updateUrl, updateMessage, messageSender);
        this.spigotId = spigotId;
    }

    public static SpigotUpdateDataBuilder builder(Plugin plugin, int spigotId) {
        return new SpigotUpdateDataBuilder(plugin, spigotId);
    }

    public int getSpigotId() {
        return spigotId;
    }
}
