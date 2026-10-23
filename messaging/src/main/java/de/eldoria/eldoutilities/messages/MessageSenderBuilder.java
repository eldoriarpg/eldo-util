/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages;

import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.messages.impl.PaperMessageSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class MessageSenderBuilder {
    private final MiniMessage.Builder miniMessage = MiniMessage.builder();
    private final TagResolver.Builder messageTagResolver =
            TagResolver.builder().tag("default", Tag.styling(NamedTextColor.GREEN));
    private final TagResolver.Builder errorTagResolver =
            TagResolver.builder().tag("default", Tag.styling(NamedTextColor.RED));
    private final TagResolver.Builder defaultTagResolver = TagResolver.builder();

    @NotNull
    private final Plugin plugin;

    private Component prefix = Component.empty();
    private ILocalizer localizer = ILocalizer.DEFAULT;
    private UnaryOperator<String> preProcessor = s -> s;

    public MessageSenderBuilder(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * The localizer for locale codes in messages and in the {@code <i18n>} tag. Without it, the localizer registered
     * for the plugin is used.
     *
     * @param localizer localizer instance
     * @return builder instance
     */
    public MessageSenderBuilder localizer(ILocalizer localizer) {
        this.localizer = localizer;
        return this;
    }

    public MessageSenderBuilder prefix(Component prefix) {
        this.prefix = prefix;
        defaultTagResolver.tag("prefix", Tag.selfClosingInserting(this.prefix));
        return this;
    }

    public MessageSenderBuilder prefix(String prefix) {
        return prefix(MiniMessage.miniMessage().deserialize(prefix));
    }

    public MessageSenderBuilder messageColor(TextColor color) {
        messageTagResolver.tag("default", Tag.styling(color));
        return this;
    }

    public MessageSenderBuilder errorColor(TextColor color) {
        errorTagResolver.tag("default", Tag.styling(color));
        return this;
    }

    public MessageSenderBuilder addTag(Consumer<TagResolver.Builder> consumer) {
        consumer.accept(defaultTagResolver);
        return this;
    }

    public MessageSenderBuilder addMessageTag(Consumer<TagResolver.Builder> consumer) {
        consumer.accept(messageTagResolver);
        return this;
    }

    public MessageSenderBuilder addErrorTag(Consumer<TagResolver.Builder> consumer) {
        consumer.accept(errorTagResolver);
        return this;
    }

    public MessageSenderBuilder strict(boolean strict) {
        miniMessage.strict(strict);
        return this;
    }

    public MessageSenderBuilder debug(@Nullable Consumer<String> debugOutput) {
        miniMessage.debug(debugOutput);
        return this;
    }

    public MessageSenderBuilder postProcessor(@NotNull UnaryOperator<Component> postProcessor) {
        miniMessage.postProcessor(postProcessor);
        return this;
    }

    public MessageSenderBuilder preProcessor(@NotNull UnaryOperator<String> preProcessor) {
        this.preProcessor = preProcessor;
        return this;
    }

    /**
     * Builds and registers the message sender for the provided plugin
     *
     * @return registered message sender
     */
    public MessageSender register() {
        var defaultResolver =
                defaultTagResolver.resolver(StandardTags.defaults()).build();
        var messageSender = new PaperMessageSender(
                plugin,
                miniMessage.tags(defaultResolver).preProcessor(preProcessor).build(),
                TagResolver.resolver(defaultResolver, messageTagResolver.build()),
                TagResolver.resolver(defaultResolver, errorTagResolver.build()),
                prefix,
                localizer);
        MessageSender.register(messageSender);
        return messageSender;
    }
}
