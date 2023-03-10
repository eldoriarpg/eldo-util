/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.bstats;

import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Wrapper for the Metrics class of bstats. Just because they removed this shitty isEnabled() function.
 */
public class EldoMetrics extends Metrics {
    private final boolean enabled;

    /**
     * Creates a new Metrics instance.
     *
     * @param plugin    Your plugin instance.
     * @param serviceId The id of the service.
     */
    public EldoMetrics(JavaPlugin plugin, int serviceId) {
        super(plugin, serviceId);
        var bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        var configFile = new File(bStatsFolder, "config.yml");
        var config = YamlConfiguration.loadConfiguration(configFile);
        enabled = config.getBoolean("enabled", true);
    }

    /**
     * Check if metrics are enabled
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
}
