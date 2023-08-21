/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.payload;

import de.eldoria.eldoutilities.debug.data.PluginMetaData;
import de.eldoria.eldoutilities.debug.data.ServerMetaData;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Arrays;

public final class ServerMeta extends ServerMetaData {

    private ServerMeta(String version, int currentPlayers, String[] loadedWorlds, PluginMetaData[] plugins) {
        super(version, currentPlayers, loadedWorlds, plugins);
    }

    public static ServerMetaData create() {
        var server = Bukkit.getServer();
        var version = server.getVersion();
        var currentPlayers = server.getOnlinePlayers().size();
        var loadedWorlds = server.getWorlds().stream().map(World::getName).toArray(String[]::new);
        var plugins = Arrays.stream(server.getPluginManager().getPlugins())
                .map(PluginMeta::create)
                .toArray(PluginMeta[]::new);

        return new ServerMeta(version, currentPlayers, loadedWorlds, plugins);
    }
}
