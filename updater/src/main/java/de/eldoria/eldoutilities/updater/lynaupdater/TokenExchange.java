/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

/**
 * Lyna's answer to an upgrade token exchange.
 *
 * @param upgradeToken the token to trade next time; the one sent no longer works
 * @param downloadUrl  serves the build for a few minutes
 * @param version      version of the build
 * @param filename     file name of the build
 * @param sizeBytes    size of the build
 */
record TokenExchange(String upgradeToken, String downloadUrl, String version, String filename, long sizeBytes) {}
