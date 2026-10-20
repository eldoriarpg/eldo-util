/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * Keeps the upgrade token in a plain text file.
 * <p>
 * A new token is written to a sibling file first and moved over the old one, so a crash never leaves a half-written
 * token behind.
 */
class FileUpgradeTokenStore implements UpgradeTokenStore {
    private final Path path;

    FileUpgradeTokenStore(Path path) {
        this.path = path;
    }

    @Override
    public Optional<String> token() {
        if (!Files.exists(path)) return Optional.empty();
        try {
            return Optional.of(Files.readString(path).strip()).filter(token -> !token.isEmpty());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read upgrade token from " + path, e);
        }
    }

    @Override
    public void token(String token) {
        try {
            Files.createDirectories(path.toAbsolutePath().getParent());
            var temp = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(temp, token);
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write upgrade token to " + path, e);
        }
    }

    @Override
    public String location() {
        return path.toString();
    }
}
