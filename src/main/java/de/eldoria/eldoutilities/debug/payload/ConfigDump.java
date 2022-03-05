/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.payload;

import de.eldoria.eldoutilities.configuration.EldoConfig;
import de.eldoria.eldoutilities.debug.DebugSettings;
import de.eldoria.eldoutilities.debug.data.EntryData;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ConfigDump extends EntryData {

    public ConfigDump(String path, String content) {
        super(path, content);
    }

    /**
     * Creates a new config dump. This dump will include external configs as well if the plugin is a {@link EldoCommand}.
     *
     * @param plugin   plugin to dump teh configs
     * @param settings settings for debug dispatching
     * @return configs as an array.
     */
    public static EntryData[] create(Plugin plugin, DebugSettings settings) {
        var root = plugin.getDataFolder().toPath().toAbsolutePath().getParent().getParent();

        var mainConfig = EldoConfig.getMainConfig(plugin);

        Set<String> configs = new LinkedHashSet<>();
        if (mainConfig != null) {
            try {
                mainConfig.save();
            } catch (Exception e) {
                plugin.getLogger().log(Level.CONFIG, "something went wrong while saving the config. Skipping", e);
            }
            configs.addAll(mainConfig.getConfigs().keySet());
        } else {
            configs.add(Paths.get(plugin.getDataFolder().toPath().toString(), "config.yml").toString());
        }

        List<ConfigDump> dumps = new LinkedList<>();
        for (var config : configs) {
            var currentConfig = Paths.get(root.toString(), config).toFile();
            var content = "Could not read";
            if (currentConfig.exists()) {
                try {
                    content = Files.readAllLines(currentConfig.toPath(), StandardCharsets.UTF_8).stream()
                            .collect(Collectors.joining(System.lineSeparator()));
                } catch (IOException e) {
                    plugin.getLogger().info("Could not read config file " + config);
                }
            }
            dumps.add(new ConfigDump(config, content));
        }

        dumps.forEach(e -> e.applyFilter(settings));

        return dumps.toArray(new ConfigDump[0]);
    }
}
