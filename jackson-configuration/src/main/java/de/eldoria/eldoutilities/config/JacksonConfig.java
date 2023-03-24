package de.eldoria.eldoutilities.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.eldoria.jacksonbukkit.JacksonBukkit;
import de.eldoria.jacksonbukkit.JacksonPaper;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;


public abstract class JacksonConfig<T> {
    private final Plugin plugin;
    private final ConfigKey<T> mainKey;
    private final Map<ConfigKey<?>, Object> files = new HashMap<>();
    private ObjectMapper mapper;

    /**
     * Creates a new Jackson Configuration
     *
     * @param plugin  plugin owning the configuration
     * @param mainKey key for the main configuration file former config.yml
     */
    public JacksonConfig(@NotNull Plugin plugin, @NotNull ConfigKey<T> mainKey) {
        this.plugin = plugin;
        this.mainKey = mainKey;
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
        write(key.path(), files.get(key));
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
        return read(resolvePath(key), key.configClazz());
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
        if (!path.toFile().exists()) write(key.path(), key.initValue());
        return load(key);
    }

    /**
     * Get the object mapper used to read files
     *
     * @return object mapper instance
     */
    protected ObjectMapper reader() {
        return mapper();
    }

    /**
     * Get the mapper used to write objects
     *
     * @return object mapper instance
     */
    protected ObjectMapper writer() {
        return mapper();
    }

    /**
     * Get the object mapper used to read and write objects
     *
     * @return object mapper instance
     */
    protected ObjectMapper mapper() {
        if (mapper == null) {
            mapper = YAMLMapper.builder()
                    .addModule(getPlatformModule())
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                    .build()
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                    .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        }
        return mapper;
    }

    protected Module getPlatformModule() {
        if (plugin.getServer().getName().toLowerCase(Locale.ROOT).contains("spigot")) {
            return getBukkitModule();
        }
        return getPaperModule();
    }

    protected JacksonPaper getPaperModule() {
        return JacksonPaper.builder()
                .colorAsHex()
                .build();
    }

    protected JacksonBukkit getBukkitModule() {
        return JacksonBukkit.builder()
                .colorAsHex()
                .build();
    }

    private void write(Path path, Object object) {
        try {
            writer().writeValue(path.toFile(), object);
        } catch (IOException e) {
            // TODO: handle c:
            throw new RuntimeException(e);
        }
    }

    private <V> V read(Path path, Class<V> clazz) {
        try {
            return reader().readValue(path.toFile(), clazz);
        } catch (IOException e) {
            // TODO: Handle c:
            throw new RuntimeException(e);
        }
    }

    private Path resolvePath(ConfigKey<?> key) {
        return key.path().isAbsolute() ? key.path() : plugin.getDataFolder().toPath().resolve(key.path());
    }
}
