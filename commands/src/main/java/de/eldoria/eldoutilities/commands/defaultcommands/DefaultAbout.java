/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.defaultcommands;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.executor.ITabExecutor;
import de.eldoria.eldoutilities.messages.Replacement;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class DefaultAbout extends AdvancedCommand implements ITabExecutor {
    private final String discord;

    public DefaultAbout(Plugin plugin) {
        this(plugin, "rfRuUge");
    }

    public DefaultAbout(Plugin plugin, String discord) {
        super(plugin, CommandMeta.builder("about").build());
        this.discord = discord;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) {
        var descr = plugin().getDescription();
        messageSender().sendMessage(sender, "about",
                Replacement.create("PLUGIN_NAME", descr.getName(), Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)),
                Replacement.create("AUTHORS", String.join(", ", descr.getAuthors()), Style.style(TextDecoration.BOLD)),
                Replacement.create("VERSION", descr.getVersion(), Style.style(TextDecoration.BOLD)),
                Replacement.create("WEBSITE", descr.getWebsite(), Style.style(TextDecoration.BOLD)),
                Replacement.create("DISCORD", "https://discord.gg/" + discord, Style.style(TextDecoration.BOLD)));
    }
}
