/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages;

import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.messages.impl.PaperMessageSender;
import de.eldoria.eldoutilities.messages.impl.SpigotMessageSender;
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

import static de.eldoria.eldoutilities.utils.ReflectionUtil.isPaper;

public class MessageSenderBuilder {
    private final MiniMessage.Builder miniMessage = MiniMessage.builder();
    private final TagResolver.Builder messageTagResolver = TagResolver.builder()
                                                                      .tag("default", Tag.styling(NamedTextColor.GREEN));
    private final TagResolver.Builder errorTagResolver = TagResolver.builder()
                                                                    .tag("default", Tag.styling(NamedTextColor.RED));
    private final TagResolver.Builder defaultTagResolver = TagResolver.builder();
    private static final String ADVENTURE_PATH = String.join(".","net", "kyori", "adventure", "text");

    @NotNull
    private final Plugin plugin;
    private Component prefix = Component.empty();
    private ILocalizer localizer = ILocalizer.DEFAULT;
    private UnaryOperator<String> preProcessor = s -> s;

    public MessageSenderBuilder(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * The localizer used to serialize messages via the {@code <i8ln>} tag
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
        return prefix(MiniMessage.miniMessage()
                                 .deserialize(prefix));
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

    public MiniMessage.@NotNull Builder strict(boolean strict) {
        return miniMessage.strict(strict);
    }

    public MiniMessage.@NotNull Builder debug(@Nullable Consumer<String> debugOutput) {
        return miniMessage.debug(debugOutput);
    }

    public MiniMessage.@NotNull Builder postProcessor(@NotNull UnaryOperator<Component> postProcessor) {
        return miniMessage.postProcessor(postProcessor);
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
        var defaultResolver = defaultTagResolver
                .resolver(StandardTags.defaults())
                .build();
        MessageSender messageSender;
        plugin.getLogger().info("Determining message sender type");
        boolean paper = isPaper();
        if (!paper) {
            plugin.getLogger().info("Detected non paper based server");
        }
        if (!paper || hasRelocatedAdventure()) {
            plugin.getLogger().info("Using legacy message sender");
            messageSender = new SpigotMessageSender(plugin,
                    miniMessage.tags(defaultResolver)
                               .preProcessor(in -> preProcessor.apply(localizer.localize(in)))
                               .build(),
                    TagResolver.resolver(defaultResolver, messageTagResolver.build()),
                    TagResolver.resolver(defaultResolver, errorTagResolver.build()),
                    prefix);
        } else {
            plugin.getLogger().info("Using Paper Message Sender");
            messageSender = new PaperMessageSender(plugin,
                    miniMessage.tags(defaultResolver)
                               .preProcessor(in -> preProcessor.apply(localizer.localize(in)))
                               .build(),
                    TagResolver.resolver(defaultResolver, messageTagResolver.build()),
                    TagResolver.resolver(defaultResolver, errorTagResolver.build()),
                    prefix);
        }
        MessageSender.register(messageSender);
        return messageSender;
    }

    boolean hasRelocatedAdventure() {
        boolean relocated = !Component.class.getPackageName().startsWith(ADVENTURE_PATH);
        if (relocated) {
            plugin.getLogger().info("Found relocated adventure at %s instead of net.kyori.adventure.text.".formatted(Component.class.getPackageName()));
        }
        return relocated;
    }
}
