/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.notifier;

import de.eldoria.eldoutilities.updater.UpdateData;
import de.eldoria.eldoutilities.updater.UpdateResponse;
import de.eldoria.eldoutilities.updater.UpdaterMessages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

/**
 * Notifier implementation for updater with download function.
 *
 * @since 1.1.0
 */
public class DownloadedNotifier<T extends UpdateResponse> extends UpdateNotifier<T> {
    private final boolean updated;

    public DownloadedNotifier(Plugin plugin, UpdateData<T> data, T latestVersion, boolean updated) {
        super(plugin, data, latestVersion);
        this.updated = updated;
    }

    @Override
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission(data.notifyPermission())) {
            var message = updated
                    ? UpdaterMessages.resolve(plugin, UpdaterMessages.DOWNLOADED)
                    : data.updateMessage(latest());
            data.messageSender().sendMessage(event.getPlayer(), message, data.placeholders(latest()));
        }
    }
}
