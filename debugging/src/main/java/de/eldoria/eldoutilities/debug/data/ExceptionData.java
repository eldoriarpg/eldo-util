/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.data;

/**
 * An error or warning found in the log, with how often it occurred.
 *
 * @param text      the logged message and stack trace
 * @param count     how often it was logged
 * @param firstSeen the log time of the first occurrence, {@code HH:mm:ss}
 * @param lastSeen  the log time of the last occurrence, {@code HH:mm:ss}
 */
public record ExceptionData(String text, int count, String firstSeen, String lastSeen) {}
