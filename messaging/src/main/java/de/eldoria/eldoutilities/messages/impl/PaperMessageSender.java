/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages.impl;

import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class PaperMessageSender extends MessageSender {
    public PaperMessageSender(
            Plugin plugin,
            MiniMessage miniMessage,
            TagResolver messageTagResolver,
            TagResolver errorTagResolver,
            Component prefix) {
        super(plugin, miniMessage, messageTagResolver, errorTagResolver, prefix);
    }

    /**
     * Creates a Paper message sender that localizes with the given localizer.
     *
     * @param plugin             owning plugin
     * @param miniMessage        MiniMessage instance used to parse messages
     * @param messageTagResolver tags of normal messages
     * @param errorTagResolver   tags of error messages
     * @param prefix             prefix put in front of every message
     * @param localizer          localizer for locale codes, or null to use the localizer registered for the plugin
     */
    public PaperMessageSender(
            Plugin plugin,
            MiniMessage miniMessage,
            TagResolver messageTagResolver,
            TagResolver errorTagResolver,
            Component prefix,
            @Nullable ILocalizer localizer) {
        super(plugin, miniMessage, messageTagResolver, errorTagResolver, prefix, localizer);
    }

    public void sendMessage(CommandSender sender, Component component) {
        sender.sendMessage(applyPrefix(component));
    }

    public void broadcast(String message) {
        plugin().getServer().broadcast(serialize(null, message, messageTagResolver()));
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
        player.sendActionBar(serialize(player, message, messageTagResolver(), placeholder));
    }

    /**
     * Send a localized action bar to a player
     *
     * @param player  player to send
     * @param message message to send
     */
    public void sendErrorActionBar(Player player, String message, TagResolver... placeholder) {
        player.sendActionBar(serialize(player, message, errorTagResolver(), placeholder));
    }

    public void sendBossBar(Player player, BossBar bossBar) {
        player.showBossBar(bossBar);
    }

    public BossBar sendBossBar(
            Player player,
            String message,
            float progress,
            BossBar.Color color,
            BossBar.Overlay overlay,
            Set<BossBar.Flag> flags) {
        var bossBar =
                BossBar.bossBar(serialize(player, message, messageTagResolver()), progress, color, overlay, flags);
        player.showBossBar(bossBar);
        return bossBar;
    }

    public void hideBossBar(Player player, BossBar bossBar) {
        player.hideBossBar(bossBar);
    }

    @Override
    public Audience asAudience(Player player) {
        return player;
    }
}
