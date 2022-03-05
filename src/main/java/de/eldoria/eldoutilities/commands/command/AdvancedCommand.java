/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.command;

import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.command.util.CommandAssertions;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.IConsoleTabExecutor;
import de.eldoria.eldoutilities.commands.executor.IPlayerTabExecutor;
import de.eldoria.eldoutilities.commands.executor.ITabExecutor;
import de.eldoria.eldoutilities.localization.DummyLocalizer;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AdvancedCommand implements CommandRoute {
    private final Plugin plugin;
    private CommandMeta meta;
    private ILocalizer localizer;
    private MessageSender messageSender;

    public AdvancedCommand(Plugin plugin, CommandMeta meta) {
        this.plugin = plugin;
        this.meta = meta;
        linkMeta();
    }

    public AdvancedCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    private void linkMeta() {
        for (var cmd : meta.subCommands().values()) {
            cmd.meta().parent(this);
        }
    }

    @Override
    public void commandRoute(CommandSender sender, String label, Arguments args) throws CommandException {
        CommandAssertions.permission(sender, meta(), false);
        if (this instanceof IConsoleTabExecutor) {
            CommandAssertions.console(sender);
        }
        if (this instanceof IPlayerTabExecutor) {
            CommandAssertions.player(sender);
        }
        if (this instanceof IPlayerTabExecutor && sender instanceof Player) {
            CommandAssertions.invalidArguments(meta(), args);
            ((IPlayerTabExecutor) this).onCommand((Player) sender, label, args);
            return;
        }

        if (this instanceof IConsoleTabExecutor && sender instanceof ConsoleCommandSender) {
            CommandAssertions.invalidArguments(meta(), args);
            ((IConsoleTabExecutor) this).onCommand((ConsoleCommandSender) sender, label, args);
            return;
        }
        if (this instanceof ITabExecutor) {
            CommandAssertions.invalidArguments(meta(), args);
            CommandAssertions.allowedSender(meta, sender);
            ((ITabExecutor) this).onCommand(sender, label, args);
            return;
        }

        if (args.isEmpty()) {
            CommandAssertions.unexpectedRouteEnd(meta, args);
            meta.defaultCommand().commandRoute(sender, label, args);
            return;
        }

        if (meta.subCommands().isEmpty()) {
            throw CommandException.message("Unhandled end of command route. The command needs to implement a executor or subcommands");
        }
        final var newArgs = args.subArguments();

        var command = getCommand(args.asString(0));
        CommandAssertions.unexpectedRouteEnd(meta, command);
        command.get().commandRoute(sender, args.asString(0), newArgs);
    }

    @Override
    public @Nullable List<String> tabCompleteRoute(CommandSender sender, String label, Arguments args) throws CommandException {
        if (!meta().permissions().isEmpty()) {
            var access = false;
            for (var permission : meta().permissions()) {
                if (sender.hasPermission(permission)) {
                    access = true;
                    break;
                }
            }
            if (!access) {
                var permissions = String.join(", ", meta().permissions());
                return Collections.singletonList(localizer.localize("error.permission",
                        Replacement.create("PERMISSION", permissions)));
            }
        }

        // Check for end of route
        if (this instanceof IPlayerTabExecutor && sender instanceof Player) {
            return ((IPlayerTabExecutor) this).onTabComplete((Player) sender, label, args);
        }

        if (this instanceof IConsoleTabExecutor && sender instanceof ConsoleCommandSender) {
            return ((IConsoleTabExecutor) this).onTabComplete((ConsoleCommandSender) sender, label, args);
        }

        if (this instanceof ITabExecutor) {
            return ((ITabExecutor) this).onTabComplete(sender, label, args);
        }

        // Provide routes
        if (args.size() == 1) {
            return TabCompleteUtil.complete(args.asString(0), meta.registeredCommands());
        }

        var command = getCommand(args.asString(0));
        if (!command.isPresent()) {
            return Collections.singletonList(localizer().getMessage("error.invalidCommand"));
        }
        // forward
        return command.get().tabCompleteRoute(sender, args.asString(0), args.subArguments());
    }

    private Optional<AdvancedCommand> getCommand(String command) {
        for (var entry : meta.subCommands().values()) {
            if (entry.meta().isCommand(command)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    /**
     * Get a instance of the localizer.
     *
     * @return localizer instance
     */
    protected final ILocalizer localizer() {
        if (localizer == null || localizer instanceof DummyLocalizer) {
            localizer = ILocalizer.getPluginLocalizer(plugin);
        }
        return localizer;
    }

    /**
     * Get a instance of the message sender.
     *
     * @return message sender instance
     */
    protected final MessageSender messageSender() {
        if (messageSender == null || messageSender.isDefault()) {
            messageSender = MessageSender.getPluginMessageSender(plugin);
        }
        return messageSender;
    }

    /**
     * Get the player from a sender if the sender is a instance of {@link Player} returns true.
     *
     * @param sender sender to cast
     * @return player or null if sender is not player
     */
    protected final Player getPlayerFromSender(CommandSender sender) {
        return (sender instanceof Player) ? (Player) sender : null;
    }

    public Plugin plugin() {
        return plugin;
    }

    public final CommandMeta meta() {
        Objects.requireNonNull(meta);
        return meta;
    }

    protected final void meta(CommandMeta meta) {
        if (this.meta != null) throw new IllegalStateException("Meta is already assigned");
        this.meta = meta;
        linkMeta();
    }
}
