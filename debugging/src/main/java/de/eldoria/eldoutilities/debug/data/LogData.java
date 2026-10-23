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
    private final ExceptionData[] internalExceptions;
    private final ExceptionData[] externalExceptions;

    public LogData(
            String log, String pluginLog, ExceptionData[] internalExceptions, ExceptionData[] externalExceptions) {
        this.log = log;
        this.pluginLog = pluginLog;
        this.internalExceptions = internalExceptions;
        this.externalExceptions = externalExceptions;
    }

    public String getLog() {
        return log;
    }

    public String getPluginLog() {
        return pluginLog;
    }

    public ExceptionData[] getInternalExceptions() {
        return internalExceptions;
    }

    public ExceptionData[] getExternalExceptions() {
        return externalExceptions;
    }
}
