/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.notifier;

import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.updater.UpdateData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

/**
 * Notifier implementation for updater with download function.
 *
 * @since 1.1.0
 */
public class DownloadedNotifier extends UpdateNotifier {
    private final boolean updated;

    public DownloadedNotifier(Plugin plugin, UpdateData data, String latestVersion, boolean updated) {
        super(plugin, data, latestVersion);
        this.updated = updated;
    }

    @Override
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var description = plugin.getDescription();
        // send to operator.
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission(data.notifyPermission())) {
            if (updated) {
                MessageSender.getPluginMessageSender(plugin).sendMessage(event.getPlayer(),
                        "New version of §b" + plugin.getName() + "§r downloaded.\n"
                        + "Newest version: §a" + latest + "§r! Current version: §c" + description.getVersion() + "§r!\n"
                        + "Restart to apply update. Patchnotes can be found here: §b" + description.getWebsite());
            } else {
                MessageSender.getPluginMessageSender(plugin).sendMessage(event.getPlayer(), data.updateMessage(latest()));
            }
        }
    }
}
