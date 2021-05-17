package de.eldoria.eldoutilities.serialization;

import de.eldoria.eldoutilities.serialization.wrapper.MapEntry;
import de.eldoria.eldoutilities.utils.EnumUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Map for type resolving.
 *
 * @since 1.0.0
 */
public final class TypeResolvingMap extends AbstractMap<String, Object> {
    private final Map<String, Object> delegate;

    TypeResolvingMap(Map<String, Object> delegate) {
        this.delegate = new LinkedHashMap<>(delegate);
        this.delegate.entrySet().removeIf(e -> e.getValue() == null);
    }

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return delegate.entrySet();
    }

    /**
     * Get a value from map.
     *
     * @param key key
     * @return object or null if key is not present
     */
    @Override
    public Object get(Object key) {
        return this.delegate.get(key);
    }

    /**
     * Get a value from map.
     *
     * @param key key
     * @param <T> type of return value
     * @return object or null if key is not present
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return (T) get(key);
    }

    public <T, V> void listToMap(Map<T, V> map, String key, Function<V, T> valueToKey) {
        List<V> values = getValue(key);
        values.forEach(v -> map.put(valueToKey.apply(v), v));
    }

    /**
     * Get a value from map.
     *
     * @param key          key
     * @param defaultValue default value if key does not exist
     * @param <T>          type of return value
     * @return value of key or default value
     */
    @SuppressWarnings("unchecked")
    public <T> T getValueOrDefault(String key, T defaultValue) {
        return (T) delegate.getOrDefault(key, defaultValue);
    }

    public UUID getValueOrDefault(String key, UUID defaultValue) {
        return getValueOrDefault(key, defaultValue, UUID::fromString);
    }

    /**
     * Get a value from map.
     *
     * @param key          key
     * @param defaultValue default value if key does not exist
     * @param clazz        enum clazz to resolve
     * @param <T>          type of return value
     * @return value of key or default value
     */
    public <T extends Enum<T>> T getValueOrDefault(String key, T defaultValue, Class<T> clazz) {
        return EnumUtil.parse(getValueOrDefault(key, defaultValue.name()), clazz, defaultValue);
    }

    /**
     * Get a value from map.
     *
     * @param key          key
     * @param defaultValue default value if key does not exist
     * @param clazz        enum clazz to resolve
     * @param <T>          type of return value
     * @return value of key or default value
     */
    public <T extends Enum<T>> List<T> getValueOrDefault(String key, List<T> defaultValue, Class<T> clazz) {
        List<String> names = getValue(key);
        if (names == null) return defaultValue;
        return names.stream().map(name -> EnumUtil.parse(name, clazz)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Get a value from map.
     *
     * @param key            key
     * @param defaultValue   default value if key does not exist
     * @param valueConverter Function to parse the string to value
     * @param <T>            type of return value
     * @return value of key or default value
     */
    @SuppressWarnings("unchecked")
    public <T> T getValueOrDefault(String key, T defaultValue, Function<String, T> valueConverter) {
        if (containsKey(key)) {
            return getValue(key, valueConverter);
        }
        return defaultValue;
    }

    /**
     * Converts a map which was saved with {@link SerializationUtil.Builder#addMap(String, Map, BiFunction)} )} to a map.
     * These objects will be wrapped into an {@link MapEntry} object.
     *
     * @param key             key of map
     * @param keyOrValueToKey function to map the key or the value to a key
     * @param <K>             type of key
     * @param <V>             type of value
     * @return map
     * @since 1.3.2
     */
    @SuppressWarnings("unchecked")
    public @NotNull <K, V> Map<K, V> getMap(String key, BiFunction<String, V, K> keyOrValueToKey) {
        List<MapEntry> mapObjects = getValue(key);
        HashMap<K, V> results = new HashMap<>();
        if (mapObjects == null) {
            return results;
        }

        mapObjects.stream().forEach(e -> results.put(keyOrValueToKey.apply(e.getKey(), (V) e.getObject()), (V) e.getObject()));
        return results;
    }

    /**
     * Get a value from map.
     *
     * @param key            key
     * @param valueConverter Function to parse the string to value.
     * @param <T>            type of return value
     * @return converted string or null if key is not present.
     */
    public <T> T getValue(String key, Function<String, T> valueConverter) {
        String value = getValue(key);
        return value == null ? null : valueConverter.apply(value);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }
}