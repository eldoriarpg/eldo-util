/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * The actions behind the buttons of the update messages: {@code download} the update, {@code enable-auto} switch
 * automatic updates on, ask again {@code later}, or {@code never} ask again. Without an action it downloads.
 * <p>
 * The command is registered at runtime as {@code /<plugin>:eldo-update}, so a plugin does not have to declare it, and
 * the plugin's namespace keeps it apart from commands of the plugin itself. Only the console, operators and holders of
 * the notify permission may use it.
 */
class UpdateCommand extends Command {
    private static final String NAME = "eldo-update";
    private final Plugin plugin;
    private final Updater<?, ?> updater;
    private final String commandLine;

    UpdateCommand(Plugin plugin, Updater<?, ?> updater) {
        super(NAME, "Manages updates of " + plugin.getName(), "/" + NAME + " <download|enable-auto|later|never>", List.of());
        this.plugin = plugin;
        this.updater = updater;
        this.commandLine = commandLine(plugin);
    }

    /**
     * Registers the command for the updater on the server's global thread.
     *
     * @param plugin  plugin owning the updater
     * @param updater updater performing the actions
     * @return the command line running the command, without an action
     */
    static String register(Plugin plugin, Updater<?, ?> updater) {
        ServerTasks.runGlobal(plugin, () -> plugin.getServer().getCommandMap()
                .register(plugin.getName().toLowerCase(Locale.ROOT), new UpdateCommand(plugin, updater)));
        return commandLine(plugin);
    }

    private static String commandLine(Plugin plugin) {
        return "/%s:%s".formatted(plugin.getName().toLowerCase(Locale.ROOT), NAME);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!mayManage(sender)) {
            sendError(sender, UpdaterMessages.DOWNLOAD_DENIED);
            return true;
        }
        var action = args.length == 0 ? "download" : args[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "download" -> download(sender);
            case "enable-auto" -> enableAutoUpdate(sender);
            case "later" -> send(sender, UpdaterMessages.AUTO_ASK_LATER);
            case "never" -> updater.declineAutoUpdate()
                    .thenRun(() -> ServerTasks.runGlobal(plugin, () -> send(sender, UpdaterMessages.AUTO_NOT_ASKED)));
            default -> sendError(sender, UpdaterMessages.UNKNOWN_ACTION);
        }
        return true;
    }

    /**
     * Downloads the update and, when automatic updates are offered, asks whether future updates should be installed
     * automatically.
     *
     * @param sender who asked for the download
     * @return future completing once the result was sent
     */
    CompletableFuture<Void> download(CommandSender sender) {
        send(sender, UpdaterMessages.DOWNLOADING);
        return updater.download().thenAccept(downloaded -> ServerTasks.runGlobal(plugin, () -> {
            if (!downloaded) {
                sendError(sender, UpdaterMessages.DOWNLOAD_FAILED);
                return;
            }
            send(sender, UpdaterMessages.DOWNLOAD_DONE);
            if (updater.offersAutoUpdate()) {
                updater.getData().messageSender().sendMessage(sender, autoUpdatePrompt(), placeholders());
            }
        }));
    }

    /**
     * Switches automatic updates on.
     *
     * @param sender who switched them on
     * @return future completing once the result was sent
     */
    CompletableFuture<Void> enableAutoUpdate(CommandSender sender) {
        return updater.enableAutoUpdate().thenAccept(enabled -> ServerTasks.runGlobal(plugin, () -> {
            if (enabled) {
                send(sender, UpdaterMessages.AUTO_ENABLED);
            } else {
                sendError(sender, UpdaterMessages.AUTO_UNAVAILABLE);
            }
        }));
    }

    private String autoUpdatePrompt() {
        return "%s\n%s %s %s".formatted(
                text(UpdaterMessages.AUTO_PROMPT),
                button("enable-auto", UpdaterMessages.AUTO_YES),
                button("later", UpdaterMessages.AUTO_LATER),
                button("never", UpdaterMessages.AUTO_NEVER));
    }

    private String button(String action, String label) {
        return "<click:run_command:'%s %s'>%s</click>".formatted(commandLine, action, text(label));
    }

    private void send(CommandSender sender, String key) {
        updater.getData().messageSender().sendMessage(sender, text(key), placeholders());
    }

    private void sendError(CommandSender sender, String key) {
        updater.getData().messageSender().sendError(sender, text(key), placeholders());
    }

    private String text(String key) {
        return UpdaterMessages.resolve(plugin, key);
    }

    private TagResolver placeholders() {
        return Placeholder.unparsed("plugin_name", plugin.getName());
    }

    private boolean mayManage(CommandSender sender) {
        return sender instanceof ConsoleCommandSender
               || sender.isOp()
               || sender.hasPermission(updater.getData().notifyPermission());
    }
}
