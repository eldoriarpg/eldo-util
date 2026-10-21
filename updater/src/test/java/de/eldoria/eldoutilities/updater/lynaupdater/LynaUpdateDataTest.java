/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LynaUpdateDataTest {
    @Test
    void readsTheProductIdOfTheBuildData() {
        assertEquals(8, LynaUpdateData.productId(Optional.of("8")));
        assertEquals(8, LynaUpdateData.productId(Optional.of(" 8\r")));
    }

    @Test
    void refusesAMissingOrInvalidProductId() {
        assertThrows(IllegalStateException.class, () -> LynaUpdateData.productId(Optional.empty()));
        assertThrows(IllegalStateException.class, () -> LynaUpdateData.productId(Optional.of("grid")));
    }

    @Test
    void linksToTheProductPage() {
        assertEquals("https://lyna.eldoria.de/products/8", LynaUpdateDataBuilder.productPage("https://lyna.eldoria.de", 8));
        assertEquals("https://lyna.eldoria.de/products/8", LynaUpdateDataBuilder.productPage("https://lyna.eldoria.de/", 8));
    }
}
