/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static de.eldoria.eldoutilities.messages.conversion.MiniMessageConversion.Replacement.create;

public class MiniMessageConversion {
    private static final List<Replacement> CODES = new ArrayList<>();
    private static final Pattern LEGACY_CODE = Pattern.compile("[§&][a-f0-9klmnor]");

    static {
        CODES.add(create("0", "black"));
        CODES.add(create("1", "dark_blue"));
        CODES.add(create("2", "dark_green"));
        CODES.add(create("3", "dark_aqua"));
        CODES.add(create("4", "dark_red"));
        CODES.add(create("5", "dark_purple"));
        CODES.add(create("6", "gold"));
        CODES.add(create("7", "gray"));
        CODES.add(create("8", "dark_gray"));
        CODES.add(create("9", "blue"));
        CODES.add(create("a", "green"));
        CODES.add(create("b", "aqua"));
        CODES.add(create("c", "red"));
        CODES.add(create("d", "light_purple"));
        CODES.add(create("e", "yellow"));
        CODES.add(create("f", "white"));
        CODES.add(create("k", "obf"));
        CODES.add(create("l", "b"));
        CODES.add(create("m", "st"));
        CODES.add(create("n", "u"));
        CODES.add(create("o", "i"));
        CODES.add(create("r", "reset"));
    }

    /**
     * Converts legacy color codes in a given message to their corresponding tags.
     *
     * @param message The message to convert.
     * @return The converted message with color code tags.
     */
    public static String convertLegacyColorCodes(String message) {
        if (!LEGACY_CODE.matcher(message).find()) return message;
        for (var entry : CODES) message = entry.apply(message);
        return message;
    }

    record Replacement(Pattern pattern, String tag) {
        public static Replacement create(String code, String tag) {
            return new Replacement(Pattern.compile("[§&]" + code), "<%s>".formatted(tag));
        }

        public String apply(String message) {
            return pattern.matcher(message).replaceAll(tag);
        }
    }
}
