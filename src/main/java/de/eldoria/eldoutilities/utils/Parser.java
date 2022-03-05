/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * This class contains methods to parse strings to primitve types and other things.
 *
 * @since 1.0.0
 */
public final class Parser {
    private Parser() {
        throw new UnsupportedOperationException("This is a utility class!");
    }

    /**
     * Parse a string to a int.
     *
     * @param s string to parse
     * @return optional integer with integer when the parsing was successful.
     */
    public static Optional<Integer> parseInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Parse a string to a double.
     *
     * @param s string to parse
     * @return optional double with double when the parsing was successful.
     */
    public static Optional<Double> parseDouble(String s) {
        try {
            return Optional.of(Double.parseDouble(s.replace(",", ".")));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Parse a string to a boolean
     *
     * @param s string to parse
     * @return optional boolean with boolean when the parsing was successful.
     */
    public static Optional<Boolean> parseBoolean(String s) {
        return parseBoolean(s, "true", "false");
    }

    /**
     * Parse a string to a boolean
     *
     * @param s          string to parse
     * @param trueValue  true value as string
     * @param falseValue false value as string
     * @return optional boolean with boolean when the parsing was successful.
     */
    public static Optional<Boolean> parseBoolean(String s, String trueValue, String falseValue) {
        if (s.equalsIgnoreCase(trueValue)) {
            return Optional.of(true);
        }
        if (s.equalsIgnoreCase(falseValue)) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    /**
     * Parses a time in format H24:mm to ticks.
     *
     * @param s string to parse
     * @return time as ticks or null if value could not be parsed.
     */
    public static OptionalInt parseTimeToTicks(String s) {
        var split = s.split(":");
        if (split.length != 2) return OptionalInt.empty();
        var hour = parseInt(split[0]);
        var min = parseInt(split[1]);

        if (!hour.isPresent() || !min.isPresent()) return OptionalInt.empty();

        var hourTicks = (hour.get() - 6) * 1000 % 24000;
        if (hourTicks < 0) hourTicks = 24000 + hourTicks;
        var minTicks = (int) Math.floor(1000 / 60d * min.get());

        return OptionalInt.of(hourTicks + minTicks);
    }

    /**
     * Parses ticks to a time with format H24:mm.
     *
     * @param ticks ticks to parse
     * @return ticks as time
     */
    public static String parseTicksToTime(long ticks) {
        var time = ticks % 24000;
        var hours = ((int) Math.floor(time / 1000d) + 6) % 24;
        var min = (int) Math.floor(((time % 1000) + 1) / (1000 / 60d));
        if (min < 10) return hours + ":0" + min;
        return hours + ":" + min;
    }

    public static <T> Optional<Long> parseLong(String value) {
        try {
            return Optional.of(Long.parseLong(value.replace(",", ".")));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
