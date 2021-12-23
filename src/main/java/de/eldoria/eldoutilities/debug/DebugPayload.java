package de.eldoria.eldoutilities.debug;

import de.eldoria.eldoutilities.debug.data.DebugPayloadData;
import de.eldoria.eldoutilities.debug.data.EntryData;
import de.eldoria.eldoutilities.debug.data.LogData;
import de.eldoria.eldoutilities.debug.data.PluginMetaData;
import de.eldoria.eldoutilities.debug.data.ServerMetaData;
import de.eldoria.eldoutilities.debug.payload.ConfigDump;
import de.eldoria.eldoutilities.debug.payload.LogMeta;
import de.eldoria.eldoutilities.debug.payload.PluginMeta;
import de.eldoria.eldoutilities.debug.payload.ServerMeta;
import org.bukkit.plugin.Plugin;

public final class DebugPayload extends DebugPayloadData {

    private DebugPayload(PluginMetaData pluginMeta, ServerMetaData serverMeta, EntryData[] additionalPluginMeta,
                         LogData latestLog, EntryData[] configDumps) {
        super(pluginMeta, serverMeta, additionalPluginMeta, latestLog, configDumps);
    }

    /**
     * Create a new debug payload.
     *
     * @param plugin plugin to create debug data for
     * @return debug payload data
     */
    public static DebugPayloadData create(Plugin plugin) {
        return create(plugin, DebugSettings.DEFAULT);
    }

    /**
     * Create a new debug payload.
     *
     * @param plugin   plugin to create debug data for
     * @param settings settings for debug sending
     * @return debug payload data
     */
    public static DebugPayloadData create(Plugin plugin, DebugSettings settings) {
        var pluginMeta = PluginMeta.create(plugin);
        var serverMeta = ServerMeta.create();
        var additionalPluginMeta = DebugUtil.getAdditionalPluginMeta(plugin);
        var latestLog = LogMeta.create(plugin, settings);
        var configDumps = ConfigDump.create(plugin, settings);
        return new DebugPayload(pluginMeta, serverMeta, additionalPluginMeta, latestLog, configDumps);
    }
}
