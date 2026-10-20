/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages.conversion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static de.eldoria.eldoutilities.messages.conversion.MiniMessageConversion.convertLegacyColorCodes;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MiniMessageConversionTest {
    @Test
    void sectionCodesAreAlwaysConverted() {
        assertEquals("<green>Hello<b>World", convertLegacyColorCodes("§aHello§lWorld"));
    }

    @Test
    void ampersandCodesAreConvertedAtTheStartOfAWord() {
        assertEquals("<gold>Hello <red>World", convertLegacyColorCodes("&6Hello &cWorld"));
    }

    @Test
    void chainedCodesAreConverted() {
        assertEquals("<b><green>Hello", convertLegacyColorCodes("&l&aHello"));
    }

    @Test
    void codesAreRecognisedInEitherCase() {
        assertEquals("<green>Green <b>Bold", convertLegacyColorCodes("§AGreen &LBold"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"rock&roll", "https://example.com/?a=1&b=2", "Tom&Jerry", "no codes at all"})
    void ampersandsInsideWordsStay(String text) {
        assertEquals(text, convertLegacyColorCodes(text));
    }
}
