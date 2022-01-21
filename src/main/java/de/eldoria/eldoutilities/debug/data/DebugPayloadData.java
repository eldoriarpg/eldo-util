/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.data;

/**
 * Payload for UpdateButler
 */
public class DebugPayloadData {
    private final int v = 1;
    protected PluginMetaData pluginMeta;
    protected ServerMetaData serverMeta;
    protected EntryData[] additionalPluginMeta;
    protected LogData latestLog;
    protected EntryData[] configDumps;

    public DebugPayloadData(PluginMetaData pluginMeta, ServerMetaData serverMeta, EntryData[] additionalPluginMeta, LogData latestLog, EntryData[] configDumps) {
        this.pluginMeta = pluginMeta;
        this.serverMeta = serverMeta;
        this.additionalPluginMeta = additionalPluginMeta;
        this.latestLog = latestLog;
        this.configDumps = configDumps;
    }


}
