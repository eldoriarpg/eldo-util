/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.serialization.wrapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.lang.reflect.Type;
import java.util.Map;

public class SerializeContainer {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(SerializeContainer.class, new SerializeContainerAdapter())
            .create();

    private final Map<String, Object> data;

    private SerializeContainer(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * Create a container from a serializable object.
     *
     * @param obj onbect to serialize
     * @return container holding the object as a map.
     */
    public static SerializeContainer fromObject(ConfigurationSerializable obj) {
        return new SerializeContainer(obj.serialize());
    }

    /**
     * Create a container from a map which was serialized previously
     *
     * @param json json string
     * @return container holding the string as a map
     */
    public static SerializeContainer fromJson(String json) {
        return GSON.fromJson(json, SerializeContainer.class);
    }

    /**
     * Convert a serializable object to a json string
     *
     * @param obj object to serialize
     * @return object as string
     */
    public static String serializeToJson(ConfigurationSerializable obj) {
        return fromObject(obj).toJson();
    }

    /**
     * Convert a json serialized object to a new object instance
     *
     * @param json  json to convert
     * @param clazz clazz to determine the return type
     * @param <T>   type to return
     * @return a new instance of the serialzied object.
     */
    public static <T extends ConfigurationSerializable> T deserializeFromJson(String json, Class<T> clazz) {
        return fromJson(json).toObject(clazz);
    }

    /**
     * Converts the underlying map to the object
     *
     * @param clazz clazz to determine the return type
     * @param <T>   type to return
     * @return a new instance of the serialzied object.
     */
    @SuppressWarnings("unchecked")
    public <T extends ConfigurationSerializable> T toObject(Class<T> clazz) {
        return (T) ConfigurationSerialization.deserializeObject(data, clazz);
    }

    /**
     * Conversts the underlying map to a json string
     *
     * @return map as json string
     */
    public String toJson() {
        return GSON.toJson(this);
    }

    private static class SerializeContainerAdapter implements JsonDeserializer<SerializeContainer> {

        private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
        }.getType();

        @Override
        public SerializeContainer deserialize(JsonElement json, Type typeOfT,
                                              JsonDeserializationContext context) throws JsonParseException {
            var jsonObject = json.getAsJsonObject();

            Map<String, Object> deserializedMap = context.deserialize(jsonObject.get("data"), MAP_TYPE);

            return new SerializeContainer(deserializedMap);
        }
    }
}
