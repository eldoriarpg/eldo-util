/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class MessageSenderBuilder {
    private final MiniMessage.Builder miniMessage = MiniMessage.builder();
    private final TagResolver.Builder messageTagResolver = TagResolver.builder()
            .tag("default", Tag.styling(NamedTextColor.GREEN));
    private final TagResolver.Builder errorTagResolver = TagResolver.builder()
            .tag("default", Tag.styling(NamedTextColor.RED));

    private final Plugin plugin;
    private Component prefix = Component.empty();

    public MessageSenderBuilder(Plugin plugin) {
        this.plugin = plugin;
    }

    public MessageSenderBuilder prefix(Component prefix) {
        this.prefix = prefix;
        messageTagResolver.tag("prefix", Tag.selfClosingInserting(this.prefix));
        errorTagResolver.tag("prefix", Tag.selfClosingInserting(this.prefix));
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
        consumer.accept(errorTagResolver);
        consumer.accept(messageTagResolver);
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

    public MessageSenderBuilder miniMessage(Consumer<MiniMessage.Builder> consumer) {
        consumer.accept(miniMessage);
        return this;
    }

    public MessageSender build() {
        return new MessageSender(plugin, miniMessage.build(), messageTagResolver.build(), errorTagResolver.build(), prefix);
    }
}
