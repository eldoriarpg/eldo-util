/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Core class of EldoUtilitites.
 * <p>
 * If you want to use anything from here you need to call {@link EldoUtilities#preWarm(Plugin)} onLoad and {@link EldoUtilities#ignite(Plugin)} onEnable.
 * If your plugins extend {@code EldoPlugin} this will be done automatically.
 */
public final class EldoUtilities {
    private static Plugin mainOwner;
    private static Map<Class<? extends Plugin>, Plugin> instanceOwners = new LinkedHashMap<>();
    private static YamlConfiguration configuration;

    private EldoUtilities() {
    }

    public static Logger logger() {
        return Bukkit.getLogger();
    }

    public static void preWarm(Plugin eldoPlugin) {
        instanceOwners.put(eldoPlugin.getClass(), eldoPlugin);
    }

    public static void ignite(Plugin plugin) {
        var path = plugin.getDataFolder().toPath().toAbsolutePath().getParent().resolve(Paths.get("EldoUtilities", "config.yml"));
        configuration = YamlConfiguration.loadConfiguration(path.toFile());
    }

    public static void shutdown() {
    }

    public static YamlConfiguration getConfiguration() {
        if (configuration == null) {
            var config = Bukkit.getUpdateFolderFile().toPath().toAbsolutePath().getParent().resolve(Paths.get("EldoUtilities", "config.yml"));
            configuration = YamlConfiguration.loadConfiguration(config.toFile());
        }
        return configuration;
    }

    public static Plugin getInstanceOwner(Class<? extends Plugin> plugin) {
        return instanceOwners.get(plugin);
    }

    public static void forceInstanceOwner(Plugin plugin) {
        if (mainOwner != null) {
            throw new IllegalStateException("A instance owner is already set");
        }
        mainOwner = plugin;
    }

    public static Plugin getInstanceOwner() {
        if (mainOwner != null) {
            return mainOwner;
        }

        for (var entry : instanceOwners.entrySet()) {
            return entry.getValue();
        }

        throw new IllegalStateException("No instance owner is set but requested");
    }
}
