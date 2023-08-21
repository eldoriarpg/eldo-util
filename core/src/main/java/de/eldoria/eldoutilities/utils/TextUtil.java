/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

/**
 * Basic text utilities
 *
 * @since 1.0.0
 */
public final class TextUtil {
    private TextUtil() {
        throw new UnsupportedOperationException("This is a utility class!");
    }

    /**
     * Count how often the char was used inside the string.
     *
     * @param string string to check
     * @param count  char to count
     * @return the number of occurences of the char in the string.
     */
    public static int countChars(String string, char count) {
        var i = 0;
        for (var c : string.toCharArray()) {
            if (c == count) i++;
        }
        return i;
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
}
