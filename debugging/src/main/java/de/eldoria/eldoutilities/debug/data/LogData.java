/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.data;

/**
 * Represents log data.
 */
public class LogData {
    private final String log;
    private final String pluginLog;
    private final String[] internalExceptions;
    private final String[] externalExceptions;

    public LogData(String log, String pluginLog, String[] internalExceptions, String[] externalExceptions) {
        this.log = log;
        this.pluginLog = pluginLog;
        this.internalExceptions = internalExceptions;
        this.externalExceptions = externalExceptions;
    }
}
