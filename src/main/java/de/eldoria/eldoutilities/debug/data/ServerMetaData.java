/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.data;

public class ServerMetaData {
    protected String version;
    protected int currentPlayers;
    protected String[] loadedWorlds;
    protected PluginMetaData[] plugins;

    protected ServerMetaData(String version, int currentPlayers, String[] loadedWorlds, PluginMetaData[] plugins) {
        this.version = version;
        this.currentPlayers = currentPlayers;
        this.loadedWorlds = loadedWorlds;
        this.plugins = plugins;
    }
}
