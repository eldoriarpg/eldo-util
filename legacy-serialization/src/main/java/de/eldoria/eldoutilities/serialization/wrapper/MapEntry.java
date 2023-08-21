/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.serialization.wrapper;

import de.eldoria.EldoUtilities;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.util.PluginSerializationName;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@PluginSerializationName("{plugin}MapEntry")
public class MapEntry implements ConfigurationSerializable {
    private String key;
    private Object object;

    public MapEntry(String key, Object object) {
        this.key = key;
        this.object = object;
    }

    public MapEntry(Map<String, Object> objectMap) {
        var map = SerializationUtil.mapOf(objectMap);

        objectMap.forEach((k, v) -> EldoUtilities.logger().finer("Key: " + k + " | Value : " + v));

        key = map.getValue("key");
        object = map.getValue("object");
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("key", key)
                .add("object", object)
                .build();
    }

    public String getKey() {
        return key;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public String toString() {
        return "MapEntry{" +
               "key='" + key + '\'' +
               ", object=" + object +
               '}';
    }
}
