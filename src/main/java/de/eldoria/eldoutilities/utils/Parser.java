package de.eldoria.eldoutilities.utils;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

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
    public static OptionalInt parseInt(String s) {
        try {
            return OptionalInt.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    /**
     * Parse a string to a double.
     *
     * @param s string to parse
     * @return optional double with double when the parsing was successful.
     */
    public static OptionalDouble parseDouble(String s) {
        try {
            return OptionalDouble.of(Double.parseDouble(s.replace(",", ".")));
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
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
        String[] split = s.split(":");
        if (split.length != 2) return OptionalInt.empty();
        OptionalInt hour = parseInt(split[0]);
        OptionalInt min = parseInt(split[1]);

        if (!hour.isPresent() || !min.isPresent()) return OptionalInt.empty();

        int hourTicks = (hour.getAsInt() - 6) * 1000 % 24000;
        if (hourTicks < 0) hourTicks = 24000 + hourTicks;
        int minTicks = (int) Math.floor(1000 / 60d * min.getAsInt());

        return OptionalInt.of(hourTicks + minTicks);
    }

    /**
     * Parses ticks to a time with format H24:mm.
     *
     * @param ticks ticks to parse
     * @return ticks as time
     */
    public static String parseTicksToTime(long ticks) {
        long time = ticks % 24000;
        int hours = ((int) Math.floor(time / 1000d) + 6) % 24;
        int min = (int) Math.floor(((time % 1000) + 1) / (1000 / 60d));
        if (min < 10) return hours + ":0" + min;
        return hours + ":" + min;
    }
}
