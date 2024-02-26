/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages;

import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.localization.IMessageComposer;
import de.eldoria.eldoutilities.messages.conversion.MiniMessageConversion;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
public abstract class MessageSender {
    private static final Map<Class<? extends Plugin>, MessageSender> PLUGIN_SENDER = new HashMap<>();
    @Nullable
    private final Class<? extends Plugin> ownerPlugin;
    private final Plugin plugin;
    private MiniMessage miniMessage;
    private TagResolver messageTagResolver;
    private TagResolver errorTagResolver;
    private Component prefix;

    public MessageSender(Plugin plugin, MiniMessage miniMessage, TagResolver messageTagResolver, TagResolver errorTagResolver, Component prefix) {
        this.ownerPlugin = plugin.getClass();
        this.plugin = plugin;
        this.miniMessage = miniMessage;
        this.messageTagResolver = messageTagResolver;
        this.errorTagResolver = errorTagResolver;
        this.prefix = prefix;
    }

    public static void register(MessageSender messageSender) {
        if (messageSender.ownerPlugin == null) return;
        PLUGIN_SENDER.put(messageSender.ownerPlugin, messageSender);
    }

    /**
     * Get the message sender created for this plugin.
     *
     * @param plugin plugin
     * @return message sender of plugin or default sender if plugin is null
     */
    public static MessageSender getPluginMessageSender(@NotNull Plugin plugin) {
        if (plugin == null) throw new IllegalArgumentException("Plugin can not be null");
        return getPluginMessageSender(plugin.getClass());
    }

    public static MessageSenderBuilder builder(Plugin plugin) {
        return new MessageSenderBuilder(plugin);
    }

    /**
     * Get the message sender created for this plugin.
     *
     * @param plugin plugin
     * @return message sender of plugin or default sender if plugin is null
     */
    public static MessageSender getPluginMessageSender(@NotNull Class<? extends Plugin> plugin) {
        if (!PLUGIN_SENDER.containsKey(plugin)) {
            throw new IllegalStateException("No message sender was created for " + plugin.getName());
        }
        if (plugin == null) throw new IllegalArgumentException("Plugin can not be null");
        return PLUGIN_SENDER.get(plugin);
    }

    private MessageSender update(MiniMessage miniMessage, TagResolver messageTagResolver, TagResolver errorTagResolver, Component prefix) {
        this.miniMessage = miniMessage;
        this.messageTagResolver = messageTagResolver;
        this.errorTagResolver = errorTagResolver;
        this.prefix = prefix;
        return this;
    }

    /**
     * Send a message to a sender
     * <p>
     * The message will be localized if a localizer is available and a locale code is detected.
     * <p>
     * The message can be a simple locale code in the format "code" or "code.code....".
     * <p>
     * If multiple code should be used every code musst be surrounded by a {@code $} mark. Example {@code "$code.code$
     * and $code.more.code$}. You can write what you want between locale codes.
     *
     * @param sender  receiver of the message
     * @param message message with optional color codes
     */
    public void sendMessage(CommandSender sender, String message, TagResolver... placeholder) {
        sendMessage(sender, serialize(message, messageTagResolver, placeholder));
    }


    /**
     * Send a message to a sender
     * <p>
     * The message will be localized if a localizer is available and a locale code is detected.
     * <p>
     * The message can be a simple locale code in the format "code" or "code.code....".
     * <p>
     * If multiple code should be used every code musst be surrounded by a {@code $} mark. Example {@code "$code.code$
     * and $code.more.code$}. You can write what you want between locale codes.
     *
     * @param sender   receiver of the message
     * @param composer message composer
     */
    public void sendMessage(CommandSender sender, IMessageComposer composer) {
        sendMessage(sender, serialize(composer.build(), messageTagResolver,
                composer.replacements().toArray(new TagResolver[0])));
    }

    /**
     * Sends an error to a sender
     * <p>
     * The message will be localized if a localizer is available and a locale code is detected.
     * <p>
     * The message can be a simple locale code in the format "code" or "code.code....".
     * <p>
     * If multiple code should be used every code musst be surrounded by a {@code $} mark. Example {@code "$code.code$
     * and $code.more.code$}. You can write what you want between locale codes.
     *
     * @param sender  receiver of the message
     * @param message message with optinal color codes
     */
    public void sendError(CommandSender sender, String message, TagResolver... placeholder) {
        sendMessage(sender, serialize(message, errorTagResolver, placeholder));
    }

    /**
     * Sends an error to a sender
     * <p>
     * The message will be localized if a localizer is available and a locale code is detected.
     * <p>
     * The message can be a simple locale code in the format "code" or "code.code....".
     * <p>
     * If multiple code should be used every code musst be surrounded by a {@code $} mark. Example {@code "$code.code$
     * and $code.more.code$}. You can write what you want between locale codes.
     *
     * @param sender   receiver of the message
     * @param composer message composer
     */
    public void sendError(CommandSender sender, IMessageComposer composer) {
        sendMessage(sender, serialize(composer.build(), errorTagResolver,
                composer.replacements().toArray(new TagResolver[0])));
    }

    public abstract void sendMessage(CommandSender sender, Component component);

    public abstract void broadcast(String message);

    /**
     * Send a localized title to a player
     *
     * @param player player to send
     * @param title  title to send
     */
    public void sendTitle(Player player, String title, String subtitle, Title.Times times, TagResolver... placeholder) {
        sendTitle(player, Title.title(serialize(title, messageTagResolver, placeholder), serialize(subtitle, messageTagResolver, placeholder), times));
    }

    /**
     * Send a title to a player
     *
     * @param player player to send
     * @param title  title to send
     */
    public abstract void sendTitle(Player player, Title title);

    /**
     * Send a localized action bar to a player
     *
     * @param player  player to send
     * @param message message to send
     */
    public abstract void sendActionBar(Player player, String message, TagResolver... placeholder);

    /**
     * Send a localized action bar to a player
     *
     * @param player  player to send
     * @param message message to send
     */
    public abstract void sendErrorActionBar(Player player, String message, TagResolver... placeholder);

    public abstract void sendBossBar(Player player, BossBar bossBar);

    public abstract BossBar sendBossBar(Player player, String message, float progress, BossBar.Color color, BossBar.Overlay overlay, Set<BossBar.Flag> flags);

    public abstract void hideBossBar(Player player, BossBar bossBar);

    private ILocalizer loc() {
        return ILocalizer.getPluginLocalizer(ownerPlugin);
    }

    protected Component serialize(String message, TagResolver resolver, TagResolver... placeholder) {
        var converted = MiniMessageConversion.convertLegacyColorCodes(message);
        if (!converted.equals(message)) {
            plugin.getLogger().warning("Found legacy color codes in message.");
            plugin.getLogger().warning(message);
            message = converted;
        }
        if (ILocalizer.isLocaleCode(message)) {
            message = ILocalizer.escape(message);
        }
        message = "<default>" + message;
        var finalResolver = new TagResolver[]{resolver};
        if (placeholder.length > 0) {
            var tags = Arrays.copyOf(placeholder, placeholder.length + 1);
            tags[tags.length - 1] = resolver;
            finalResolver = tags;
        }
        return resolveTags(message, finalResolver);
    }

    private Component resolveTags(String message, TagResolver... resolver) {
        var component = miniMessage.deserialize(message, resolver);
        var newMessage = miniMessage.serialize(component);
        if (newMessage.equals(message)) {
            return component;
        }
        return resolveTags(newMessage, resolver);
    }

    public Component prefix() {
        return prefix;
    }

    protected Component applyPrefix(Component component) {
        return prefix.appendSpace().append(component);
    }

    public String translatePlain(String message, TagResolver... replacements) {
        return PlainTextComponentSerializer.plainText().serialize(serialize(message, messageTagResolver, replacements));
    }

    public Component serializeMessage(String message, TagResolver... placeholder) {
        return serialize(message, messageTagResolver, placeholder);
    }

    public Component serializeError(String message, TagResolver... placeholder) {
        return serialize(message, errorTagResolver, placeholder);
    }

    public MiniMessage miniMessage() {
        return miniMessage;
    }

    public boolean isAnonymous() {
        return ownerPlugin == null;
    }

    protected TagResolver messageTagResolver() {
        return messageTagResolver;
    }

    protected TagResolver errorTagResolver() {
        return errorTagResolver;
    }

    protected Plugin plugin() {
        return plugin;
    }

    public abstract Audience asAudience(Player player);
}
