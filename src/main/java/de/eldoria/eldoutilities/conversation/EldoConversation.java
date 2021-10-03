package de.eldoria.eldoutilities.conversation;

import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.messages.MessageType;
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
    private final MessageType messageType;
    private final String userPrefix;
    private final String pluginPrefix;

    public EldoConversation(@Nullable Plugin plugin, @NotNull Conversable forWhom, @Nullable Prompt firstPrompt,
                            @NotNull Map<Object, Object> initialSessionData, String pluginPrefix, String userPrefix, MessageType messageType) {
        super(plugin, forWhom, firstPrompt, initialSessionData);
        this.messageType = messageType;
        localizer = ILocalizer.getPluginLocalizer(plugin);
        sender = MessageSender.getPluginMessageSender(plugin);
        this.pluginPrefix = pluginPrefix == null ? sender.getPrefix() : pluginPrefix;
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
            for (ConversationCanceller canceller : cancellers) {
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
            String promptText = currentPrompt.getPromptText(context);
            promptText = localizer.localize(promptText);
            promptText = messageType.forceColor(promptText);
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
        private String pluginPrefix;
        private String userPrefix = "";
        private MessageType type = MessageType.BLANK;

        public Builder(Plugin plugin, Conversable forWhom, Prompt firstPrompt) {
            this.plugin = plugin;
            this.forWhom = forWhom;
            this.firstPrompt = firstPrompt;
        }

        public Builder withDefaultPluginPrefix() {
            this.pluginPrefix = null;
            return this;
        }

        public Builder withPluginPrefix(String pluginPrefix) {
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

        public Builder ofType(MessageType type) {
            this.type = type;
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
            return new EldoConversation(plugin, forWhom, firstPrompt, initialValues, pluginPrefix, userPrefix, type);
        }
    }
}
