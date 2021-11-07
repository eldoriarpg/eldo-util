package de.eldoria.eldoutilities.configuration;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * A simple wrapper to manage File configurations.
 */
public final class ConfigFileWrapper {
    private final File file;
    @Nullable
    private final Plugin plugin;
    private FileConfiguration fileConfiguration;

    private ConfigFileWrapper(@Nullable Plugin plugin, Path filePath, @Nullable Configuration defaultConfig) {
        this.plugin = plugin;
        file = filePath.toFile();

        createIfAbsent();

        fileConfiguration = YamlConfiguration.loadConfiguration(file);
        if (defaultConfig != null) {
            fileConfiguration.addDefaults(defaultConfig);
            fileConfiguration.options().copyDefaults(true);
        }

        save();
    }

    /**
     * Create a config for a file
     *
     * @param plugin   owner of the config
     * @param filePath path to file
     * @return new instance
     */
    public static ConfigFileWrapper forFile(Plugin plugin, String filePath) {
        return new ConfigFileWrapper(plugin, resolvePath(plugin, filePath), null);
    }

    /**
     * Create a config for a file
     *
     * @param plugin   owner of the config
     * @param filePath path to file
     * @return new instance
     */
    public static ConfigFileWrapper forFile(Plugin plugin, Path filePath) {
        return new ConfigFileWrapper(plugin, filePath, null);
    }

    /**
     * Create a config for a file. This file is not owned by any plugin.
     * The path will be relative to the plugin directory itself.
     *
     * @param filePath path to file
     * @return new instance
     */
    public static ConfigFileWrapper forFile(Path filePath) {
        return new ConfigFileWrapper(null, filePath, null);
    }

    /**
     * Create a config for a file
     *
     * @param plugin     owner of the config
     * @param filePath   path to file
     * @param defaultMap a map with default values to set.
     * @return new instance
     */
    public static ConfigFileWrapper forFileWithDefaults(Plugin plugin, String filePath, Map<String, Object> defaultMap) {
        var defaults = new YamlConfiguration();
        defaultMap.forEach(defaults::set);
        return new ConfigFileWrapper(plugin, resolvePath(plugin, filePath), defaults);
    }

    /**
     * Create a config for a file
     *
     * @param plugin     owner of the config
     * @param filePath   path to file
     * @param defaultMap a map with default values to set.
     * @return new instance
     */
    public static ConfigFileWrapper forFileWithDefaults(Plugin plugin, Path filePath, Map<String, Object> defaultMap) {
        var defaults = new YamlConfiguration();
        defaultMap.forEach(defaults::set);
        return new ConfigFileWrapper(plugin, filePath, defaults);
    }

    /**
     * Create a config for a file
     *
     * @param plugin        owner of the config
     * @param filePath      path to file
     * @param defaultConfig a configuration with default values to set.
     * @return new instance
     */
    public static ConfigFileWrapper forFileWithDefaults(Plugin plugin, String filePath, @Nullable Configuration defaultConfig) {
        return new ConfigFileWrapper(plugin, resolvePath(plugin, filePath), defaultConfig);
    }

    /**
     * Create a config for a file
     *
     * @param plugin        owner of the config
     * @param filePath      path to file
     * @param defaultConfig a configuration with default values to set.
     * @return new instance
     */
    public static ConfigFileWrapper forFileWithDefaults(Plugin plugin, Path filePath, @Nullable Configuration defaultConfig) {
        return new ConfigFileWrapper(plugin, filePath, defaultConfig);
    }

    /**
     * Get the file configuration.
     * <p>
     * Should not be cached.
     *
     * @return file configuration
     */
    public FileConfiguration get() {
        return fileConfiguration;
    }

    /**
     * Write data to the config with a consumer
     *
     * @param consumer consumer to apply
     */
    public void write(Consumer<FileConfiguration> consumer) {
        consumer.accept(fileConfiguration);
        save();
    }

    /**
     * Save the config file to disk
     */
    public void save() {
        createIfAbsent();
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            log(Level.SEVERE, "Could not Save config to " + this.plugin, e);
        }
    }

    /**
     * Reload the config file from disk
     */
    public void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    private void createIfAbsent() {
        if (!Files.exists(file.toPath())) {
            try {
                Files.createDirectories(file.toPath().getParent());
            } catch (IOException e) {
                log(Level.SEVERE, "Could not create directory at " + file.toPath(), e);
            }
            try {
                Files.createFile(file.toPath());
            } catch (IOException e) {
                log(Level.SEVERE, "Could not create config at " + file.toPath(), e);
            }
        }
    }

    private void log(Level level, String message, Throwable e) {
        if (plugin == null) {
            Bukkit.getLogger().log(level, "[EldoUtilitites] " + message, e);
        } else {
            plugin.getLogger().log(level, message, e);
        }
    }

    private static Path resolvePath(Plugin plugin, String filePath) {
        if (plugin == null) {
            return Bukkit.getUpdateFolderFile().toPath().getParent().resolve(filePath);
        }
        return plugin.getDataFolder().toPath().resolve(filePath);
    }

}
