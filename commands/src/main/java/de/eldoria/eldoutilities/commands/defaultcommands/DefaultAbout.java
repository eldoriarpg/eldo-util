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
        this(plugin, "https://discord.eldoria.de", "commands.about");
    }

    public DefaultAbout(Plugin plugin, String discord, String localeTag) {
        super(plugin, CommandMeta.builder(localeTag).build());
        this.discord = discord;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) {
        var descr = plugin().getDescription();
        messageSender().sendMessage(sender, "about",
                Replacement.create("PLUGIN_NAME", descr.getName()),
                Replacement.create("AUTHORS", String.join(", ", descr.getAuthors())),
                Replacement.create("VERSION", descr.getVersion()),
                Replacement.create("WEBSITE", descr.getWebsite()),
                Replacement.create("DISCORD", discord));
    }
}
