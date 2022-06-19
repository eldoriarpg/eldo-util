/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.defaultcommands;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.IConsoleTabExecutor;
import de.eldoria.eldoutilities.commands.executor.IPlayerTabExecutor;
import de.eldoria.eldoutilities.debug.DebugSettings;
import de.eldoria.eldoutilities.debug.DebugUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class DefaultDebug extends AdvancedCommand implements IPlayerTabExecutor, IConsoleTabExecutor {

    private final DebugSettings settings;

    public DefaultDebug(Plugin plugin, String permission, DebugSettings settings) {
        super(plugin, CommandMeta.builder("debug")
                .withPermission(permission, "eldoutilitites.debug")
                .build());
        this.settings = settings;
    }

    public DefaultDebug(Plugin plugin, String permission) {
        this(plugin, permission, DebugSettings.DEFAULT);
    }

    protected void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) {
        DebugUtil.dispatchDebug(sender, plugin(), settings);
    }

    @Override
    public void onCommand(@NotNull ConsoleCommandSender console, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        onCommand((CommandSender) console, alias, args);
    }

    @Override
    public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        onCommand((CommandSender) player, alias, args);
    }
}
