/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages;

import de.eldoria.eldoutilities.localization.ILocalizer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * How a message string becomes the component a player sees.
 */
class MessageSenderTest {
    private Plugin plugin;

    @BeforeEach
    void setUp() {
        plugin = mock(Plugin.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("MessageSenderTest"));
    }

    @AfterEach
    void tearDown() {
        ILocalizer.LOCALIZER.remove(plugin.getClass());
    }

    @Test
    void noPrefixAddsNoLeadingSpace() {
        var sender = new TestSender(plugin, Component.empty());
        assertEquals("Hello", plain(sender.prefixed(sender.render("Hello"))));
    }

    @Test
    void aPrefixIsSeparatedByASpace() {
        var sender = new TestSender(plugin, Component.text("[P]"));
        assertEquals("[P] Hello", plain(sender.prefixed(sender.render("Hello"))));
    }

    @Test
    void dottedTextStaysTextWithoutALocalizer() {
        var sender = new TestSender(plugin, Component.empty());
        assertEquals("example.com", plain(sender.render("example.com")));
        assertEquals("1.5", plain(sender.render("1.5")));
    }

    @Test
    void anExplicitLocaleTagShowsItsKeyWithoutALocalizer() {
        var sender = new TestSender(plugin, Component.empty());
        assertEquals("Key: greeting.hello", plain(sender.render("Key: <i18n:greeting.hello>")));
    }

    @Test
    void aLocaleCodeIsLocalizedWithALocalizer() {
        ILocalizer.LOCALIZER.put(plugin.getClass(), new MapLocalizer(Map.of("greeting.hello", "Hallo <name>")));
        var sender = new TestSender(plugin, Component.empty());
        assertEquals("Hallo Lilly", plain(sender.render("greeting.hello", Replacement.unparsed("name", "Lilly"))));
        assertEquals("example.com", plain(sender.render("example.com")));
    }

    @Test
    void anUnparsedReplacementCannotAddTags() {
        var sender = new TestSender(plugin, Component.empty());
        var component = sender.render("Home: <name>", Replacement.unparsed("name", "<click:run_command:'/op me'>evil"));
        assertEquals("Home: <click:run_command:'/op me'>evil", plain(component));
        assertFalse(hasClickEvent(component));
    }

    @Test
    void anAmpersandInsideAWordSurvives() {
        var sender = new TestSender(plugin, Component.empty());
        assertEquals("rock&roll", plain(sender.render("rock&roll")));
    }

    private static boolean hasClickEvent(Component component) {
        return component.clickEvent() != null
                || component.children().stream().anyMatch(MessageSenderTest::hasClickEvent);
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static final class MapLocalizer extends ILocalizer.DummyLocalizer {
        private final Map<String, String> messages;

        private MapLocalizer(Map<String, String> messages) {
            this.messages = messages;
        }

        @Override
        public String localize(CommandSender sender, String message) {
            return messages.getOrDefault(message, message);
        }
    }

    private static final class TestSender extends MessageSender {
        private TestSender(Plugin plugin, Component prefix) {
            super(
                    plugin,
                    MiniMessage.builder().tags(StandardTags.defaults()).build(),
                    TagResolver.resolver(
                            StandardTags.defaults(),
                            TagResolver.builder()
                                    .tag("default", Tag.styling(NamedTextColor.GREEN))
                                    .build()),
                    TagResolver.resolver(
                            StandardTags.defaults(),
                            TagResolver.builder()
                                    .tag("default", Tag.styling(NamedTextColor.RED))
                                    .build()),
                    prefix);
        }

        private Component render(String message, TagResolver... placeholder) {
            return serialize(null, message, messageTagResolver(), placeholder);
        }

        private Component prefixed(Component component) {
            return applyPrefix(component);
        }

        @Override
        public void sendMessage(CommandSender sender, Component component) {}

        @Override
        public void broadcast(String message) {}

        @Override
        public void sendTitle(Player player, Title title) {}

        @Override
        public void sendActionBar(Player player, String message, TagResolver... placeholder) {}

        @Override
        public void sendErrorActionBar(Player player, String message, TagResolver... placeholder) {}

        @Override
        public void sendBossBar(Player player, BossBar bossBar) {}

        @Override
        public BossBar sendBossBar(
                Player player,
                String message,
                float progress,
                BossBar.Color color,
                BossBar.Overlay overlay,
                Set<BossBar.Flag> flags) {
            return null;
        }

        @Override
        public void hideBossBar(Player player, BossBar bossBar) {}

        @Override
        public Audience asAudience(Player player) {
            return null;
        }
    }
}
