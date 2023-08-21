/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages.impl;

import de.eldoria.eldoutilities.messages.MessageSender;
import net.kyori.adventure.audience.Audiences;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public final class PaperMessageSender extends MessageSender {
    public PaperMessageSender(Plugin plugin, MiniMessage miniMessage, TagResolver messageTagResolver, TagResolver errorTagResolver, Component prefix) {
        super(plugin, miniMessage, messageTagResolver, errorTagResolver, prefix);
    }

    public void sendMessage(CommandSender sender, Component component) {
        sender.sendActionBar(applyPrefix(component));
    }

    public void broadcast(String message) {
        plugin().getServer().broadcast(serialize(message, messageTagResolver()));
    }

    /**
     * Send a title to a player
     *
     * @param player player to send
     * @param title  title to send
     */
    public void sendTitle(Player player, Title title) {
        player.showTitle(title);
    }

    /**
     * Send a localized action bar to a player
     *
     * @param player  player to send
     * @param message message to send
     */
    public void sendActionBar(Player player, String message, TagResolver... placeholder) {
        player.sendActionBar(serialize(message, messageTagResolver(), placeholder));
    }

    /**
     * Send a localized action bar to a player
     *
     * @param player  player to send
     * @param message message to send
     */
    public void sendErrorActionBar(Player player, String message, TagResolver... placeholder) {
        player.sendActionBar(serialize(message, errorTagResolver(), placeholder));
    }

    public void sendBossBar(Player player, BossBar bossBar) {
        player.showBossBar(bossBar);
    }

    public BossBar sendBossBar(Player player, String message, float progress, BossBar.Color color, BossBar.Overlay overlay, Set<BossBar.Flag> flags) {
        var bossBar = BossBar.bossBar(serialize(message, messageTagResolver()), progress, color, overlay, flags);
        player.showBossBar(bossBar);
        return bossBar;
    }

    public void hideBossBar(Player player, BossBar bossBar) {
        player.hideBossBar(bossBar);
    }
}
