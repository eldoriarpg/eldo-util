/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.defaultcommands;

import de.eldoria.eldoutilities.commands.command.util.Arguments;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class FailsaveCommand extends DefaultDebug {
    public FailsaveCommand(Plugin plugin, String permission) {
        super(plugin, permission);
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) {
        if (args.isEmpty()) {
            messageSender().sendError(sender, "<red>The plugin failed to load correctly. Please use <gold>/" + alias + "<red> debug to create a debug paste and send it to the developer.");
            return;
        }
        if ("debug".equalsIgnoreCase(args.get(0).asString())) {
            super.onCommand(sender, alias, args);
            return;
        }
        messageSender().sendMessage(sender, "<red>The plugin failed to load correctly. Please use <gold>/" + alias + "<red> debug to create a debug paste and send it to the developer.");
    }
}
