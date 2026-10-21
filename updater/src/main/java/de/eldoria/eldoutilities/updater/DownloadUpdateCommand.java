/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import de.eldoria.eldoutilities.messages.MessageSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Downloads an available update on request, for updaters that do not update on their own.
 * <p>
 * The command is registered at runtime under the name of the plugin, so a plugin does not have to declare it. The
 * command map is looked up reflectively, because the Spigot API does not expose it.
 */
class DownloadUpdateCommand extends Command {
    private static final String NAME = "download-update";
    private final Plugin plugin;
    private final Updater<?, ?> updater;

    private DownloadUpdateCommand(Plugin plugin, Updater<?, ?> updater) {
        super(NAME, "Downloads the available update of " + plugin.getName(), "/" + NAME, List.of());
        this.plugin = plugin;
        this.updater = updater;
    }

    /**
     * Registers the command for the updater on the server's global thread.
     *
     * @param plugin  plugin owning the updater
     * @param updater updater performing the download
     * @return the command line running the command
     */
    static String register(Plugin plugin, Updater<?, ?> updater) {
        var prefix = plugin.getName().toLowerCase(Locale.ROOT);
        ServerTasks.runGlobal(plugin, () -> {
            try {
                var server = plugin.getServer();
                var commandMap = (CommandMap) server.getClass().getMethod("getCommandMap").invoke(server);
                commandMap.register(prefix, new DownloadUpdateCommand(plugin, updater));
            } catch (ReflectiveOperationException | ClassCastException e) {
                plugin.getLogger().log(Level.WARNING, "Could not register the update download command.", e);
            }
        });
        return "/%s:%s".formatted(prefix, NAME);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        var messages = MessageSender.getPluginMessageSender(plugin);
        if (!mayDownload(sender)) {
            messages.sendError(sender, "You are not allowed to download updates of " + plugin.getName() + ".");
            return true;
        }
        messages.sendMessage(sender, "Downloading the update of " + plugin.getName() + "...");
        updater.download().thenAccept(downloaded -> ServerTasks.runGlobal(plugin, () -> {
            if (downloaded) {
                messages.sendMessage(sender, "<green>Update of " + plugin.getName() + " downloaded. Restart the server to apply it.");
            } else {
                messages.sendError(sender, "The update of " + plugin.getName() + " could not be downloaded. See the console for details.");
            }
        }));
        return true;
    }

    private boolean mayDownload(CommandSender sender) {
        return sender instanceof ConsoleCommandSender
               || sender.isOp()
               || sender.hasPermission(updater.getData().notifyPermission());
    }
}
