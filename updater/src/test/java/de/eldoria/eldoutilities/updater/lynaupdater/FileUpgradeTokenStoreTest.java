/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FileUpgradeTokenStoreTest {
    @TempDir
    Path dir;

    @Test
    void missingFileHasNoToken() {
        assertEquals(
                Optional.empty(),
                UpgradeTokenStore.file(dir.resolve("upgrade-token.txt")).token());
    }

    @Test
    void blankFileHasNoToken() throws IOException {
        var path = Files.writeString(dir.resolve("upgrade-token.txt"), " \n");
        assertEquals(Optional.empty(), UpgradeTokenStore.file(path).token());
    }

    @Test
    void readsPastedTokenWithoutSurroundingWhitespace() throws IOException {
        var path = Files.writeString(dir.resolve("upgrade-token.txt"), "lyu_abc\n");
        assertEquals(Optional.of("lyu_abc"), UpgradeTokenStore.file(path).token());
    }

    @Test
    void replacesTokenAndLeavesNoTemporaryFile() throws IOException {
        var path = Files.writeString(dir.resolve("upgrade-token.txt"), "lyu_old");
        var store = UpgradeTokenStore.file(path);
        store.token("lyu_new");
        assertEquals(Optional.of("lyu_new"), store.token());
        assertFalse(Files.exists(dir.resolve("upgrade-token.txt.tmp")));
    }

    @Test
    void createsMissingDirectories() {
        var store = UpgradeTokenStore.file(dir.resolve("plugin/upgrade-token.txt"));
        store.token("lyu_new");
        assertEquals(Optional.of("lyu_new"), store.token());
    }
}
