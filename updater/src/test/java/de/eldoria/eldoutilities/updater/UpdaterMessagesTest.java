/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * How the updater's texts are picked and rendered, with and without a localizer.
 */
class UpdaterMessagesTest {
    private Plugin plugin;
    private MessageSender sender;

    @BeforeEach
    void setUp() {
        plugin = mock(Plugin.class);
        var description = mock(PluginDescriptionFile.class);
        when(plugin.getName()).thenReturn("TestPlugin");
        when(plugin.getLogger()).thenReturn(Logger.getLogger("UpdaterMessagesTest"));
        when(plugin.getDescription()).thenReturn(description);
        when(description.getVersion()).thenReturn("1.0.0");
        sender = MessageSender.builder(plugin).register();
    }

    @AfterEach
    void tearDown() {
        ILocalizer.LOCALIZER.remove(plugin.getClass());
        MessageSender.unregister(plugin);
    }

    @Test
    void withoutALocalizerTheEnglishDefaultIsSent() {
        var data = new TestData(plugin, UpdaterMessages.NOTICE);
        var response = new TestResponse("2.0.0");

        var text = sender.translatePlain(data.updateMessage(response), data.placeholders(response));

        assertEquals("""
                New version of TestPlugin available.
                Newest version: 2.0.0! Current version: 1.0.0!
                Download the new version here: https://example.com""", text);
    }

    @Test
    void withALocalizerItsTranslationIsSent() {
        var localizer =
                new MapLocalizer(Map.of(UpdaterMessages.NOTICE, "Neue Version <new_version> von <plugin_name>"));
        ILocalizer.LOCALIZER.put(plugin.getClass(), localizer);
        var data = new TestData(plugin, UpdaterMessages.NOTICE);
        var response = new TestResponse("2.0.0");

        var text = sender.translatePlain(data.updateMessage(response), data.placeholders(response));

        assertEquals("Neue Version 2.0.0 von TestPlugin", text);
    }

    @Test
    void theCodesAreAddedToTheLocalizer() {
        var localizer = new MapLocalizer(Map.of());
        ILocalizer.LOCALIZER.put(plugin.getClass(), localizer);

        UpdaterMessages.register(plugin);

        assertEquals(UpdaterMessages.defaults(), localizer.added);
    }

    @Test
    void aMessageWithBracePlaceholdersStillWorks() {
        var data = new TestData(plugin, "Update {plugin_name} to {new_version}");
        var response = new TestResponse("2.0.0");

        var text = sender.translatePlain(data.updateMessage(response), data.placeholders(response));

        assertEquals("Update TestPlugin to 2.0.0", text);
    }

    @Test
    void anInjectedMessageSenderIsUsed() {
        var injected = mock(MessageSender.class);
        var data = new UpdateData<TestResponse>(
                plugin, "test.notify", true, false, "https://example.com", UpdaterMessages.NOTICE, injected) {};

        assertSame(injected, data.messageSender());
    }

    @Test
    void withoutAnInjectedSenderThePluginsSenderIsUsed() {
        assertSame(sender, new TestData(plugin, UpdaterMessages.NOTICE).messageSender());
    }

    @Test
    void otherTextsAreLeftAlone() {
        assertEquals("plugin.own.key", UpdaterMessages.resolve(plugin, "plugin.own.key"));
    }

    private record TestResponse(String latestVersion) implements UpdateResponse {
        @Override
        public boolean isOutdated() {
            return true;
        }
    }

    private static final class TestData extends UpdateData<TestResponse> {
        private TestData(Plugin plugin, String message) {
            super(plugin, "test.notify", true, false, "https://example.com", message);
        }
    }

    private static final class MapLocalizer extends ILocalizer.DummyLocalizer {
        private final Map<String, String> messages;
        private final Map<String, String> added = new HashMap<>();

        private MapLocalizer(Map<String, String> messages) {
            this.messages = messages;
        }

        @Override
        public void addLocaleCodes(Map<String, String> runtimeLocaleCodes) {
            added.putAll(runtimeLocaleCodes);
        }

        @Override
        public String localize(CommandSender sender, String message) {
            return messages.getOrDefault(message, message);
        }
    }
}
