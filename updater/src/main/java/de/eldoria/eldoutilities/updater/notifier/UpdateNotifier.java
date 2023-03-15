/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.notifier;

import de.eldoria.eldoutilities.updater.UpdateData;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.updater.UpdateData;
import de.eldoria.eldoutilities.updater.UpdateResponse;
import de.eldoria.eldoutilities.updater.Notifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

/**
 * Notifier implementation for updater with update check function.
 *
 * @since 1.1.0
 */
public class UpdateNotifier<T extends UpdateResponse> extends Notifier<T> {

    public UpdateNotifier(Plugin plugin, UpdateData<T> data, T latest) {
        super(plugin, data, latest);
    }

    @Override
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // send to operator.
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission(data.notifyPermission())) {
            MessageSender.getPluginMessageSender(plugin).sendMessage(event.getPlayer(), data.updateMessage(latest()));
        }
    }
}
