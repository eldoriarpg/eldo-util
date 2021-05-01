package de.eldoria.eldoutilities.serialization;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SimpleSampleImplementation {
    // We implement the ConfigurationSerializable interface
    @SerializableAs("SerializableClass")
    public class SerializableClass implements ConfigurationSerializable {
        // We add a default value to all values
        private int someInt = 1;
        private String someString = "Hewo";
        private NestedClass nestedClass = new NestedClass();
        private transient int ignoreMe = 0;

        public SerializableClass() {
        }

        // Constructor to map the map on your object
        // This constructor is called by bukkit to deserialize your object.
        public SerializableClass(Map<String, Object> objectMap) {
            // We just call this method. It will map the map values on our object
            SerializationUtil.mapOnObject(objectMap, this);
        }

        // Method to serialize your object on a map.
        // Please not that we just throw our nested class in here
        @Override
        public @NotNull Map<String, Object> serialize() {
            // We just map our object to a map
            // the transient int from above will not be serialized.
            return SerializationUtil.objectToMap(this);
        }
    }

    @SerializableAs("NestedClass")
    public class NestedClass implements ConfigurationSerializable {
        private String someString = "Amazing";
        private String someOtherString = "Much default";

        public NestedClass(Map<String, Object> objectMap) {
            SerializationUtil.mapOnObject(objectMap, this);
        }

        public NestedClass() {
        }

        @Override
        public @NotNull Map<String, Object> serialize() {
            return SerializationUtil.objectToMap(this);
        }
    }
}
