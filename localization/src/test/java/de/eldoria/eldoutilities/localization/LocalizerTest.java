/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.localization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class LocalizerTest {
    private static final Set<String> LOADED = Set.of("en_US", "de_DE", "zh_CN");

    @Test
    void anExactLocaleIsUsed() {
        Assertions.assertEquals("de_DE", Localizer.closestLanguage(LOADED, "de_DE", "en_US"));
        Assertions.assertEquals("en_US", Localizer.closestLanguage(LOADED, "en-us", "en_US"));
    }

    @Test
    void anotherLocaleOfTheSameLanguageIsUsed() {
        Assertions.assertEquals("de_DE", Localizer.closestLanguage(LOADED, "de_AT", "en_US"));
        Assertions.assertEquals("en_US", Localizer.closestLanguage(LOADED, "en_GB", "en_US"));
    }

    @Test
    void anUnknownLanguageFallsBack() {
        Assertions.assertEquals("en_US", Localizer.closestLanguage(LOADED, "fr_FR", "en_US"));
        Assertions.assertEquals("en_US", Localizer.closestLanguage(LOADED, null, "en_US"));
    }
}
