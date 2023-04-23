/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages;

import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.localization.IMessageComposer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
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
public final class MessageSender {
    private static final Map<Class<? extends Plugin>, MessageSender> PLUGIN_SENDER = new HashMap<>();
    @Nullable
    private final Class<? extends Plugin> ownerPlugin;
    private final BukkitAudiences audiences;
    private MiniMessage miniMessage;
    private TagResolver messageTagResolver;
    private TagResolver errorTagResolver;
    private Component prefix;

    MessageSender(Plugin plugin, MiniMessage miniMessage, TagResolver messageTagResolver, TagResolver errorTagResolver, Component prefix) {
        this.ownerPlugin = plugin.getClass();
        this.miniMessage = miniMessage;
        this.messageTagResolver = messageTagResolver;
        this.errorTagResolver = errorTagResolver;
        this.prefix = prefix;
        audiences = BukkitAudiences.create(plugin);
    }

    public static void register(MessageSender messageSender) {
        PLUGIN_SENDER.put(messageSender.ownerPlugin, messageSender);
    }

    static MessageSender create(@NotNull Plugin plugin, MiniMessage miniMessage, TagResolver messageTagResolver, TagResolver errorTagResolver, Component prefix) {
        if (plugin == null) throw new IllegalArgumentException("Plugin can not be null");

        return PLUGIN_SENDER.compute(plugin.getClass(),
                (k, v) -> v == null
                        ? new MessageSender(plugin, miniMessage, messageTagResolver, errorTagResolver, prefix)
                        : v.update(miniMessage, messageTagResolver, errorTagResolver, prefix));
    }

    public static MessageSender anonymous() {
        var resolver = TagResolver.builder().resolver(Replacement.create("default", "")).build();
        return new MessageSender(null, MiniMessage.miniMessage(), resolver, resolver, Component.empty());
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

    public void sendMessage(CommandSender sender, Component component) {
        audiences.sender(sender).sendMessage(applyPrefix(component));
    }

    public void broadcast(String message) {
        audiences.all().sendMessage(serialize(message, messageTagResolver));
    }

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
    public void sendTitle(Player player, Title title) {
        audiences.player(player).showTitle(title);
    }

    /**
     * Send a localized action bar to a player
     *
     * @param player  player to send
     * @param message message to send
     */
    public void sendActionBar(Player player, String message, TagResolver... placeholder) {
        audiences.player(player).sendActionBar(serialize(message, messageTagResolver, placeholder));
    }

    /**
     * Send a localized action bar to a player
     *
     * @param player  player to send
     * @param message message to send
     */
    public void sendErrorActionBar(Player player, String message, TagResolver... placeholder) {
        audiences.player(player).sendActionBar(serialize(message, errorTagResolver, placeholder));
    }

    public void sendBossBar(Player player, BossBar bossBar) {
        audiences.player(player).showBossBar(bossBar);
    }

    public BossBar sendBossBar(Player player, String message, float progress, BossBar.Color color, BossBar.Overlay overlay, Set<BossBar.Flag> flags) {
        var bossBar = BossBar.bossBar(serialize(message, messageTagResolver), progress, color, overlay, flags);
        audiences.player(player).showBossBar(bossBar);
        return bossBar;
    }

    public void hideBossBar(Player player, BossBar bossBar) {
        audiences.player(player).hideBossBar(bossBar);
    }

    private ILocalizer loc() {
        return ILocalizer.getPluginLocalizer(ownerPlugin);
    }

    private Component serialize(String message, TagResolver resolver, TagResolver... placeholder) {
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

    private Component applyPrefix(Component component) {
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
}
