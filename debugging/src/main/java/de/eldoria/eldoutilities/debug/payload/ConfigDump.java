/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.payload;

import de.eldoria.eldoutilities.debug.DebugSettings;
import de.eldoria.eldoutilities.debug.data.EntryData;
import org.bukkit.plugin.Plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class ConfigDump extends EntryData {

    public ConfigDump(String path, String content) {
        super(path, content);
    }

    /**
     * Creates a new config dump. This dump will include external configs as well if the plugin is a {@code EldoCommand}.
     *
     * @param plugin   plugin to dump the configs
     * @param settings settings for debug dispatching
     * @return configs as an array.
     */
    public static EntryData[] create(Plugin plugin, DebugSettings settings) {
        var root = plugin.getDataFolder().toPath().toAbsolutePath().getParent().getParent();

        var mainConfig = plugin.getConfig();

        List<ConfigDump> dumps = new LinkedList<>();
        if (mainConfig != null) {
            try {
                dumps.add(new ConfigDump("config.yml", mainConfig.saveToString()));
            } catch (Exception e) {
                plugin.getLogger().log(Level.CONFIG, "something went wrong while saving the config. Skipping", e);
            }
        }

        dumps.forEach(e -> e.applyFilter(settings));

        return dumps.toArray(new ConfigDump[0]);
    }
}
