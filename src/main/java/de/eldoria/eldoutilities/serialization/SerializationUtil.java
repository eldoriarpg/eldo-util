package de.eldoria.eldoutilities.serialization;

import de.eldoria.eldoutilities.core.EldoUtilities;
import de.eldoria.eldoutilities.serialization.wrapper.MapEntry;
import de.eldoria.eldoutilities.utils.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Class which provides utilities for serialization and deserialization.
 *
 * @since 1.0.0
 */
public final class SerializationUtil {
    private static final NamingStrategy NAMING_STRATEGY = new KebabNamingStrategy();

    private SerializationUtil() {

    }

    /**
     * Creates a new serialization map builder.
     *
     * @return builder for serialization
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Creates a new serialization map builder based on a map.
     *
     * @param map map which should be used to append the new values
     * @return builder for serialization
     */
    public static Builder newBuilder(Map<String, Object> map) {
        return new Builder(map);
    }

    public static <T, U> BiFunction<T, U, String> keyToString() {
        return (k, v) -> k.toString();
    }

    public static <T, U> BiFunction<T, U, Object> valueOnly(Function<U, ?> valueFunction) {
        return (k, v) -> valueFunction.apply(v);
    }

    public static <T, U> BiFunction<T, U, String> keyToPrefixedString(String prefix) {
        return (k, v) -> prefix + k.toString();
    }

    /**
     * Creates a new type resolving map from a map.
     *
     * @param serialized object as map
     * @return type resolving map
     */
    public static TypeResolvingMap mapOf(Map<String, Object> serialized) {
        return new TypeResolvingMap(serialized);
    }

    /**
     * Converts an Object to a Map.
     * <p>
     * This is done by retrieving all fields which are not marked as transient.
     * This will include fields which are defined by a inherited class as well.
     * <p>
     * the variable names are used as keys.
     *
     * @param obj object to map
     * @return object as map
     * @since 1.1.0
     */
    public static Map<String, Object> objectToMap(Object obj) {
        var builder = newBuilder();
        var declaredFields = ReflectionUtil.getAllFields(obj);
        for (var declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if (!Modifier.isTransient(declaredField.getModifiers())) {
                try {
                    builder.add(declaredField.getName(), declaredField.get(obj));
                } catch (IllegalAccessException e) {
                    EldoUtilities.logger().log(Level.WARNING, "Could not access field " + declaredField.getName(), e);
                }
            }
        }
        return builder.build();
    }

    /**
     * Maps objects from a map on an object.
     * <p>
     * Only fields which are not mapped as transient will be mapped.
     * <p>
     * Fields are identified by their name. Fields are only set when a key is present in the map.
     *
     * @param objectMap mappings of object content
     * @param obj       object to map the objects from map
     * @param <T>       type of object
     * @since 1.1.0
     */
    public static <T> void mapOnObject(Map<String, Object> objectMap, T obj) {
        var declaredFields = ReflectionUtil.getAllFields(obj);
        for (var declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if (!Modifier.isTransient(declaredField.getModifiers())) {
                if (!objectMap.containsKey(declaredField.getName())) continue;
                try {
                    declaredField.set(obj, objectMap.get(declaredField.getName()));
                } catch (IllegalAccessException e) {
                    EldoUtilities.logger().log(Level.WARNING, "Could not access field " + declaredField.getName(), e);
                }
            }
        }
    }


    public static final class Builder {
        private final Map<String, Object> serialized;

        public Builder() {
            serialized = new LinkedHashMap<>();
        }

        public Builder(Map<String, Object> map) {
            serialized = new LinkedHashMap<>(map);
        }

        /**
         * Adda a key with a object.
         *
         * @param key   key
         * @param value value to add
         * @return builder with values changed
         */
        public Builder add(String key, Object value) {
            this.serialized.put(key, value);
            return this;
        }

        /**
         * @param key      key
         * @param value    value to add
         * @param toString method to convert value to string
         * @param <T>      type of value
         * @return builder with values changed
         */
        public <T> Builder add(String key, T value, Function<T, String> toString) {
            return add(key, toString.apply(value));
        }

        public Builder add(String key, Map<?, ?> value) {
            return add(key, new ArrayList<>(value.values()));
        }

        /**
         * Serialize a map to a list.
         * <p>
         * The entries will be wrapped in a {@link MapEntry} which preserves key and value.
         * <p>
         * Serialize via {@link TypeResolvingMap#getMap(String, BiFunction)}
         *
         * @param key         key for map
         * @param map         map to serialize
         * @param keyToString function to map the key or value to a unique key.
         * @param <K>         type of key
         * @param <V>         type of value
         * @return builder with values changed
         */
        public <K, V> Builder addMap(String key, Map<K, V> map, BiFunction<K, V, String> keyToString) {
            return add(key, map.entrySet().stream()
                    .map(e -> new MapEntry(keyToString.apply(e.getKey(), e.getValue()), e.getValue()))
                    .collect(Collectors.toList()));
        }

        /**
         * Add a key with a enum constant name
         *
         * @param key       key
         * @param enumValue enum value
         * @return builder with values changed
         */
        public Builder add(String key, Enum<?> enumValue) {
            return add(key, enumValue.name());
        }

        /**
         * Adds a key with a collection which will be wrapped in a list.
         *
         * @param key        key
         * @param collection collection
         * @return builder with values changed
         */
        public Builder add(String key, Collection<?> collection) {
            this.serialized.put(key, new ArrayList<>(collection)); // serialize collection as list
            return this;
        }

        /**
         * Adds a object. The key will be computed by the current naming strategy.
         *
         * @param value value to add
         * @return builder with values changed
         */
        public Builder add(Object value) {
            return add(NAMING_STRATEGY.adapt(value.getClass()), value);
        }

        /**
         * Adds a enum value. The key will be computed by the current naming strategy.
         *
         * @param enumValue value to add
         * @return builder with values changed
         */
        public Builder add(Enum<?> enumValue) {
            return add(NAMING_STRATEGY.adapt(enumValue.getClass()), enumValue);
        }

        public Builder add(String key, UUID uuid) {
            return add(key, uuid.toString());
        }

        /**
         * Add a map to the map
         *
         * @param map           map to add
         * @param keyFunction   function to map key to string
         * @param valueFunction function to map value to object
         * @param <K>           key type
         * @param <V>           value type
         * @return builder with values changed
         */
        public <K, V> Builder add(Map<K, V> map, BiFunction<K, V, String> keyFunction,
                                  BiFunction<K, V, Object> valueFunction) {
            map.forEach((k, v) -> add(keyFunction.apply(k, v), valueFunction.apply(k, v)));
            return this;
        }

        public <T extends Enum<?>> Builder addEnum(String key, Collection<T> values) {
            return add(key, values.stream().map(Enum::name).collect(Collectors.toCollection((Supplier<Collection<String>>) ArrayList::new)));
        }

        /**
         * Add a map to the serialization map
         *
         * @param map map to add
         * @return builder with values changed
         */
        public Builder add(Map<String, Object> map) {
            map.forEach(this::add);
            return this;
        }

        public Map<String, Object> build() {
            return this.serialized;
        }
    }
}
