/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.simplecommands.commands;

import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;


/**
 * @deprecated Use {@link de.eldoria.eldoutilities.commands.defaultcommands.DefaultAbout} instead.
 */
@Deprecated(forRemoval = true)
public class DefaultAbout extends EldoCommand {
    private String discord = "rfRuUge";

    public DefaultAbout(Plugin plugin) {
        super(plugin);
    }

    public DefaultAbout(Plugin plugin, String discord) {
        super(plugin);
        this.discord = discord;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var descr = getPlugin().getDescription();
        messageSender().sendLocalizedMessage(sender, "about",
                Replacement.create("PLUGIN_NAME", descr.getName(), 'b'),
                Replacement.create("AUTHORS", String.join(", ", descr.getAuthors()), 'b'),
                Replacement.create("VERSION", descr.getVersion(), 'b'),
                Replacement.create("WEBSITE", descr.getWebsite(), 'b'),
                Replacement.create("DISCORD", "https://discord.gg/" + discord, 'b'));
        return true;
    }
}
