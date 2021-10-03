package de.eldoria.eldoutilities.messages;

import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.messages.channeldata.ChannelData;
import de.eldoria.eldoutilities.messages.channeldata.TitleData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A message sender to manage message sending.
 * <p>
 * Allows definition of a plugin prefix, Message color, Error color.
 * <p>
 * Allows sending of messages, error messages, titles.
 * <p>
 * Allows sending of automatically localized messages in combination with a created localizer.
 *
 * @since 1.0.0
 */
public final class MessageSender {
    private static final MessageSender DEFAULT_SENDER = new MessageSender(null, "");
    private static final Map<Class<? extends Plugin>, MessageSender> PLUGIN_SENDER = new HashMap<>();
    private final Class<? extends Plugin> ownerPlugin;
    private String prefix;

    private MessageSender(Class<? extends Plugin> ownerPlugin, String prefix) {
        this.ownerPlugin = ownerPlugin;
        this.prefix = prefix;
    }

    /**
     * Creates a new message sender for the plugin
     *
     * @param plugin       plugin to create the message sender for
     * @param prefix       plugin prefix with color code
     * @param messageColor default message color
     * @param errorColor   default error color
     * @return message sender instance or default sender if plugin is null
     */
    @Deprecated
    public static MessageSender create(Class<? extends Plugin> plugin, String prefix, char messageColor, char errorColor) {
        return create(plugin, prefix, new char[]{messageColor}, new char[]{errorColor});
    }

    /**
     * Creates a new message sender for the plugin
     *
     * @param plugin       plugin to create the message sender for
     * @param prefix       plugin prefix with color code
     * @param messageColor default message color
     * @param errorColor   default error color
     * @return message sender instance or default sender if plugin is null
     */
    @Deprecated
    public static MessageSender create(Plugin plugin, String prefix, char messageColor, char errorColor) {
        return create(plugin.getClass(), prefix, new char[]{messageColor}, new char[]{errorColor});
    }

    /**
     * Creates a new message sender for the plugin
     *
     * @param plugin       plugin to create the message sender for
     * @param prefix       plugin prefix with color code
     * @param messageColor default message color
     * @param errorColor   default error color
     * @return message sender instance or default sender if plugin is null
     */
    @Deprecated
    public static MessageSender create(Plugin plugin, String prefix, char[] messageColor, char[] errorColor) {
        return create(plugin.getClass(), prefix, messageColor, errorColor);
    }

    /**
     * Creates a new message sender for the plugin
     *
     * @param plugin       plugin to create the message sender for
     * @param prefix       plugin prefix with color code
     * @param messageColor default message color
     * @param errorColor   default error color
     * @return message sender instance or default sender if plugin is null
     */
    @Deprecated
    public static MessageSender create(Class<? extends Plugin> plugin, String prefix, char[] messageColor, char[] errorColor) {
        return create(plugin, prefix);
    }

    public static MessageSender create(Plugin plugin, String prefix) {
        return create(plugin.getClass(), prefix);
    }

    public static MessageSender create(Class<? extends Plugin> plugin, String prefix) {
        if (plugin == null) return DEFAULT_SENDER;

        return PLUGIN_SENDER.compute(plugin,
                (k, v) -> v == null
                        ? new MessageSender(plugin, prefix.trim() + " ")
                        : v.update(prefix));
    }

    /**
     * Get the message sender created for this plugin.
     *
     * @param plugin plugin
     * @return message sender of plugin or default sender if plugin is null
     */
    public static MessageSender getPluginMessageSender(@Nullable Plugin plugin) {
        if (plugin == null) return DEFAULT_SENDER;
        return getPluginMessageSender(plugin.getClass());
    }

    /**
     * Get the message sender created for this plugin.
     *
     * @param plugin plugin
     * @return message sender of plugin or default sender if plugin is null
     */
    public static MessageSender getPluginMessageSender(@Nullable Class<? extends Plugin> plugin) {
        return plugin == null ? DEFAULT_SENDER
                : PLUGIN_SENDER.getOrDefault(plugin, DEFAULT_SENDER);
    }

    public static MessageSender getDefaultSender() {
        return DEFAULT_SENDER;
    }

    private MessageSender update(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Send a message to a sender
     *
     * @param sender  receiver of the message
     * @param message message with optinal color codes
     */
    public void sendMessage(CommandSender sender, String message) {
        send(MessageChannel.CHAT, MessageType.NORMAL, sender, message);
    }

    /**
     * Sends a error to a sender
     *
     * @param sender  receiver of the message
     * @param message message with optinal color codes
     */
    public void sendError(CommandSender sender, String message) {
        send(MessageChannel.CHAT, MessageType.ERROR, sender, message);
    }

    /**
     * Send a message to a sender
     * <p>
     * The message will be localized.
     * <p>
     * The message can be a simple locale code in the format "code" or "code.code....".
     * <p>
     * If multiple code should be used every code musst be surrounded by a {@code $} mark. Example {@code "$code.code$
     * and $code.more.code$}. You can write what you want between locale codes.
     *
     * @param sender       receiver of the message
     * @param message      message with optinal color codes
     * @param replacements replacements to apply on the message
     */
    public void sendLocalizedMessage(CommandSender sender, String message, Replacement... replacements) {
        sendLocalized(MessageChannel.CHAT, MessageType.NORMAL, sender, message, replacements);
    }

    /**
     * Sends a error to a sender
     * <p>
     * The message will be localized.
     * <p>
     * The message can be a simple locale code in the format "code" or "code.code....".
     * <p>
     * If multiple code should be used every code musst be surrounded by a {@code $} mark. Example {@code "$code.code$
     * and $code.more.code$}. You can write what you want between locale codes.
     *
     * @param sender       receiver of the message
     * @param message      message with optinal color codes
     * @param replacements replacements to apply on the message
     */
    public void sendLocalizedError(CommandSender sender, String message, Replacement... replacements) {
        sendLocalized(MessageChannel.CHAT, MessageType.ERROR, sender, message, replacements);
    }

    /**
     * @deprecated flagged for removal. Use {@link #send(MessageChannel, MessageType, CommandSender, String, ChannelData)} instead.
     */
    @Deprecated
    public void sendTitle(Player player, String defaultColor, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        send(MessageChannel.TITLE, () -> defaultColor, player, title, TitleData.forFadeAndTime(fadeIn, stay, fadeOut, subtitle));
    }

    /**
     * @deprecated flagged for removal. Use {@link #send(MessageChannel, MessageType, CommandSender, String, ChannelData)} instead.
     */
    @Deprecated
    public void sendTitle(Player player, String defaultColor, String title, String subtitle) {
        send(MessageChannel.TITLE, () -> defaultColor, player, title, TitleData.forOtherLine(subtitle));
    }

    /**
     * Send a title to a player
     *
     * @param player   player to send
     * @param title    title to send
     * @param subtitle subtitle to send
     * @param fadeIn   fade in time of title
     * @param stay     stay time of title
     * @param fadeOut  fade out time of title
     * @deprecated flagged for removal. Use {@link #send(MessageChannel, MessageType, CommandSender, String, ChannelData)} instead.
     */
    @Deprecated
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        send(MessageChannel.TITLE, MessageType.NORMAL, player, title, TitleData.forFadeAndTime(fadeIn, stay, fadeOut, subtitle));
    }

    /**
     * Send a title to a player
     *
     * @param player   player to send
     * @param title    title to send
     * @param subtitle subtitle to send
     * @deprecated flagged for removal. Use {@link #send(MessageChannel, MessageType, CommandSender, String, ChannelData)} instead.
     */
    @Deprecated
    public void sendTitle(Player player, String title, String subtitle) {
        send(MessageChannel.TITLE, MessageType.NORMAL, player, title, TitleData.forOtherLine(subtitle));
    }

    /**
     * Send a localized title to a player
     *
     * @param player       player to send
     * @param defaultColor default color of message
     * @param title        title to send
     * @param subtitle     subtitle to send
     * @param fadeIn       fade in time of title
     * @param stay         stay time of title
     * @param fadeOut      fade out time of title
     * @param replacements replacements for the localized message
     * @deprecated flagged for removal. Use {@link #sendLocalized(MessageChannel, MessageType, CommandSender, String, ChannelData, Replacement...)} instead.
     */
    @Deprecated
    public void sendLocalizedTitle(Player player, String defaultColor, String title, String subtitle, int fadeIn, int stay, int fadeOut, Replacement... replacements) {
        sendLocalized(MessageChannel.TITLE, () -> defaultColor, player, title, TitleData.forFadeAndTime(fadeIn, stay, fadeOut, subtitle), replacements);
    }

    /**
     * Send a localized title to a player
     *
     * @param player       player to send
     * @param title        title to send
     * @param subtitle     subtitle to send
     * @param fadeIn       fade in time of title
     * @param stay         stay time of title
     * @param fadeOut      fade out time of title
     * @param replacements replacements for the localized message
     * @deprecated flagged for removal. Use {@link #sendLocalized(MessageChannel, MessageType, CommandSender, String, ChannelData, Replacement...)} instead.
     */
    @Deprecated
    public void sendLocalizedTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut, Replacement... replacements) {
        sendLocalized(MessageChannel.TITLE, MessageType.NORMAL, player, title, TitleData.forFadeAndTime(fadeIn, stay, fadeOut, subtitle), replacements);
    }

    /**
     * Send a localized title to a player
     *
     * @param player       player to send
     * @param defaultColor default color of message
     * @param title        title to send
     * @param subtitle     subtitle to send
     * @param replacements replacements for the localized message
     * @deprecated flagged for removal. Use {@link #sendLocalized(MessageChannel, MessageType, CommandSender, String, ChannelData, Replacement...)} instead.
     */
    @Deprecated
    public void sendLocalizedTitle(Player player, String defaultColor, String title, String subtitle, Replacement... replacements) {
        sendLocalized(MessageChannel.TITLE, () -> defaultColor, player, title, TitleData.forOtherLine(subtitle), replacements);
    }

    /**
     * Send a localized title to a player
     *
     * @param player       player to send
     * @param title        title to send
     * @param subtitle     subtitle to send
     * @param replacements replacements for the localized message
     * @deprecated flagged for removal. Use {@link #sendLocalized(MessageChannel, MessageType, CommandSender, String, ChannelData, Replacement...)} instead.
     */
    @Deprecated
    public void sendLocalizedTitle(Player player, String title, String subtitle, Replacement... replacements) {
        sendLocalized(MessageChannel.TITLE, MessageType.NORMAL, player, title, TitleData.forOtherLine(subtitle), replacements);
    }

    /**
     * Send a localized action bar to a player
     *
     * @param player       player to send
     * @param message      message to send
     * @param replacements replacements for the localized message
     * @deprecated flagged for removal. Use {@link #sendLocalized(MessageChannel, MessageType, CommandSender, String, Replacement...)} instead.
     */
    @Deprecated
    public void sendLocalizedActionBar(Player player, String message, Replacement... replacements) {
        sendLocalized(MessageChannel.ACTION_BAR, MessageType.NORMAL, player, message, replacements);
    }

    /**
     * Send a message to a player Action bar.
     *
     * @param player  player to send
     * @param message message to send
     * @deprecated flagged for removal. Use {@link #sendLocalized(MessageChannel, MessageType, CommandSender, String, Replacement...)} instead.
     */
    @Deprecated
    public void sendActionBar(Player player, String message) {
        send(MessageChannel.ACTION_BAR, MessageType.NORMAL, player, message);
    }

    private ILocalizer loc() {
        return ILocalizer.getPluginLocalizer(ownerPlugin);
    }

    /**
     * Send a localized message via a channel.
     *
     * @param channel      channel which should be used
     * @param type         type of message
     * @param sender       target of message
     * @param message      message locale codes
     * @param replacements replacements for messages in locale codes
     * @param <T>          type of channel data
     * @since 1.2.1
     */
    public <T extends ChannelData> void sendLocalized(MessageChannel<T> channel, MessageType type, CommandSender sender, String message, Replacement... replacements) {
        sendLocalized(channel, type, sender, message, null, replacements);
    }

    /**
     * Send a localized message via a channel.
     *
     * @param channel      channel which should be used
     * @param type         type of message
     * @param sender       target of message
     * @param message      message locale codes
     * @param replacements replacements for messages in locale codes
     * @param data         additional data for channel
     * @param <T>          channel data type
     * @since 1.3.0
     */
    public <T extends ChannelData> void sendLocalized(MessageChannel<T> channel, MessageType type, CommandSender sender, String message, @Nullable T data, Replacement... replacements) {
        if (data != null) {
            data.localized(loc(), replacements);
        }
        send(channel, type, sender, loc().localize(message, replacements), data);
    }

    /**
     * Sends a message via a channel
     *
     * @param channel channel which should be used
     * @param type    type of message
     * @param target  target of message
     * @param message message locale codes
     * @param <T>     type of channel data
     * @since 1.2.1
     */
    public <T extends ChannelData> void send(MessageChannel<T> channel, MessageType type, CommandSender target, String message) {
        send(channel, type, target, message, null);
    }

    /**
     * Sends a message via a channel
     *
     * @param channel channel which should be used
     * @param type    type of message
     * @param target  target of message
     * @param message message locale codes
     * @param data    additional data for channel
     * @param <T>     type of data
     * @since 1.3.0
     */
    public <T extends ChannelData> void send(MessageChannel<T> channel, MessageType type, CommandSender target, String message, @Nullable T data) {
        String coloredMessage = type.forceColor(message);
        coloredMessage = channel.addPrefix(coloredMessage, prefix);
        if (data != null) {
            data.formatText(type, channel, prefix);
        }
        channel.sendMessage(coloredMessage, this, target == null ? Bukkit.getConsoleSender() : target, data);
    }

    public boolean isDefault() {
        return ownerPlugin == null;
    }

    public String getPrefix() {
        return prefix;
    }
}
