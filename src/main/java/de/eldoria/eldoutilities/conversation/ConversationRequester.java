package de.eldoria.eldoutilities.conversation;

import de.eldoria.eldoutilities.core.EldoUtilities;
import de.eldoria.eldoutilities.messages.MessageType;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Class to handle Conversations.
 */
public class ConversationRequester implements ConversationAbandonedListener, ConversationCanceller {

    private final Plugin plugin;
    private final Map<Player, Long> sessions = new HashMap<>();

    private ConversationRequester(Plugin plugin) {
        this.plugin = plugin;
    }

    public static ConversationRequester start(Plugin plugin) {
        return new ConversationRequester(plugin);
    }

    private static Prompt getSimplePromt(String text, Predicate<String> validation, Consumer<String> callback) {
        return new Prompt() {
            @Override
            public @NotNull String getPromptText(@NotNull ConversationContext context) {
                return text;
            }

            @Override
            public boolean blocksForInput(@NotNull ConversationContext context) {
                return true;
            }

            @Override
            public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
                if (validation.test(input)) {
                    EldoUtilities.getDelayedActions().schedule(() -> callback.accept(input), 0);
                    return null;
                }
                return this;
            }
        };
    }

    public void requestInput(Player player, String text, Predicate<String> validation, int timeout, Consumer<String> callback) {
        Map<Object, Object> data = new HashMap<>();
        long sessionId = System.currentTimeMillis();
        data.put("id", sessionId);
        sessions.put(player, sessionId);
        EldoConversation conversation = EldoConversation.builder(plugin, player,
                getSimplePromt(text, validation, callback))
                .ofType(MessageType.NORMAL)
                .withInitalValues(data)
                .build();
        player.beginConversation(conversation);

        conversation.addConversationCanceller(this);
        conversation.addConversationAbandonedListener(this);

        if (timeout > 0) {
            EldoUtilities.getDelayedActions().schedule(() -> {
                if (sessions.containsKey(player)) {
                    if (sessions.get(player) == sessionId) {
                        conversation.abandon(new ConversationAbandonedEvent(conversation, this));
                    }
                }
            }, timeout);
        }
    }

    @Override
    public void conversationAbandoned(@NotNull ConversationAbandonedEvent abandonedEvent) {
        Object id = abandonedEvent.getContext().getSessionData("id");
        if (id != null) {
            Long aLong = sessions.get(abandonedEvent.getContext().getForWhom());
            if (aLong != null && aLong.equals(id)) {
                sessions.remove(abandonedEvent.getContext().getForWhom());
            }
        }
    }

    @Override
    public void setConversation(@NotNull Conversation conversation) {
        // we are the canceller and will use ourself. Nothing to do here.
    }

    @Override
    public boolean cancelBasedOnInput(@NotNull ConversationContext context, @NotNull String input) {
        if ("cancel".equalsIgnoreCase(input)) {
            if (context.getForWhom() instanceof Player) {
                return sessions.containsKey(context.getForWhom());
            }
        }
        return false;
    }

    @Override
    public ConversationRequester clone() {
        return this;
    }
}
