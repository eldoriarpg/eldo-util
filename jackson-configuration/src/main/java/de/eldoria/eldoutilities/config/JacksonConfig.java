/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.config;

import de.eldoria.eldoutilities.debug.ConfigFiles;
import de.eldoria.eldoutilities.debug.DebugDataProvider;
import de.eldoria.eldoutilities.debug.data.EntryData;
import de.eldoria.jacksonbukkit.JacksonPaper;
import dev.chojo.ocular.Configurations;
import dev.chojo.ocular.components.Format;
import dev.chojo.ocular.dataformats.YamlDataFormat;
import dev.chojo.ocular.key.Key;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.MapperBuilder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Configuration files of a plugin, stored as yaml relative to {@link Plugin#getDataFolder()}.
 * <p>
 * Files are managed by ocular's {@link Configurations} and identified by a {@link Key}.
 * This class adds the server specifics: files are read with the plugin class loader, which polymorphism and the
 * library loader rely on, the Paper module of jackson-bukkit is registered, and every loaded file is exposed as debug
 * data unless {@link #excludeFromDebug(Key[])} leaves it out.
 *
 * @param <T> type of main configuration
 */
public class JacksonConfig<T> extends Configurations<T> implements DebugDataProvider {
    private final Plugin plugin;
    private final Set<Key<?>> keys = ConcurrentHashMap.newKeySet();
    private final Set<Key<?>> debugExcluded = ConcurrentHashMap.newKeySet();
    private Format<?, ?> debugFormat;

    /**
     * Creates a new Jackson Configuration
     *
     * @param plugin  plugin owning the configuration
     * @param mainKey key for the main configuration file former config.yml
     */
    public JacksonConfig(@NotNull Plugin plugin, @NotNull Key<T> mainKey) {
        super(
                plugin.getDataFolder().toPath(),
                mainKey,
                List.of(new YamlDataFormat()),
                plugin.getClass().getClassLoader(),
                null);
        this.plugin = plugin;
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
     * Leaves configuration files out of debug reports, for files whose content the report filters can not hide.
     * <p>
     * The files are neither added as debug data of this configuration nor carried as configuration files of the
     * report. Every file is included until it is excluded here.
     *
     * @param keys keys of the files to leave out
     * @since 3.7.0
     */
    public void excludeFromDebug(Key<?>... keys) {
        for (Key<?> key : keys) {
            debugExcluded.add(key);
            ConfigFiles.exclude(plugin, base().resolve(key.path()));
        }
    }

    @Override
    public synchronized <V> V secondary(Key<V> key) {
        keys.add(key);
        return super.secondary(key);
    }

    @Override
    public <V> void replace(Key<V> key, V newValue) {
        keys.add(key);
        super.replace(key, newValue);
    }

    @Override
    public void configure(MapperBuilder<ObjectMapper, ?> builder) {
        super.configure(builder);
        builder.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * Modules registered on every mapper. Overrides adding further modules should include this list.
     *
     * @return list of modules, containing the jackson-bukkit Paper module
     */
    @Override
    public List<JacksonModule> additionalModules() {
        return List.of(getPaperModule());
    }

    /**
     * Create the module used for paper server.
     *
     * @return paper module
     */
    protected JacksonPaper getPaperModule() {
        return JacksonPaper.builder().colorAsHex().build();
    }

    @Override
    public @NotNull EntryData[] getDebugInformations() {
        return keys.stream()
                .filter(key -> !debugExcluded.contains(key))
                .map(this::dump)
                .flatMap(Optional::stream)
                .toArray(EntryData[]::new);
    }

    private Optional<EntryData> dump(Key<?> key) {
        try {
            return Optional.of(
                    new EntryData(key.toString(), debugFormat().writer().writeValueAsString(secondary(key))));
        } catch (JacksonException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not dump config", e);
            return Optional.empty();
        }
    }

    private synchronized Format<?, ?> debugFormat() {
        if (debugFormat == null) {
            debugFormat = new Format<>(new YamlDataFormat(), this);
        }
        return debugFormat;
    }
}
