/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion;

/**
 * A exception which is thrown when the current server version is not supported by the plugin.
 */
public class UnsupportedVersionException extends RuntimeException {
    public UnsupportedVersionException() {
        super("Version " + ServerVersion.CURRENT_VERSION + " is not supported.");
    }
}
