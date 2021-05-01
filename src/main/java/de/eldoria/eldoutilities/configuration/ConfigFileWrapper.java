package de.eldoria.eldoutilities.configuration;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * A simple wrapper to manage File configurations.
 */
public final class ConfigFileWrapper {
    private final File file;
    private final Plugin plugin;
    private FileConfiguration fileConfiguration;

    private ConfigFileWrapper(Plugin plugin, String filePath, @Nullable Configuration defaultConfig) {
        this.plugin = plugin;
        Path path = Paths.get(plugin.getDataFolder().toString(), filePath);
        file = path.toFile();

        createIfAbsent();

        fileConfiguration = YamlConfiguration.loadConfiguration(file);
        if (defaultConfig != null) {
            fileConfiguration.addDefaults(defaultConfig);
            fileConfiguration.options().copyDefaults(true);
        }

        save();
    }

    public static ConfigFileWrapper forFile(Plugin plugin, String filePath) {
        return new ConfigFileWrapper(plugin, filePath, null);
    }

    public static ConfigFileWrapper forFileWithDefaults(Plugin plugin, String filePath, Map<String, Object> defaultMap) {
        YamlConfiguration defaults = new YamlConfiguration();
        defaultMap.forEach(defaults::set);
        return new ConfigFileWrapper(plugin, filePath, defaults);
    }

    public static ConfigFileWrapper forFileWithDefaults(Plugin plugin, String filePath, @Nullable Configuration defaultConfig) {
        return new ConfigFileWrapper(plugin, filePath, defaultConfig);
    }

    public FileConfiguration get() {
        return fileConfiguration;
    }

    public void write(Consumer<FileConfiguration> consumer) {
        consumer.accept(fileConfiguration);
        save();
    }

    public void save() {
        createIfAbsent();
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not Save config to " + this.plugin, e);
        }
    }

    public void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    private void createIfAbsent() {
        if (!file.exists()) {
            try {
                Files.createDirectories(file.toPath().getParent());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create directory at " + file.toPath().toString(), e);
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create config at " + file.toPath().toString(), e);
            }
        }
    }

}
