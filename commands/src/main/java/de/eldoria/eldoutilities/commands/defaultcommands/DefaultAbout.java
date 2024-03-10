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
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class DefaultAbout extends AdvancedCommand implements ITabExecutor {
    private final String localeTag;
    private final TagResolver resolver;

    public DefaultAbout(Plugin plugin) {
        this(plugin, "https://discord.eldoria.de", "commands.about");
    }

    public DefaultAbout(Plugin plugin, String discord, String localeTag, TagResolver... replacements) {
        super(plugin, CommandMeta.builder("about").build());
        this.localeTag = localeTag;
        var descr = plugin.getDescription();
        resolver = TagResolver.builder()
                .resolvers(Replacement.create("plugin_name", descr.getName()),
                        Replacement.create("authors", String.join(", ", descr.getAuthors())),
                        Replacement.create("version", descr.getVersion()),
                        Replacement.create("website", link(descr.getWebsite())),
                        Replacement.create("discord", link(discord)))
                .resolvers(replacements)
                .build();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) {
        messageSender().sendMessage(sender, localeTag, resolver);
    }

    private String link(String url) {
        return "<click:open_url:'%s'>%s</click>".formatted(url, url);
    }
}
