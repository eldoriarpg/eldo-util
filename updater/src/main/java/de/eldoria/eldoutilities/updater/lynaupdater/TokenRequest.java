/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import org.jetbrains.annotations.Nullable;

/**
 * The build asked for when exchanging an upgrade token.
 *
 * @param version      version of the build
 * @param downloadType download type of the build, or null for the first one the account may have
 */
record TokenRequest(String version, @Nullable Integer downloadType) {
}
