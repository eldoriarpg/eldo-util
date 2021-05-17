package de.eldoria.eldoutilities.serialization.wrapper;

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
        SerializationUtil.mapOnObject(objectMap, this);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.objectToMap(this);
    }

    public String getKey() {
        return key;
    }

    public Object getObject() {
        return object;
    }
}
