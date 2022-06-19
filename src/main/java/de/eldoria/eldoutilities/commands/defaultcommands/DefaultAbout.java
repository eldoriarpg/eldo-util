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
import de.eldoria.eldoutilities.localization.Replacement;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class DefaultAbout extends AdvancedCommand implements IPlayerTabExecutor, IConsoleTabExecutor {
    private final String discord;

    public DefaultAbout(Plugin plugin) {
        this(plugin, "rfRuUge");
    }

    public DefaultAbout(Plugin plugin, String discord) {
        super(plugin, CommandMeta.builder("about").build());
        this.discord = discord;
    }

    public void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) {
        var descr = plugin().getDescription();
        messageSender().sendLocalizedMessage(sender, "about",
                Replacement.create("PLUGIN_NAME", descr.getName(), 'b'),
                Replacement.create("AUTHORS", String.join(", ", descr.getAuthors()), 'b'),
                Replacement.create("VERSION", descr.getVersion(), 'b'),
                Replacement.create("WEBSITE", descr.getWebsite(), 'b'),
                Replacement.create("DISCORD", "https://discord.gg/" + discord, 'b'));
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
