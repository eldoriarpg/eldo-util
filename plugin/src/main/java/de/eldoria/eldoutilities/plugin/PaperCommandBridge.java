/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.plugin;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Hands a command that Paper registered for a plugin without a {@code plugin.yml} entry to a Bukkit
 * {@link TabExecutor}. Paper plugins declare no commands in their {@code paper-plugin.yml}, so their commands are
 * registered through {@link org.bukkit.plugin.java.JavaPlugin#registerCommand(String, Collection, BasicCommand)}.
 */
final class PaperCommandBridge implements BasicCommand {
    private final String label;
    private final TabExecutor executor;
    private final Command command;

    PaperCommandBridge(String label, TabExecutor executor) {
        this.label = label;
        this.executor = executor;
        this.command = new BridgedCommand(label, executor);
    }

    @Override
    public void execute(@NotNull CommandSourceStack source, String @NotNull [] args) {
        executor.onCommand(source.getSender(), command, label, args);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String @NotNull [] args) {
        List<String> suggestions = executor.onTabComplete(source.getSender(), command, label, args);
        return suggestions == null ? List.of() : suggestions;
    }

    /**
     * The Bukkit command passed to the executor, for executors that read its name or run it again.
     */
    private static final class BridgedCommand extends Command {
        private final TabExecutor executor;

        private BridgedCommand(String name, TabExecutor executor) {
            super(name);
            this.executor = executor;
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String @NotNull [] args) {
            return executor.onCommand(sender, this, commandLabel, args);
        }
    }
}
