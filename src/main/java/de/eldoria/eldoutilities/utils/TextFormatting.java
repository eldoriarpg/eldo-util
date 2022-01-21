/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public final class TextFormatting {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");

    private TextFormatting() {
    }

    /**
     * Appends white spaces to a string to match the given length. Returns input if fill is smaller or equal
     * string.length()
     *
     * @param string String to fill
     * @param fill   Desired String length
     * @return filled string.
     */
    public static String fillString(String string, int fill) {
        if (string.length() >= fill) {
            return string;
        }
        var charsToFill = fill - string.length();
        var builder = new StringBuilder(string);
        builder.append(" ".repeat(charsToFill));
        return builder.toString();
    }

    /**
     * Returns a range of a string array as string.
     *
     * @param delimiter delimiter for string join
     * @param source    source array
     * @param from      start index (included). Use negative counts to count from the last index.
     * @param to        end index (excluded). Use negative counts to count from the last index.
     * @return range as string
     */
    public static String getRangeAsString(String delimiter, String[] source, int from, int to) {
        var finalTo = to;
        if (to < 1) {
            finalTo = source.length + to;
        }
        var finalFrom = from;
        if (from < 0) {
            finalFrom = source.length + from;
        }

        if (finalFrom > finalTo || finalFrom < 0 || finalTo > source.length) {
            return "";
        }

        return String.join(delimiter, Arrays.copyOfRange(source, finalFrom, finalTo)).trim();
    }

    /**
     * Trims a text to the desired length. Returns unmodified input if max chars is larger or equal string.length().
     *
     * @param string      String to trim
     * @param endSequence end sequence which should be append at the end of the string. included in max chars.
     * @param maxChars    max char length.
     * @param keepWords   true if no word should be cut.
     * @return String with length of maxChars of shorter.
     */
    public static String cropText(String string, String endSequence, int maxChars, boolean keepWords) {
        if (string.length() <= maxChars) {
            return string;
        }
        if (!keepWords) {
            var substring = string.substring(0, Math.max(0, maxChars - endSequence.length()));
            return (substring + endSequence).trim();
        }

        var split = string.split("\\s");

        var builder = new StringBuilder();

        for (var s : split) {
            if (builder.length() + s.length() + 1 + endSequence.length() > maxChars) {
                return builder.toString().trim() + endSequence;
            }
            builder.append(s).append(" ");
        }
        return builder.toString().trim();
    }


    /**
     * Changes the boolean in to a specified String.
     *
     * @param bool    boolean value
     * @param trueTo  value if true
     * @param falseTo value if false
     * @return bool as string representative.
     */
    public static String mapBooleanTo(boolean bool, String trueTo, String falseTo) {
        return bool ? trueTo : falseTo;
    }

    /**
     * Get the current time as string.
     *
     * @return time in format:  HH:mm dd.MM.yyyy
     */
    public static String getTimeAsString() {
        return DATE_TIME_FORMATTER.format(LocalDateTime.now());
    }
}
