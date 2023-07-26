/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion;

import de.eldoria.eldoutilities.utils.Version;
import org.bukkit.Bukkit;

/**
 * Enum to determine and work with multiple versions.
 *
 * @since 1.0.0
 */
public final class ServerVersion {
    public static final Version MC_UNKOWN = Version.of(0, 0, 0);

    /**
     * Contains the current version of the server.
     */
    public static final Version CURRENT_VERSION;

    static {
        CURRENT_VERSION = Bukkit.getServer() != null ? getVersion() : MC_UNKOWN;
    }

    /**
     * Get the version of the server.
     *
     * @return version of server
     */
    public static Version getVersion() {
        return Version.parse(Bukkit.getServer().getBukkitVersion());
    }

    /**
     * This method will check if the current version is between the oldest and newest version. Will abort enable of
     * plugin when called on enable.
     *
     * @param oldest oldest version (inclusive)
     * @param newest newest version (inclusive)
     * @throws UnsupportedVersionException when the server version is not between the oldest and newest version.
     */
    public static void forceVersion(Version oldest, Version newest) {
        if (CURRENT_VERSION.isBetween(oldest, newest)) return;
        throw new UnsupportedVersionException();
    }
}
