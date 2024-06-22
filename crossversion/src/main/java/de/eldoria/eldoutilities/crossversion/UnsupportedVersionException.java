/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion;

import de.eldoria.eldoutilities.utils.Version;

/**
 * A exception which is thrown when the current server version is not supported by the plugin.
 */
public class UnsupportedVersionException extends RuntimeException {
    public UnsupportedVersionException() {
        super("Version " + ServerVersion.CURRENT_VERSION + " is not supported.");
    }

    public UnsupportedVersionException(Version version) {
        super("Version " + version + " is not supported.");
    }
}
