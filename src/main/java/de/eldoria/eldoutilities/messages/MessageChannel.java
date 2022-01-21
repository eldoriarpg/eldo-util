/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages;

import de.eldoria.eldoutilities.core.EldoUtilities;
import de.eldoria.eldoutilities.messages.channeldata.BossBarData;
import de.eldoria.eldoutilities.messages.channeldata.ChannelData;
import de.eldoria.eldoutilities.messages.channeldata.TitleData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public interface MessageChannel<T extends ChannelData> {
    String KEY_PREFIX = "messageChannel";

    /**
     * Default implementation for a chat message
     */
    MessageChannel<? extends ChannelData> CHAT = new MessageChannel<>() {
        @Override
        public String name() {
            return "CHAT";
        }

        @Override
        public void sendMessage(String message, MessageSender sender, CommandSender target, ChannelData data) {
            target.sendMessage(message);
        }

        @Override
        public String addPrefix(String message, String prefix) {
            return prefix + message;
        }
    };

    /**
     * Default implementation for a title message
     */
    MessageChannel<TitleData> TITLE = new MessageChannel<>() {
        @Override
        public String name() {
            return "TITLE";
        }

        @Override
        public void sendMessage(String message, MessageSender sender, CommandSender target, TitleData data) {
            var titleData = data;
            if (titleData == null) titleData = TitleData.DEFAULT;
            if (target instanceof Player) {
                ((Player) target).sendTitle(message, titleData.getOtherLine(), titleData.getFadeIn(), titleData.getStay(), titleData.getFadeOut());
            } else {
                sender.send(CHAT, MessageType.NORMAL, target, message);
            }
        }
    };

    /**
     * Default implementation for a subtitle message
     */
    MessageChannel<TitleData> SUBTITLE = new MessageChannel<>() {
        @Override
        public String name() {
            return "SUBTITLE";
        }

        @Override
        public void sendMessage(String message, MessageSender sender, CommandSender target, TitleData data) {
            var titleData = data;
            if (titleData == null) titleData = TitleData.DEFAULT;
            if (target instanceof Player) {
                ((Player) target).sendTitle(titleData.getOtherLine(), message, titleData.getFadeIn(), titleData.getStay(), titleData.getFadeOut());
            } else {
                sender.send(CHAT, MessageType.NORMAL, target, message);
            }

        }
    };

    /**
     * Default implementation for a action bar message
     */
    MessageChannel<? extends ChannelData> ACTION_BAR = new MessageChannel<>() {
        @Override
        public String name() {
            return "ACTION_BAR";
        }

        @Override
        public void sendMessage(String message, MessageSender sender, CommandSender target, ChannelData data) {
            if (target instanceof Player) {
                ((Player) target).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            } else {
                sender.send(CHAT, MessageType.NORMAL, target, message);
            }
        }
    };

    MessageChannel<BossBarData> BOSS_BAR = new MessageChannel<>() {
        @Override
        public String name() {
            return "BOSS_BAR";
        }

        @Override
        public void sendMessage(String message, MessageSender sender, CommandSender target, BossBarData data) {
            var bossBarData = data;
            if (bossBarData == null) {
                bossBarData = BossBarData.DEFAULT;
            }
            if (target instanceof Player) {
                var key = KEY_PREFIX + target.getName() + ThreadLocalRandom.current().nextInt(10000, 99999);
                var barKey = new NamespacedKey(EldoUtilities.getInstanceOwner(), key);
                var bossBar = bossBarData.create(barKey, message);
                bossBar.setProgress(1);
                bossBar.addPlayer((Player) target);
                Bukkit.getScheduler().runTaskLater(EldoUtilities.getInstanceOwner(), () -> {
                    bossBar.removeAll();
                    Bukkit.removeBossBar(barKey);
                }, bossBarData.getDuration());
            } else {
                target.sendMessage(message);
            }
        }
    };

    MessageChannel<? extends ChannelData> BROADCAST = new MessageChannel<>() {
        @Override
        public String name() {
            return "BROADCAST";
        }

        @Override
        public void sendMessage(String message, MessageSender sender, CommandSender target, ChannelData data) {
            Bukkit.broadcastMessage(message);
        }

        @Override
        public String addPrefix(String message, String prefix) {
            return prefix + message;
        }
    };

    /**
     * Get a default channel by name.
     *
     * @param name name of channel not case sensitive
     * @return channel or {@link #CHAT} if channel is not found or name is null
     */
    static @NotNull MessageChannel<? extends ChannelData> getChannelByNameOrDefault(@Nullable String name) {
        return getChannelByName(name).orElse(CHAT);
    }

    /**
     * Get a default channel by name.
     *
     * @param name name of channel not case sensitive
     * @return channel or null if name is null or no matching channel is found
     */
    static Optional<MessageChannel<? extends ChannelData>> getChannelByName(@Nullable String name) {
        for (var value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    /**
     * Get the name of the channel
     *
     * @return name of channel
     */
    String name();

    /**
     * Send a message via this channel to a target with the delivered message sender instance.
     *
     * @param message message to send
     * @param sender  message sender instance
     * @param target  target of message
     * @param data    Additional data for the channel
     */
    void sendMessage(String message, MessageSender sender, CommandSender target, T data);

    /**
     * Send a message via this channel to a target with the delivered message sender instance.
     *
     * @param message message to send
     * @param sender  message sender instance
     * @param target  target of message
     */
    default void sendMessage(String message, MessageSender sender, CommandSender target) {
        sendMessage(message, sender, target, null);
    }

    default String addPrefix(String message, String prefix) {
        return message;
    }

    static MessageChannel<?>[] values() {
        return new MessageChannel[]{CHAT, TITLE, SUBTITLE, ACTION_BAR, BOSS_BAR, BROADCAST};
    }
}
