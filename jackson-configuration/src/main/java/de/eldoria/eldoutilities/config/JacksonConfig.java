/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.eldoria.eldoutilities.config.exceptions.ConfigurationException;
import de.eldoria.eldoutilities.config.template.PluginBaseConfiguration;
import de.eldoria.jacksonbukkit.JacksonBukkit;
import de.eldoria.jacksonbukkit.JacksonPaper;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 * Class allowing to manage multiple configuration files
 *
 * @param <T> type of main configuration
 */
public abstract class JacksonConfig<T> {
    private final Plugin plugin;
    private final ConfigKey<T> mainKey;
    private final Map<ConfigKey<?>, Object> files = new HashMap<>();
    private ObjectMapper mapper;
    private ObjectMapper writer;
    private ObjectMapper reader;

    /**
     * Creates a new Jackson Configuration
     *
     * @param plugin  plugin owning the configuration
     * @param mainKey key for the main configuration file former config.yml
     */
    public JacksonConfig(@NotNull Plugin plugin, @NotNull ConfigKey<T> mainKey) {
        this.plugin = plugin;
        this.mainKey = mainKey;
        secondary(PluginBaseConfiguration.KEY);
    }

    /**
     * Plugin associated with this configuration
     *
     * @return plugin instance
     */
    public Plugin plugin() {
        return plugin;
    }

    /**
     * Get the primary configuration.
     * <p>
     * This will be the config.yml in most cases.
     * <p>
     * If the config was not yet created, it will be created.
     *
     * @return configuration
     */
    public T main() {
        return secondary(mainKey);
    }

    /**
     * Get a configuration file.
     * <p>
     * If this file was not yet created, it will be created.
     *
     * @param key configuration key
     * @param <V> type of configuration
     * @return configuration file
     */
    public <V> V secondary(ConfigKey<V> key) {
        return (V) files.computeIfAbsent(key, k -> createAndLoad(key));
    }

    /**
     * Get the primary configuration wrapper.
     * <p>
     * This wrapper can be used to save the config using the closable.
     * It is also safe to be stored since it does not store the file itself.
     * <p>
     * This will be the config.yml in most cases.
     * <p>
     * If the config was not yet created, it will be created.
     *
     * @return configuration
     */
    public Wrapper<T> mainWrapped() {
        return Wrapper.of(mainKey, this);
    }

    /**
     * Get a configuration file.
     * <p>
     * This wrapper can be used to save the config using the closable.
     * It is also safe to be stored since it does not store the file itself.
     * <p>
     * If this file was not yet created, it will be created.
     *
     * @param key configuration key
     * @param <V> type of configuration
     * @return configuration file
     */
    public <V> Wrapper<V> secondaryWrapped(ConfigKey<V> key) {
        return Wrapper.of(key, this);
    }

    /**
     * Replace the configuration currently associated with this key with a new configuration.
     *
     * @param key      configuration key
     * @param newValue new value of key
     * @param <V>      type of key
     */
    public <V> void replace(ConfigKey<V> key, V newValue) {
        files.put(key, newValue);
    }

    /**
     * Saves all files loaded via this instance.
     */
    public void save() {
        for (var configKey : files.keySet()) {
            save(configKey);
        }
    }

    /**
     * Saves the file associated with the config key
     *
     * @param key configuration key
     */
    public void save(ConfigKey<?> key) {
        write(resolvePath(key), files.get(key));
    }

    /**
     * Relaods all files loaded via this instance including the main configuration.
     */
    public void reload() {
        // We will modify the collection. Therefore, we need to copy first.
        for (var key : new HashSet<>(files.keySet())) {
            reload(key);
        }
    }

    /**
     * Reloads a single file associated with the config key
     *
     * @param key configuration key
     */
    public void reload(ConfigKey<?> key) {
        files.put(key, createAndLoad(key));
    }

    /**
     * Get the object mapper used to read files
     *
     * @return object mapper instance
     */
    public final ObjectMapper reader() {
        if (reader == null) {
            reader = createReadMapper();
        }
        return reader;
    }

    /**
     * Get the mapper used to write objects
     *
     * @return object mapper instance
     */
    public final ObjectMapper writer() {
        if (writer == null) {
            writer = createWriteMapper();
        }
        return writer;
    }

    /**
     * Get the object mapper used to read and write objects
     *
     * @return object mapper instance
     */
    public final ObjectMapper mapper() {
        if (mapper == null) {
            mapper = createMapper();
        }
        return mapper;
    }

    /**
     * Create a mapper for reading files.
     *
     * @return mapper instance
     */
    protected ObjectMapper createReadMapper() {
        return mapper();
    }

    /**
     * Create a mapper for writing files.
     *
     * @return mapper instance
     */
    protected ObjectMapper createWriteMapper() {
        return mapper();
    }

    /**
     * Load a file defined in the configuration key.
     * <p>
     * Will fail if the file is not present.
     * <p>
     * Use {@link #createAndLoad(ConfigKey)} if you want the file to be created.
     *
     * @param key configuration key
     * @param <V> type of file
     * @return instance of file
     */
    protected final <V> V load(ConfigKey<V> key) {
        if (!resolvePath(key).toFile().exists()) return null;
        try {
            return read(resolvePath(key), key.configClazz());
        } catch (ConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load configuration file.", e);
            backup(key);
            plugin.getLogger().log(Level.WARNING, "Recreating default config");
            write(resolvePath(key), key.initValue().get());
        }
        return key.initValue().get();
    }

    /**
     * Load a file defined in the configuration key.
     * <p>
     * If this file was not yet created, it will be created.
     *
     * @param key configuration key
     * @param <V> type of file
     * @return instance of file
     */
    protected final <V> V createAndLoad(ConfigKey<V> key) {
        var path = resolvePath(key);
        if (!path.toFile().exists()) {
            plugin.getLogger().info("Configuration file: " + path + " does not exist. Creating.");
            write(path, key.initValue().get());
        }
        return load(key);
    }

    /**
     * Create a general mapper for read and write.
     * <p>
     * You can define different wrapper for read and write operations by overwriting {@link #createReadMapper()} and {@link #createWriteMapper()}
     *
     * @return mapper instance
     */
    protected ObjectMapper createMapper() {
        return YAMLMapper.builder()
                .addModule(getPlatformModule())
                // This is very important when using polymorphism and library loader feature.
                .typeFactory(TypeFactory.defaultInstance().withClassLoader(plugin.getClass().getClassLoader()))
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .build()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
    }

    /**
     * Get the module for the current platform.
     *
     * @return module
     */
    protected final Module getPlatformModule() {
        if (plugin.getServer().getName().toLowerCase(Locale.ROOT).contains("spigot")) {
            return getBukkitModule();
        }
        return getPaperModule();
    }

    /**
     * Create the module used for paper server.
     * <p>
     * This should be a {@link JacksonPaper} module.
     *
     * @return paper module
     */
    protected JacksonPaper getPaperModule() {
        return JacksonPaper.builder()
                .colorAsHex()
                .build();
    }

    /**
     * Create the module used for spigot/bukkit server.
     * <p>
     * This should be a {@link JacksonBukkit} module.
     *
     * @return spigot/bukkit module
     */
    protected JacksonBukkit getBukkitModule() {
        return JacksonBukkit.builder()
                .colorAsHex()
                .build();
    }

    private void backup(ConfigKey<?> key) {
        var target = resolvePath(key);
        var backupName = "backup_" + DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm").format(LocalDateTime.now()) + "_" + target.getFileName();
        plugin.getLogger().log(Level.WARNING, "Backing up " + target + " to " + backupName);
        try {
            Files.move(target, target.getParent().resolve(backupName));
            plugin.getLogger().log(Level.SEVERE, "Backup done.");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create backup.");
        }
    }

    private void write(Path path, Object object) {
        try {
            if (object instanceof ConfigSubscriber sub) {
                sub.preWrite(this);
            }
            // We do this to avoid wiping a file on serialization error.
            Files.writeString(path, writer().writeValueAsString(object));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not write configuration file to " + path, e);
            throw new ConfigurationException("Could not write configuration file to " + path, e);
        }
    }

    private <V> V read(Path path, Class<V> clazz) {
        try {
            V v = reader().readValue(path.toFile(), clazz);
            if (v instanceof ConfigSubscriber sub) {
                sub.postRead(this);
            }
            return v;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not read configuration file from " + path, e);
            throw new ConfigurationException("Could not read configuration file from " + path, e);
        }
    }

    private Path resolvePath(ConfigKey<?> key) {
        return key.path().isAbsolute() ? key.path() : plugin.getDataFolder().toPath().resolve(key.path());
    }
}
