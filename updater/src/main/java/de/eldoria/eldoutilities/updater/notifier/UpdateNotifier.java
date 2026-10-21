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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Notifier implementation for updater with update check function.
 * <p>
 * When the updater offers the download or switching automatic updates on, the message ends with buttons running the
 * matching commands.
 *
 * @since 1.1.0
 */
public class UpdateNotifier<T extends UpdateResponse> extends Notifier<T> {
    private final @Nullable String downloadCommand;
    private final @Nullable String autoUpdateCommand;

    public UpdateNotifier(Plugin plugin, UpdateData<T> data, T latest) {
        this(plugin, data, latest, null, null);
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
        this(plugin, data, latest, downloadCommand, null);
    }

    /**
     * Creates a notifier offering the update for download and automatic updates.
     *
     * @param plugin            plugin instance
     * @param data              update data
     * @param latest            the update check response
     * @param downloadCommand   command line downloading the update, or null to offer no download
     * @param autoUpdateCommand command line switching automatic updates on, or null to not offer it
     */
    public UpdateNotifier(Plugin plugin, UpdateData<T> data, T latest, @Nullable String downloadCommand,
                          @Nullable String autoUpdateCommand) {
        super(plugin, data, latest);
        this.downloadCommand = downloadCommand;
        this.autoUpdateCommand = autoUpdateCommand;
    }

    @Override
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // send to operator.
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission(data.notifyPermission())) {
            data.messageSender().sendMessage(event.getPlayer(), message(), data.placeholders(latest()));
        }
    }

    private String message() {
        var buttons = new ArrayList<String>();
        if (downloadCommand != null) {
            buttons.add(button(downloadCommand, UpdaterMessages.DOWNLOAD_HOVER, UpdaterMessages.DOWNLOAD_BUTTON));
        }
        if (autoUpdateCommand != null) {
            buttons.add(button(autoUpdateCommand, UpdaterMessages.AUTO_HOVER, UpdaterMessages.AUTO_BUTTON));
        }
        var message = data.updateMessage(latest());
        if (buttons.isEmpty()) return message;
        return message.stripTrailing() + "\n" + String.join(" ", buttons);
    }

    private String button(String command, String hover, String label) {
        return "<click:run_command:'%s'><hover:show_text:'%s'>%s</hover></click>".formatted(
                command, UpdaterMessages.resolve(plugin, hover), UpdaterMessages.resolve(plugin, label));
    }
}
