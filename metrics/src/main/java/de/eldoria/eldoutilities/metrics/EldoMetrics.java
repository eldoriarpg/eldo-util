/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.metrics;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * Helper for bStats, whose Metrics class no longer tells whether metrics are enabled.
 */
public class EldoMetrics {

    /**
     * Checks whether the server owner left bStats enabled in {@code plugins/bStats/config.yml}.
     *
     * @param plugin any plugin of the server, used to find the plugins folder
     * @return true if enabled
     */
    public static boolean isEnabled(Plugin plugin) {
        var bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        var configFile = new File(bStatsFolder, "config.yml");
        var config = YamlConfiguration.loadConfiguration(configFile);
        return config.getBoolean("enabled", true);
    }
}
