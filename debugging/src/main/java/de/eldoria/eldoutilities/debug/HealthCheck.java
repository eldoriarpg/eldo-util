/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

/**
 * The outcome of something a plugin checked about its own setup, shown first in a debug report, worst first.
 *
 * @param name    what was checked, e.g. "Database"
 * @param level   how it went
 * @param message what was found, e.g. "Reachable in 3 ms" or "World 'nether' is not loaded"
 */
public record HealthCheck(String name, Level level, String message) {
    public static HealthCheck ok(String name, String message) {
        return new HealthCheck(name, Level.OK, message);
    }

    public static HealthCheck warn(String name, String message) {
        return new HealthCheck(name, Level.WARN, message);
    }

    public static HealthCheck error(String name, String message) {
        return new HealthCheck(name, Level.ERROR, message);
    }

    public enum Level {
        OK,
        WARN,
        ERROR
    }
}
