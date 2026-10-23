/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion;

import de.eldoria.eldoutilities.utils.Version;

/**
 * A exception which is thrown when the current server version is not supported by the plugin.
 * @deprecated eldo-util targets a single Paper version. This module is removed in 4.0.
 */
@Deprecated(since = "3.6.0", forRemoval = true)
public class UnsupportedVersionException extends RuntimeException {
    public UnsupportedVersionException() {
        super("Version " + ServerVersion.CURRENT_VERSION + " is not supported.");
    }

    public UnsupportedVersionException(Version version) {
        super("Version " + version + " is not supported.");
    }
}
