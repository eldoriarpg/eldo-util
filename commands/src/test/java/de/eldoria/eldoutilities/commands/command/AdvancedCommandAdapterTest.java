/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.command;

import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.executor.ITabExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdvancedCommandAdapterTest {
    @Test
    void anUnexpectedErrorIsLoggedInsteadOfThrown() {
        var records = new ArrayList<LogRecord>();
        var logger = Logger.getLogger("AdvancedCommandAdapterTest");
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        });
        var plugin = mock(Plugin.class);
        when(plugin.getLogger()).thenReturn(logger);
        var adapter = AdvancedCommandAdapter.wrap(plugin, new Failing(plugin));

        assertTrue(adapter.onCommand(mock(CommandSender.class), mock(Command.class), "failing", new String[0]));

        assertEquals(
                List.of(Level.SEVERE), records.stream().map(LogRecord::getLevel).toList());
    }

    private static final class Failing extends AdvancedCommand implements ITabExecutor {
        private Failing(Plugin plugin) {
            super(plugin, CommandMeta.builder("failing").build());
        }

        @Override
        public void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) {
            throw new IllegalStateException("broken");
        }
    }
}
