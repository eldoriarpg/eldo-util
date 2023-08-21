/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.defaultcommands;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.executor.ITabExecutor;
import de.eldoria.eldoutilities.debug.DebugSettings;
import de.eldoria.eldoutilities.debug.DebugUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class DefaultDebug extends AdvancedCommand implements ITabExecutor {

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

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) {
        DebugUtil.dispatchDebug(sender, plugin(), settings);
    }
}
