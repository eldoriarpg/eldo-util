/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.configuration;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * A wrapper class for {@link FileConfiguration}.
 * <p>
 * This class can also be act as a main config which holds references to external configuration files.
 *
 * @since 1.1.0
 */
public abstract class EldoConfig {
    private static final Map<Plugin, EldoConfig> INSTANCE_CONFIG = new HashMap<>();
    protected final Plugin plugin;
    private final Path pluginData;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private FileConfiguration config;

    public EldoConfig(Plugin plugin) {
        this.plugin = plugin;
        INSTANCE_CONFIG.putIfAbsent(plugin, this);
        pluginData = plugin.getDataFolder().toPath();
        config = plugin.getConfig();
        plugin.saveDefaultConfig();
        if (isMainConfig()) {
            init();
        }
        reload();
        save();
    }

    /**
     * Checks if a plugin is in debug state.
     *
     * @return true if plugin is in debug state.
     */
    public static Level getLogLevel(Plugin plugin) {
        // we probably want to load the config before the plugin is enabled.
        // Since we use the configuration serializable we cant load the config directly if the plugin is not enabled.
        String debug;
        if (!plugin.isEnabled()) {
            var dataFolder = plugin.getDataFolder();
            String config;
            try {
                config = String.join("\n", Files.readAllLines(Paths.get(dataFolder.getAbsolutePath(), "config.yml")));
            } catch (FileNotFoundException e) {
                return Level.INFO;
            } catch (IOException e) {
                // We dont know if our logger already exists...
                Bukkit.getLogger().log(Level.INFO, "[EldoUtilities] Could not load config file. Using default log level.");
                return Level.INFO;
            }
            var compile = Pattern.compile("^debug:.?([a-zA-Z].*?)$", Pattern.MULTILINE);
            var matcher = compile.matcher(config);
            if (matcher.find()) {
                debug = matcher.group(1);
            } else {
                debug = "INFO";
            }
        } else {
            debug = plugin.getConfig().getString("debug", "INFO");
        }

        return parseLevel(debug);
    }

    private static Level parseLevel(String level) {
        return switch (level.toUpperCase()) {
            case "OFF" -> Level.OFF;
            case "SEVERE" -> Level.SEVERE;
            case "WARNING" -> Level.WARNING;
            case "DEBUG", "TRUE" -> Level.CONFIG;
            case "FINE" -> Level.FINE;
            case "FINER" -> Level.FINER;
            case "FINEST" -> Level.FINEST;
            case "ALL" -> Level.ALL;
            default -> Level.INFO;
        };
    }

    /**
     * Get the main config.
     * <p>
     * Also refered as the config.yml
     *
     * @return file configuration for the main config.
     */
    public static EldoConfig getMainConfig(Plugin plugin) {
        return INSTANCE_CONFIG.get(plugin);
    }

    /**
     * Saves the config to disk.
     */
    public final void save() {
        saveConfigs();
        writeConfigs();
    }

    /**
     * Write objects to file configs.
     * <p>
     * This message will be called first, when {@link #save()} is called.
     * <p>
     * {@link #writeConfigs()} will be called afterwards.
     */
    protected void saveConfigs() {

    }

    /**
     * Discards any unsaved changes in the config and reloads the config files
     */
    public final void reload() {
        // TODO: This is just here for backwards compatibility reasons after a stupid choice
        readConfigs();
        reloadConfigs();
    }

    /**
     * Invalidates the cached config objects and reloads.
     * <p>
     * Called after {@link #readConfigs()}}.
     * <p>
     * All configs are already reloaded.
     */
    protected void reloadConfigs() {

    }

    private void readConfigs() {
        try {
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            config = plugin.getConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load config.yml.", e);
            backupAndRemoveCorruptedFile(plugin.getDataFolder().toPath().resolve("config.yml"));
            readConfigs();
        }
        setIfAbsent("debug", "INFO");
        for (var entry : configs.entrySet()) {
            loadConfig(Paths.get(entry.getKey()), null, true);
        }
    }

    private void backupAndRemoveCorruptedFile(Path path) {
        var time = DateTimeFormatter.ofPattern("yy-MM-dd_HH.mm.ss").format(LocalDateTime.now());
        var matcher = Pattern.compile("(?<name>.*)\\.(?<type>.+?)$").matcher(path.getFileName().toString());
        String name;
        if (matcher.find()) {
            name = String.format("%s-%s.%s", matcher.group("name"), time, matcher.group("type"));
        } else {
            plugin.getLogger().severe("Could not determine file type.");
            name = path.getFileName() + time + ".backup";
        }
        var newLoc = path.getParent().resolve(name);
        plugin.getLogger().warning("Attempting to backup " + path + " to " + newLoc);
        try {
            Files.copy(path, newLoc, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create backup.", e);
        }
        plugin.getLogger().warning("Created backup at " + newLoc);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not remove old file.");
        }
        plugin.getLogger().warning("Removed corrupted file");
    }

    /**
     * Set a value if not set
     *
     * @param path  path in section
     * @param value value to set
     * @return true if the value was not present and was set.
     */
    protected final boolean setIfAbsent(String path, Object value) {
        if (!config.isSet(path)) {
            config.set(path, value);
            return true;
        }
        return false;
    }

    /**
     * Set a value if not set
     *
     * @param section section
     * @param path    path in section
     * @param value   value to set
     * @return true if the value was not present and was set.
     */
    protected final boolean setIfAbsent(ConfigurationSection section, String path, Object value) {
        if (!section.isSet(path)) {
            section.set(path, value);
            return true;
        }
        return false;
    }

    /**
     * Load a file from a directory inside the plugin directory.
     * <p>
     * Directory and or file will be created if not exits.
     *
     * @param path           path to the file. the file ending .yml is appended by the function
     * @param defaultCreator Creator of config setting, if the file is not present. If the creator is null and the file
     *                       does not exist null will be returned.
     * @param reload         forces to load the file configuration from disk even if it was already loaded
     * @return file configuration or null if something went wrong.
     * @throws ExternalConfigException When load config is invoked on a eldo config which is not the main config.
     */
    protected final FileConfiguration loadConfig(String path, @Nullable Consumer<FileConfiguration> defaultCreator, boolean reload) {
        var configPath = pluginData.resolve(path + ".yml");
        return loadConfig(configPath, defaultCreator, reload);
    }

    /**
     * @param configPath     path to the file.
     * @param defaultCreator Creator of config setting, if the file is not present. If the creator is null and the file
     *                       does not exist null will be returned.
     * @param reload         forces to load the file configuration from disk even if it was already loaded
     * @return File configuration which was already loaded, loaded or created.
     * @throws ExternalConfigException When load config is invoked on a eldo config which is not the main config.
     */
    protected final FileConfiguration loadConfig(Path configPath, @Nullable Consumer<FileConfiguration> defaultCreator, boolean reload) {
        try {
            return loadConfigWrapped(configPath, defaultCreator, reload);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load " + configPath, e);
            backupAndRemoveCorruptedFile(configPath);
            return loadConfig(configPath, defaultCreator, reload);
        }
    }

    private FileConfiguration loadConfigWrapped(Path configPath, @Nullable Consumer<FileConfiguration> defaultCreator, boolean reload) throws IOException, InvalidConfigurationException {
        validateMainConfigEntry();
        var configFile = configPath.toFile();

        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "could not create directory " + configPath.getParent().toString(), e);
            return null;
        }

        if (!configFile.exists()) {
            try {
                Files.createFile(configPath);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not create config.", e);
                return null;
            }

            var config = new YamlConfiguration();
            config.load(configFile);

            if (defaultCreator != null) defaultCreator.accept(config);

            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not save default config.");
                return null;
            }
        }

        if (reload) {
            var load = load(configFile);
            return configs.compute(configPath.toString(), (k, v) -> load);
        }

        if (configs.containsKey(configPath.toString())) {
            return configs.get(configPath.toString());
        }
        var load = load(configFile);
        return configs.computeIfAbsent(configPath.toString(), p -> load);
    }

    private YamlConfiguration load(File file) throws IOException, InvalidConfigurationException {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.load(file);
        return yamlConfiguration;
    }

    private void writeConfigs() {
        plugin.saveConfig();
        for (var entry : configs.entrySet()) {
            var file = Paths.get(entry.getKey()).toFile();
            if (!file.exists()) {
                try {
                    Files.createFile(file.toPath());
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Could not create config.", e);
                    return;
                }
            }

            try {
                entry.getValue().save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not save config " + file.getAbsolutePath(), e);
            }
        }
    }

    public final Plugin getPlugin() {
        return plugin;
    }

    /**
     * Get the underlying file configuration.
     *
     * @return file configuration for the main config.
     */
    public final FileConfiguration getConfig() {
        return config;
    }

    /**
     * Get the config version.
     *
     * @return config version or -1 if not set.
     */
    public final int getVersion() {
        return config.getInt("version", -1);
    }

    /**
     * Set the config version
     *
     * @param version new config version
     * @param save    true to save after set.
     */
    public final void setVersion(int version, boolean save) {
        config.set("version", version);
        if (save) {
            save();
        }
    }

    public Map<String, FileConfiguration> getConfigs() {
        Map<String, FileConfiguration> configs = new LinkedHashMap<>();
        configs.put(Paths.get(plugin.getDataFolder().toPath().toString(), "config.yml").toString(), getMainConfig(plugin).getConfig());
        configs.putAll(this.configs);
        return configs;
    }

    /**
     * Called after constructor and before reload.
     * <p>
     * Intialize everything here.
     */
    protected void init() {
    }

    public boolean isMainConfig() {
        return INSTANCE_CONFIG.get(plugin) == this;
    }

    private void validateMainConfigEntry() {
        if (!isMainConfig()) {
            throw new ExternalConfigException();
        }
    }
}
