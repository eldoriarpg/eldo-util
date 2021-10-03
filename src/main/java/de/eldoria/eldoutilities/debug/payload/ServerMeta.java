package de.eldoria.eldoutilities.debug.payload;

import de.eldoria.eldoutilities.debug.data.PluginMetaData;
import de.eldoria.eldoutilities.debug.data.ServerMetaData;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;

import java.util.Arrays;

public final class ServerMeta extends ServerMetaData {

    private ServerMeta(String version, int currentPlayers, String[] loadedWorlds, PluginMetaData[] plugins) {
        super(version, currentPlayers, loadedWorlds, plugins);
    }

    public static ServerMetaData create() {
        Server server = Bukkit.getServer();
        String version = server.getVersion();
        int currentPlayers = server.getOnlinePlayers().size();
        String[] loadedWorlds = server.getWorlds().stream().map(World::getName).toArray(String[]::new);
        PluginMeta[] plugins = Arrays.stream(server.getPluginManager().getPlugins())
                .map(PluginMeta::create)
                .toArray(PluginMeta[]::new);

        return new ServerMeta(version, currentPlayers, loadedWorlds, plugins);
    }
}
