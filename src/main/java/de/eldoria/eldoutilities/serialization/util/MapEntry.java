package de.eldoria.eldoutilities.serialization.util;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Deprecated
//@DelegateDeserialization(de.eldoria.eldoutilities.serialization.wrapper.MapEntry.class)
public class MapEntry extends de.eldoria.eldoutilities.serialization.wrapper.MapEntry {
    @Deprecated
    public MapEntry(String key, Object object) {
        super(key, object);
    }

    public de.eldoria.eldoutilities.serialization.wrapper.MapEntry deserialize(Map<String, Object> objectMap) {
        return new de.eldoria.eldoutilities.serialization.wrapper.MapEntry(objectMap);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.objectToMap(this);
    }
}
