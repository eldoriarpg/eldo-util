/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.scheduling;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

/**
 * How the queue treats the time limit it is given.
 */
class QueuingSelfSchedulingTaskTest {
    private final Plugin plugin = mock(Plugin.class);
    private final Logger logger = Logger.getLogger("QueuingSelfSchedulingTaskTest");
    private final List<LogRecord> warnings = new ArrayList<>();
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
    void setUp() {
        logger.addHandler(collector);
        when(plugin.getLogger()).thenReturn(logger);
    }

    @AfterEach
    void tearDown() {
        logger.removeHandler(collector);
    }

    @Test
    void aLimitWithinOneTickIsAcceptedSilently() {
        new Queue(plugin, 50);
        new Queue(plugin, 10);

        assertTrue(warnings.isEmpty());
    }

    @Test
    void aLimitAboveOneTickIsLoweredWithAWarning() {
        new Queue(plugin, 200);

        assertEquals(1, warnings.size());
        assertEquals(
                "Queue: a time limit of 200 ms is above one tick. Using 50 ms.",
                warnings.getFirst().getMessage());
    }

    /**
     * A queue that does nothing with its objects.
     */
    private static class Queue extends QueuingSelfSchedulingTask<String> {
        Queue(Plugin plugin, int maxDurationTarget) {
            super(plugin, DEFAULT_MAX_IDLE_TICKS, maxDurationTarget);
        }

        @Override
        public void execute(String object) {}
    }
}
