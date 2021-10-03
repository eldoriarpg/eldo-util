package de.eldoria.eldoutilities.serialization;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SampleImplementation {
    // We implement the ConfigurationSerializable interface
    @SerializableAs("SerializableClass")
    public class SerializableClass implements ConfigurationSerializable {
        // We add a default value to all values
        private int someInt = 1;
        private String someString = "Hewo";
        private NestedClass nestedClass = new NestedClass();

        public SerializableClass() {
        }

        // Constructor to map the map on your object
        // This constructor is called by bukkit to deserialize your object.
        public SerializableClass(Map<String, Object> objectMap) {
            TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
            // We use the default value from above as a default value.
            // This way we dont have to care about missing keys.
            someInt = map.getValueOrDefault("someInt", someInt);
            someString = map.getValueOrDefault("someString", someString);
            nestedClass = map.getValueOrDefault("nestedClass", nestedClass);
        }

        // Method to serialize your object on a map.
        // Please not that we just throw our nested class in here
        @Override
        public @NotNull Map<String, Object> serialize() {
            return SerializationUtil.newBuilder()
                    .add("someInt", someInt)
                    .add("someString", someString)
                    .add("nestedClass", nestedClass)
                    .build();
        }
    }

    @SerializableAs("NestedClass")
    public class NestedClass implements ConfigurationSerializable {
        private String someString = "Amazing";
        private String someOtherString = "Much default";

        public NestedClass(Map<String, Object> objectMap) {
            TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
            someString = map.getValueOrDefault("someInt", someString);
            someOtherString = map.getValueOrDefault("someInt", someOtherString);
        }

        public NestedClass() {
        }

        @Override
        public @NotNull Map<String, Object> serialize() {
            return SerializationUtil.newBuilder()
                    .add("someString", someString)
                    .add("someOtherString", someOtherString)
                    .build();
        }
    }
}
