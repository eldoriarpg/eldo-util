/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages.conversion;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Map.entry;

/**
 * Converts legacy color codes into MiniMessage tags.
 * <p>
 * A {@code §} code is always converted. A {@code &} code is only converted where a color code can sensibly begin: at
 * the start of the message, after a character that is neither letter nor digit, or directly after another code, as
 * in {@code &l&a}. This keeps an ampersand inside a word or a URL, such as {@code rock&roll} or {@code ?a=1&b=2},
 * as it is. Codes are recognised in either case.
 */
public class MiniMessageConversion {
    private static final Pattern LEGACY_CODE = Pattern.compile("[§&]([0-9a-fk-or])", Pattern.CASE_INSENSITIVE);
    private static final Map<Character, String> TAGS = Map.ofEntries(
            entry('0', "black"),
            entry('1', "dark_blue"),
            entry('2', "dark_green"),
            entry('3', "dark_aqua"),
            entry('4', "dark_red"),
            entry('5', "dark_purple"),
            entry('6', "gold"),
            entry('7', "gray"),
            entry('8', "dark_gray"),
            entry('9', "blue"),
            entry('a', "green"),
            entry('b', "aqua"),
            entry('c', "red"),
            entry('d', "light_purple"),
            entry('e', "yellow"),
            entry('f', "white"),
            entry('k', "obf"),
            entry('l', "b"),
            entry('m', "st"),
            entry('n', "u"),
            entry('o', "i"),
            entry('r', "reset"));

    /**
     * Converts legacy color codes in a given message to their corresponding tags.
     *
     * @param message The message to convert.
     * @return The converted message with color code tags.
     */
    public static String convertLegacyColorCodes(String message) {
        var matcher = LEGACY_CODE.matcher(message);
        var result = new StringBuilder();
        int lastCodeEnd = -1;
        while (matcher.find()) {
            if (!isCode(message, matcher.start(), lastCodeEnd)) continue;
            var code = matcher.group(1).toLowerCase(Locale.ROOT).charAt(0);
            matcher.appendReplacement(result, "<%s>".formatted(TAGS.get(code)));
            lastCodeEnd = matcher.end();
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static boolean isCode(String message, int start, int lastCodeEnd) {
        if (message.charAt(start) == '§') return true;
        if (start == 0 || start == lastCodeEnd) return true;
        return !Character.isLetterOrDigit(message.charAt(start - 1));
    }
}
