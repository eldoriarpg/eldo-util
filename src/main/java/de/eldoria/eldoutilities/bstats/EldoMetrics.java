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
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        enabled = config.getBoolean("enabled", true);
    }

    public boolean isEnabled() {
        return enabled;
    }
}
