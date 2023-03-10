/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.notifier;

import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.updater.Notifier;
import de.eldoria.eldoutilities.updater.UpdateData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * Notifier implementation for updater with update check function.
 *
 * @since 1.1.0
 */
public class UpdateNotifier extends Notifier {

    public UpdateNotifier(Plugin plugin, UpdateData data, String latest) {
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
