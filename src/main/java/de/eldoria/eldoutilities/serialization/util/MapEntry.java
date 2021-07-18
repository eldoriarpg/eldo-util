package de.eldoria.eldoutilities.serialization.util;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Deprecated
//git a@DelegateDeserialization(de.eldoria.eldoutilities.serialization.wrapper.MapEntry.class)
public class MapEntry implements ConfigurationSerializable {
    private String key;
    private Object object;

    @Deprecated
    public MapEntry(String key, Object object) {
        throw new UnsupportedOperationException("This class is deprecated.");
    }

    public de.eldoria.eldoutilities.serialization.wrapper.MapEntry deserialize(Map<String, Object> objectMap) {
        return new de.eldoria.eldoutilities.serialization.wrapper.MapEntry(objectMap);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.objectToMap(this);
    }
}
