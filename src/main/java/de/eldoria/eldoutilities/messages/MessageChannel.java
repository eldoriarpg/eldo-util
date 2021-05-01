package de.eldoria.eldoutilities.messages;

import de.eldoria.eldoutilities.core.EldoUtilities;
import de.eldoria.eldoutilities.messages.channeldata.BossBarData;
import de.eldoria.eldoutilities.messages.channeldata.ChannelData;
import de.eldoria.eldoutilities.messages.channeldata.TitleData;
import de.eldoria.eldoutilities.utils.ObjUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public interface MessageChannel<T extends ChannelData> {
    public static final String KEY_PREFIX = "messageChannel";

    /**
     * Default implementation for a chat message
     */
    public static MessageChannel<? extends ChannelData> CHAT = new MessageChannel<ChannelData>() {
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
    public static MessageChannel<TitleData> TITLE = (message, sender, target, data) -> {
        TitleData titleData = data;
        if (titleData == null) titleData = TitleData.DEFAULT;
        if (target instanceof Player) {
            ((Player) target).sendTitle(message, titleData.getOtherLine(), titleData.getFadeIn(), titleData.getStay(), titleData.getFadeOut());
        } else {
            sender.send(CHAT, MessageType.NORMAL, target, message);
        }
    };

    /**
     * Default implementation for a subtitle message
     */
    public static MessageChannel<TitleData> SUBTITLE = (message, sender, target, data) -> {
        TitleData titleData = data;
        if (titleData == null) titleData = TitleData.DEFAULT;
        if (target instanceof Player) {
            ((Player) target).sendTitle(titleData.getOtherLine(), message, titleData.getFadeIn(), titleData.getStay(), titleData.getFadeOut());
        } else {
            sender.send(CHAT, MessageType.NORMAL, target, message);
        }
    };

    /**
     * Default implementation for a action bar message
     */
    public static MessageChannel<? extends ChannelData> ACTION_BAR = (message, sender, target, data) -> {
        if (target instanceof Player) {
            ((Player) target).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        } else {
            sender.send(CHAT, MessageType.NORMAL, target, message);
        }
    };

    public static MessageChannel<BossBarData> BOSS_BAR = (message, sender, target, data) -> {
        BossBarData bossBarData = data;
        if (bossBarData == null) {
            bossBarData = BossBarData.DEFAULT;
        }
        if (target instanceof Player) {
            String key = KEY_PREFIX + target.getName() + ThreadLocalRandom.current().nextInt(10000, 99999);
            NamespacedKey barKey = new NamespacedKey(EldoUtilities.getInstanceOwner(), key);
            BossBar bossBar = bossBarData.create(barKey, message);
            bossBar.setProgress(1);
            bossBar.addPlayer((Player) target);
            EldoUtilities.getDelayedActions().schedule(() -> {
                bossBar.removeAll();
                Bukkit.removeBossBar(barKey);
            }, bossBarData.getDuration());
        } else {
            target.sendMessage(message);
        }
    };

    /**
     * Get a default channel by name.
     *
     * @param name name of channel not case sensitive
     * @return channel or {@link #CHAT} if channel is not found or name is null
     */
    public static @NotNull MessageChannel<? extends ChannelData> getChannelByNameOrDefault(@Nullable String name) {
        return ObjUtil.nonNull(getChannelByName(name), CHAT);
    }

    /**
     * Get a default channel by name.
     *
     * @param name name of channel not case sensitive
     * @return channel or null if name is null or no matching channel is found
     */
    public static @Nullable MessageChannel<? extends ChannelData> getChannelByName(@Nullable String name) {
        if ("CHAT".equalsIgnoreCase(name)) {
            return CHAT;
        }

        if ("TITLE".equalsIgnoreCase(name)) {
            return TITLE;
        }

        if ("SUBTITLE".equalsIgnoreCase(name)) {
            return SUBTITLE;
        }

        if ("ACTION_BAR".equalsIgnoreCase(name)) {
            return ACTION_BAR;
        }
        if ("BOSS_BAR".equalsIgnoreCase(name)) {
            return BOSS_BAR;
        }

        return null;
    }

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
}
