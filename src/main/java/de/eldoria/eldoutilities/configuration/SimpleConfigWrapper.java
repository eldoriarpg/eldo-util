package de.eldoria.eldoutilities.configuration;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.function.Function;

public class SimpleConfigWrapper {
    private final Plugin plugin;
    private FileConfiguration config;

    public SimpleConfigWrapper(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public void save() {
        plugin.saveConfig();
    }

    public <T> T get(Function<MemoryConfiguration, T> call) {
        return call.apply(config);
    }

    public MemoryConfiguration get() {
        return config;
    }
}
