/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Keeps the upgrade token a premium product trades for its download at Lyna.
 * <p>
 * Every exchange spends the token and answers with the next one, which replaces it here before the download starts.
 */
public interface UpgradeTokenStore {
    /**
     * Stores the token in a plain text file.
     *
     * @param path file holding nothing but the token
     * @return token store
     */
    static UpgradeTokenStore file(Path path) {
        return new FileUpgradeTokenStore(path);
    }

    /**
     * The token to trade next.
     *
     * @return the token, or empty when none was configured
     */
    Optional<String> token();

    /**
     * Replaces the stored token.
     *
     * @param token the token to trade next
     */
    void token(String token);

    /**
     * Where the token is kept, to tell an admin where to put a new one.
     *
     * @return human-readable location
     */
    String location();
}
