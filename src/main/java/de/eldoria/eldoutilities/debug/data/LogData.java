/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
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
