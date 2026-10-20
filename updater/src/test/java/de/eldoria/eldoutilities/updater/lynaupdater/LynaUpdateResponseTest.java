/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import com.google.gson.Gson;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LynaUpdateResponseTest {
    @ParameterizedTest
    @ValueSource(strings = {"1.2.3", "2.0", "3.0.0-RC1"})
    void stableVersions(String version) {
        assertTrue(new LynaUpdateResponse(true, version, 0).isStable());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.2.3-SNAPSHOT", "1.2.3-DEV", "1.2.3-feature-x-SNAPSHOT", "1.2.3-dev"})
    void unstableVersions(String version) {
        assertFalse(new LynaUpdateResponse(true, version, 0).isStable());
    }

    @Test
    void readsDownloadOfFreeProduct() {
        var response = new Gson().fromJson("""
                {"update":true,"latest":"1.2.3","published":10,"download":"https://lyna.eldoria.de/api/v1/download/direct/1/2/1.2.3"}
                """, LynaUpdateResponse.class);
        assertEquals(Optional.of("https://lyna.eldoria.de/api/v1/download/direct/1/2/1.2.3"), response.download());
    }

    @Test
    void premiumProductHasNoDownload() {
        var response = new Gson().fromJson("""
                {"update":true,"latest":"1.2.3","published":10,"download":null}
                """, LynaUpdateResponse.class);
        assertEquals(Optional.empty(), response.download());
    }
}
