/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.localization;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * How the localizer reads and maintains the locale files in the plugin's data folder.
 */
class LocalizerFilesTest {
    private static final String BUNDLED = "greeting.hello=Hello\nfarewell.bye=Bye\n";

    @TempDir
    Path dataFolder;

    private Path messages;
    private Plugin plugin;
    private final List<LogRecord> warnings = new ArrayList<>();
    private final Logger logger = Logger.getLogger("LocalizerFilesTest");
    private final Handler collector = new Handler() {
        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) warnings.add(record);
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    };

    @BeforeEach
    void setUp() throws IOException {
        messages = Files.createDirectories(dataFolder.resolve("messages"));
        plugin = mock(Plugin.class);
        logger.addHandler(collector);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
        when(plugin.getResource(anyString())).thenAnswer(invocation -> switch ((String) invocation.getArgument(0)) {
            case "messages.properties", "messages_en_US.properties" ->
                new ByteArrayInputStream(BUNDLED.getBytes(StandardCharsets.UTF_8));
            default -> null;
        });
    }

    @AfterEach
    void tearDown() {
        logger.removeHandler(collector);
        ILocalizer.LOCALIZER.remove(plugin.getClass());
    }

    @Test
    void aLocaleFileAddedByTheServerOwnerIsUsed() throws IOException {
        Files.writeString(
                messages.resolve("messages_fr_FR.properties"), "greeting.hello=Bonjour\nfarewell.bye=Au revoir\n");
        var player = mock(Player.class);

        var localizer =
                Localizer.builder(plugin, "en_US").setUserLocale(p -> "fr_fr").build();

        assertEquals("Bonjour", localizer.getValue("greeting.hello", player));
        localizer.setDefaultLocale("fr_FR");
        assertEquals("Bonjour", localizer.getValue("greeting.hello"));
    }

    @Test
    void aMissingKeyIsReportedOnce() {
        var localizer = Localizer.builder(plugin, "en_US").build();
        warnings.clear();

        assertEquals("does.not.exist", localizer.getMessage("does.not.exist"));
        assertEquals("does.not.exist", localizer.getMessage("does.not.exist"));

        assertEquals(1, warnings.size());
    }

    @Test
    void updatingAFileKeepsItAndOnlyAppendsMissingKeys() throws IOException {
        var original = """
                # Translated by a server owner
                greeting.hello = Bonjour \\
                  tout le monde
                """;
        Files.writeString(messages.resolve("messages_fr_FR.properties"), original);

        Localizer.builder(plugin, "en_US").build();

        var updated = Files.readString(messages.resolve("messages_fr_FR.properties"));
        assertTrue(updated.startsWith(original));
        var properties = new Properties();
        properties.load(new java.io.StringReader(updated));
        assertEquals("Bonjour tout le monde", properties.getProperty("greeting.hello"));
        assertEquals("Bye", properties.getProperty("farewell.bye"));
        assertEquals(
                "Invalid arguments.\nSyntax: <gold><syntax><default>",
                properties.getProperty("error.invalidArguments"));
    }

    @Test
    void anUpToDateFileIsLeftAlone() throws IOException {
        Localizer.builder(plugin, "en_US").build();
        var file = messages.resolve("messages_en_US.properties");
        var first = Files.readString(file);

        Localizer.builder(plugin, "en_US").build();

        assertEquals(first, Files.readString(file));
    }

    @Test
    void codesAddedLaterAreFoundAndWrittenToTheFiles() throws IOException {
        var localizer = Localizer.builder(plugin, "en_US").build();

        localizer.addLocaleCodes(java.util.Map.of("late.code", "Late text"));

        assertEquals("Late text", localizer.getValue("late.code"));
        var properties = new Properties();
        properties.load(new java.io.StringReader(Files.readString(messages.resolve("messages_en_US.properties"))));
        assertEquals("Late text", properties.getProperty("late.code"));
    }

    @Test
    void buildLocalizedReplacesLocaleCodes() {
        var localizer = Localizer.builder(plugin, "en_US").build();

        var message = MessageComposer.create()
                .text("Say ")
                .localeCode("greeting.hello")
                .text("!")
                .buildLocalized(localizer);

        assertEquals("Say Hello!", message);
    }
}
