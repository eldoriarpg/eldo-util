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
 * Helper for the Metrics class of bstats. Just because they removed this shitty isEnabled() function.
 */
public class EldoMetrics {

    /**
     * Check if metrics are enabled
     *
     * @return true if enabled
     */
    public static boolean isEnabled(Plugin plugin) {
        var bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        var configFile = new File(bStatsFolder, "config.yml");
        var config = YamlConfiguration.loadConfiguration(configFile);
        return config.getBoolean("enabled", true);
    }
}
