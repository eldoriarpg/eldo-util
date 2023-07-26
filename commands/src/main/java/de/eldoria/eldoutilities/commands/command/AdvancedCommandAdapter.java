/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.command;

import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Wraps a {@link AdvancedCommand} into a {@link TabExecutor}
 */
public class AdvancedCommandAdapter extends AdvancedCommand implements TabExecutor {
    private final AdvancedCommand advancedCommand;

    private AdvancedCommandAdapter(Plugin plugin, AdvancedCommand advancedCommand) {
        super(plugin);
        this.advancedCommand = advancedCommand;
    }

    public static AdvancedCommandAdapter wrap(Plugin plugin, AdvancedCommand advancedCommand) {
        return new AdvancedCommandAdapter(plugin, advancedCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            executeCommand(sender, label, args);
        } catch (CommandException e) {
            handleCommandError(sender, e);
        }
        return true;
    }

    private void executeCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) throws CommandException {
        var arguments = Arguments.create(plugin(), sender, args);
        advancedCommand.commandRoute(sender, label, arguments);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var arguments = Arguments.create(plugin(), sender, args);
        List<String> strings;
        try {
            strings = advancedCommand.tabCompleteRoute(sender, label, arguments);
        } catch (CommandException e) {
            strings = Collections.singletonList(messageSender().translatePlain(e.getMessage(), e.replacements()));
            plugin().getLogger().log(Level.CONFIG, "Command exception occured.", e);
        }
        return strings;
    }
}
