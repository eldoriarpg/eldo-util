/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.serialization;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;

public class SerializationUtilTest {
    private final SerializableClass.NestedObject nestedObject = new SerializableClass.NestedObject(2, "test");
    private final SerializableClass testClass = new SerializableClass(10, "test", nestedObject);
       private final InheritClass inheritClass = new InheritClass();

    @Test
    public void serializationTest() {
        Map<String, Object> stringObjectMap = SerializationUtil.objectToMap(testClass);
        Assertions.assertEquals(3, stringObjectMap.size());
    }

    @Test
    public void deserializationTest() {
        Map<String, Object> stringObjectMap = testClass.serialize();
        stringObjectMap.put("someNestedObject", nestedObject);
        SerializableClass serializableClass = new SerializableClass(stringObjectMap);
        Assertions.assertEquals(testClass, serializableClass);
    }

    @Test
    public void serializeInheritanceTest() {
        Map<String, Object> serialize = inheritClass.serialize();
        Assertions.assertEquals(3, serialize.size());
    }

    @Test
    public void deserializeInheritanceTest() {
        Map<String, Object> serialize = inheritClass.serialize();
        InheritClass inheritClass = new InheritClass(serialize);
        Assertions.assertEquals(this.inheritClass, inheritClass);
    }

    private static class SerializableClass implements ConfigurationSerializable {
        private int someInt = 1;
        private String someString = "test";
        private NestedObject someNestedObject = new NestedObject(2, "test");
        private transient Object unserializedObject = new Object();

        public SerializableClass(int someInt, String someString, NestedObject someNestedObject) {
            this.someInt = someInt;
            this.someString = someString;
            this.someNestedObject = someNestedObject;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SerializableClass that = (SerializableClass) o;
            return someInt == that.someInt &&
                    someString.equals(that.someString) &&
                    someNestedObject.equals(that.someNestedObject);
        }

        @Override
        public int hashCode() {
            return Objects.hash(someInt, someString, someNestedObject);
        }

        public SerializableClass(Map<String, Object> map) {
            SerializationUtil.mapOnObject(map, this);
        }

        @Override
        public @NotNull Map<String, Object> serialize() {
            return SerializationUtil.objectToMap(this);
        }

        private static class NestedObject implements ConfigurationSerializable {
            private int someInt = 2;
            private String someString = "test";

            public NestedObject(int someInt, String someString) {
                this.someInt = someInt;
                this.someString = someString;
            }

            public NestedObject(Map<String, Object> objectMap) {
                SerializationUtil.mapOnObject(objectMap, this);
            }

            @Override
            public @NotNull Map<String, Object> serialize() {
                return SerializationUtil.objectToMap(this);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                NestedObject that = (NestedObject) o;
                return someInt == that.someInt &&
                        someString.equals(that.someString);
            }

            @Override
            public int hashCode() {
                return Objects.hash(someInt, someString);
            }
        }
    }

    private static class InheritClass extends SerializableClass.NestedObject {

        String someOtherString = "another string";

        public InheritClass() {
            super(2, "test");
        }

        public InheritClass(Map<String, Object> objectMap) {
            super(objectMap);
        }

    }
}
