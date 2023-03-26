/*
 *     SPDX-License-Identifier: AGPL-3.0-only
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

import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.logging.Level;

public final class PluginBaseConfiguration {
    public static final ConfigKey<PluginBaseConfiguration> KEY = ConfigKey.of(
            "Base configuration",
            Path.of("base_configuration.yml"),
            PluginBaseConfiguration.class,
            (Supplier<PluginBaseConfiguration>) PluginBaseConfiguration::new);
    @JsonProperty
    private int version = 0;
    @JsonProperty
    private String lastInstalledVersion = null;
    @JsonDeserialize(contentUsing = LevelDeserializer.class)
    @JsonSerialize(contentUsing = LevelSerializer.class)
    @JsonProperty
    private String logLevel = "INFO";

    public PluginBaseConfiguration() {
    }

    public PluginBaseConfiguration(int version, String lastInstalledVersion, Level level) {
        this.version = version;
        this.lastInstalledVersion = lastInstalledVersion;
        this.logLevel = level.getName();
    }

    public int version() {
        return version;
    }

    public String lastInstalledVersion() {
        return lastInstalledVersion;
    }
}
