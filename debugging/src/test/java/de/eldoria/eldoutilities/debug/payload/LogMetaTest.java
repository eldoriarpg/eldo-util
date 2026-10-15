/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.payload;

import de.eldoria.eldoutilities.debug.DebugSettings;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What a report takes from the server log.
 */
class LogMetaTest {
    @TempDir
    Path folder;
    private final Plugin plugin = mock(Plugin.class);

    @BeforeEach
    void setUp() {
        when(plugin.getDescription()).thenReturn(new PluginDescriptionFile("BloodNight", "1.0", "de.eldoria.bloodnight.Main"));
    }

    @Test
    void theFixedStackKeepsTheNewestLines() {
        var stack = new LogMeta.FixedStack<Integer>(3);
        IntStream.range(0, 10).forEach(stack::add);

        assertEquals(List.of(7, 8, 9), stack.getLinkedList());
    }

    @Test
    void aLongLogKeepsItsStartAndItsEnd() throws IOException {
        int lines = LogMeta.MAX_LOG_PART_SIZE * 3;
        Path log = folder.resolve("latest.log");
        Files.write(log, IntStream.range(0, lines).mapToObj(i -> "[12:00:00] [Server thread/INFO]: line " + i).toList());

        String text = LogMeta.fromFile(log, plugin, DebugSettings.DEFAULT).getLog();

        assertTrue(text.contains("line 0\n"));
        assertTrue(text.contains("line " + (LogMeta.MAX_LOG_PART_SIZE - 1) + "\n\n[...]\n\n"));
        assertTrue(text.endsWith("line " + (lines - 1)));
    }

    @Test
    void exceptionsAreSortedByOriginAndKeepTheirOrder() throws IOException {
        Path log = folder.resolve("latest.log");
        Files.writeString(log, """
                [12:00:00] [Server thread/ERROR]: first
                \tat de.eldoria.bloodnight.Main.run(Main.java:1)
                [12:00:01] [Server thread/WARN]: elsewhere
                \tat com.example.Other.run(Other.java:1)
                [12:00:02] [Server thread/ERROR]: second
                \tat de.eldoria.bloodnight.Mob.tick(Mob.java:1)
                [12:00:03] [Server thread/INFO]: Done
                """);

        var data = LogMeta.fromFile(log, plugin, DebugSettings.DEFAULT);

        assertArrayEquals(new String[]{
                "[Server thread/ERROR]: first\n\tat de.eldoria.bloodnight.Main.run(Main.java:1)\n",
                "[Server thread/ERROR]: second\n\tat de.eldoria.bloodnight.Mob.tick(Mob.java:1)\n"}, data.getInternalExceptions());
        assertEquals(1, data.getExternalExceptions().length);
    }

    @Test
    void aMissingLogSaysSo() {
        assertEquals("Could not read latest log.", LogMeta.fromFile(folder.resolve("none.log"), plugin, DebugSettings.DEFAULT).getLog());
    }
}
