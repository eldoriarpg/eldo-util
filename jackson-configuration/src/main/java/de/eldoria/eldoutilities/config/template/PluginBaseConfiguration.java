/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.config.template;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.eldoria.eldoutilities.config.ConfigKey;
import de.eldoria.eldoutilities.config.parsing.deserializer.LevelDeserializer;
import de.eldoria.eldoutilities.config.parsing.serializer.LevelSerializer;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.logging.Level;

public final class PluginBaseConfiguration {
    public static final ConfigKey<PluginBaseConfiguration> KEY = ConfigKey.of(
            "Base configuration",
            Path.of("base_configuration.yml"),
            PluginBaseConfiguration.class,
            PluginBaseConfiguration::new);
    @JsonProperty
    private int version;
    @JsonProperty
    private String lastInstalledVersion;
    @JsonDeserialize(using = LevelDeserializer.class)
    @JsonSerialize(using = LevelSerializer.class)
    @JsonProperty
    private Level logLevel = Level.INFO;

    public PluginBaseConfiguration() {
    }

    public PluginBaseConfiguration(int version, String lastInstalledVersion, Level level) {
        this.version = version;
        this.lastInstalledVersion = lastInstalledVersion;
        this.logLevel = level;
    }

    public int version() {
        return version;
    }

    public String lastInstalledVersion() {
        return lastInstalledVersion;
    }

    public void version(int version) {
        this.version = version;
    }

    public void lastInstalledVersion(String lastInstalledVersion) {
        this.lastInstalledVersion = lastInstalledVersion;
    }

    public void lastInstalledVersion(Plugin plugin) {
        this.lastInstalledVersion = plugin.getDescription().getVersion();
    }

    public Level logLevel() {
        return logLevel;
    }

    public void logLevel(Level logLevel) {
        this.logLevel = logLevel;
    }
}
