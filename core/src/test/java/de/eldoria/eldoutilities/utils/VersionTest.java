/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionTest {
    @Test
    public void testVersion() {
        Version version = Version.of(1, 16, 5);
        Assertions.assertTrue(version.isOlder(Version.of(1, 17)));
        Assertions.assertFalse(version.isNewer(Version.of(1, 17)));
        Assertions.assertTrue(version.isOlderOrEqual(Version.of(1, 17)));
        Assertions.assertTrue(version.isOlderOrEqual(Version.of(1, 16, 5)));
        Assertions.assertTrue(version.isNewerOrEqual(Version.of(1,16,5)));
        Assertions.assertTrue(version.isNewerOrEqual(Version.of(1,16,4)));
        Assertions.assertTrue(version.isNewerOrEqual(Version.of(1,16)));
    }
}
