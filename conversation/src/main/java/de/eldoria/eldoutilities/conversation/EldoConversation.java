/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.conversation;

import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import net.kyori.adventure.text.Component;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.Prompt;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for {@link Conversation}
 */
public class EldoConversation extends Conversation {
    private final ILocalizer localizer;
    private final MessageSender sender;
    private final String userPrefix;
    private final Component pluginPrefix;

    public EldoConversation(@Nullable Plugin plugin, @NotNull Conversable forWhom, @Nullable Prompt firstPrompt,
                            @NotNull Map<Object, Object> initialSessionData, Component pluginPrefix, String userPrefix) {
        super(plugin, forWhom, firstPrompt, initialSessionData);
        localizer = ILocalizer.getPluginLocalizer(plugin);
        sender = MessageSender.getPluginMessageSender(plugin);
        this.pluginPrefix = pluginPrefix == null ? sender.prefix() : pluginPrefix;
        this.userPrefix = userPrefix == null ? "" : userPrefix;
    }

    public static Builder builder(Plugin plugin, Conversable forWhom, Prompt firstPrompt) {
        return new Builder(plugin, forWhom, firstPrompt);
    }

    /**
     * Adds a {@link ConversationCanceller} to the cancellers collection.
     *
     * @param canceller The {@link ConversationCanceller} to add.
     */
    void addConversationCanceller(@NotNull ConversationCanceller canceller) {
        canceller.setConversation(this);
        this.cancellers.add(canceller);
    }

    /**
     * Passes player input into the current prompt. The next prompt (as
     * determined by the current prompt) is then displayed to the user.
     *
     * @param input The user's chat text.
     */
    @Override
    public void acceptInput(@NotNull String input) {
        if (currentPrompt != null) {

            // Echo the user's input
            if (localEchoEnabled) {
                context.getForWhom().sendRawMessage(userPrefix + input);
            }

            // Test for conversation abandonment based on input
            for (var canceller : cancellers) {
                if (canceller.cancelBasedOnInput(context, input)) {
                    abandon(new ConversationAbandonedEvent(this, canceller));
                    return;
                }
            }

            // Not abandoned, output the next prompt
            currentPrompt = currentPrompt.acceptInput(context, input);
            outputNextPrompt();
        }
    }

    /**
     * Displays the next user prompt and abandons the conversation if the next
     * prompt is null.
     */
    @Override
    public void outputNextPrompt() {
        if (currentPrompt == null) {
            abandon(new ConversationAbandonedEvent(this));
        } else {
            var promptText = currentPrompt.getPromptText(context);
            promptText = localizer.localize(promptText);
            context.getForWhom().sendRawMessage(pluginPrefix + promptText);
            if (!currentPrompt.blocksForInput(context)) {
                currentPrompt = currentPrompt.acceptInput(context, null);
                outputNextPrompt();
            }
        }
    }

    /**
     * Builder for a {@link EldoConversation}
     */
    public static class Builder {
        private final Plugin plugin;
        private final Conversable forWhom;
        private final Prompt firstPrompt;
        private Map<Object, Object> initialValues = new HashMap<>();
        private Component pluginPrefix;
        private String userPrefix = "";

        public Builder(Plugin plugin, Conversable forWhom, Prompt firstPrompt) {
            this.plugin = plugin;
            this.forWhom = forWhom;
            this.firstPrompt = firstPrompt;
        }

        public Builder withDefaultPluginPrefix() {
            this.pluginPrefix = null;
            return this;
        }

        public Builder withPluginPrefix(Component pluginPrefix) {
            this.pluginPrefix = pluginPrefix;
            return this;
        }

        public Builder withDefaultUserPrefix() {
            this.userPrefix = "";
            return this;
        }

        public Builder withUserPrefix(String userPrefix) {
            this.userPrefix = userPrefix;
            return this;
        }

        public Builder withInitalValues(Map<Object, Object> initalValues) {
            this.initialValues = initalValues;
            return this;
        }

        public Builder addIniitialValue(Object key, Object value) {
            this.initialValues.put(key, value);
            return this;
        }

        public EldoConversation build() {
            return new EldoConversation(plugin, forWhom, firstPrompt, initialValues, pluginPrefix, userPrefix);
        }
    }
}
