/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.notifier;

import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.updater.UpdateData;
import de.eldoria.eldoutilities.updater.UpdateResponse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * Notifier implementation for updater with update check function.
 * <p>
 * When the updater offers the download, the message ends with a button running the download command.
 *
 * @since 1.1.0
 */
public class UpdateNotifier<T extends UpdateResponse> extends Notifier<T> {
    private final @Nullable String downloadCommand;

    public UpdateNotifier(Plugin plugin, UpdateData<T> data, T latest) {
        this(plugin, data, latest, null);
    }

    /**
     * Creates a notifier offering the update for download.
     *
     * @param plugin          plugin instance
     * @param data            update data
     * @param latest          the update check response
     * @param downloadCommand command line downloading the update, or null to offer no download
     */
    public UpdateNotifier(Plugin plugin, UpdateData<T> data, T latest, @Nullable String downloadCommand) {
        super(plugin, data, latest);
        this.downloadCommand = downloadCommand;
    }

    @Override
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // send to operator.
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission(data.notifyPermission())) {
            MessageSender.getPluginMessageSender(plugin).sendMessage(event.getPlayer(), message());
        }
    }

    private String message() {
        var message = data.updateMessage(latest());
        if (downloadCommand == null) return message;
        return message.stripTrailing() + "\n<click:run_command:'%s'><hover:show_text:'<green>Download version %s'><green>[Download]</green></hover></click>"
                .formatted(downloadCommand, latest().latestVersion());
    }
}
