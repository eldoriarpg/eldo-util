/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.serialization.wrapper;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class YamlContainer {
    private final YamlConfiguration config;

    private YamlContainer(YamlConfiguration config) {
        this.config = config;
    }

    public static YamlContainer fromYaml(String yaml) throws InvalidConfigurationException {
        var config = new YamlConfiguration();
        config.loadFromString(yaml);
        return new YamlContainer(config);
    }

    public static YamlContainer fromObject(Object object) {
        var config = new YamlConfiguration();
        config.set("object", object);
        return new YamlContainer(config);
    }

    public static String objectToYaml(Object object) {
        return fromObject(object).toYaml();
    }

    public static <T extends ConfigurationSerializable> T yamlToObject(String yaml, Class<T> clazz) throws InvalidConfigurationException {
        return fromYaml(yaml).toObject(clazz);
    }

    public String toYaml() {
        return config.saveToString();
    }

    public <T> T toObject(Class<T> clazz) {
        return config.getObject("object", clazz);
    }
}
