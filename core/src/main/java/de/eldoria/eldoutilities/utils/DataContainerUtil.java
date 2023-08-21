/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

/**
 * Utilities to work with the {@link PersistentDataContainer}
 */
public final class DataContainerUtil {
    private DataContainerUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Sets a value in a data container if it is absent.
     *
     * @param holder holder of the {@link PersistentDataContainer}
     * @param key    key to set
     * @param type   type of key
     * @param value  value of key
     * @param <T>    type of key
     * @param <Z>    type of value
     */
    public static <T, Z> void setIfAbsent(@Nullable PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        if (holder == null) return;

        var container = holder.getPersistentDataContainer();
        if (container.has(key, type)) return;

        container.set(key, type, value);
    }

    /**
     * Sets a value in a data container if it is absent.
     *
     * @param holder holder of the {@link PersistentDataContainer}
     * @param key    key to set
     * @param type   type of key
     * @param value  value of key
     * @param <T>    type of key
     * @param <Z>    type of value
     */
    public static <T, Z> void setIfAbsent(@Nullable ItemStack holder, NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        if (holder == null) return;

        var itemMeta = holder.getItemMeta();
        setIfAbsent(itemMeta, key, type, value);
        holder.setItemMeta(itemMeta);
    }

    /**
     * Compute a value in a {@link PersistentDataContainer} based on the current value
     *
     * @param holder holder of the {@link PersistentDataContainer}
     * @param key    key to compute
     * @param type   type of key
     * @param map    map the current value to the new value. Current value is null if key is not set.
     * @param <T>    type of value
     * @param <Z>    type of value
     * @return the mapped value. can be null if mapping function returns null or holder is null
     */
    public static @Nullable <T, Z> Z compute(@Nullable PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type, Function<@Nullable Z, Z> map) {
        if (holder == null) return null;

        var container = holder.getPersistentDataContainer();
        if (!container.has(key, type)) {
            container.set(key, type, map.apply(null));
            return container.get(key, type);
        }
        container.set(key, type, map.apply(container.get(key, type)));
        return container.get(key, type);
    }

    /**
     * Gets the {@link PersistentDataContainer} from item meta, computes the value by calling {@link
     * #compute(PersistentDataHolder, NamespacedKey, PersistentDataType, Function)} and applies the meta afterward
     * again.
     *
     * @param holder item stack to change. This causes the item meta to be reapplied after computation of the value.
     * @param key    key to compute
     * @param type   type of key
     * @param map    map the current value to the new value. Current value is null if key is not set.
     * @param <T>    type of value
     * @param <Z>    type of value
     * @return the mapped value. can be null if mapping function returns null or holder is null
     */
    public static @Nullable <T, Z> Z compute(@Nullable ItemStack holder, NamespacedKey key, PersistentDataType<T, Z> type, Function<Z, Z> map) {
        if (holder == null) return null;

        var itemMeta = holder.getItemMeta();
        var compute = compute(itemMeta, key, type, map);
        holder.setItemMeta(itemMeta);
        return compute;
    }

    /**
     * Compute a value in a {@link PersistentDataContainer} if it is not set.
     *
     * @param holder holder of the {@link PersistentDataContainer}
     * @param key    key to compute
     * @param type   type of key
     * @param value  value which should be set if the key is not present.
     * @param <T>    type of value
     * @param <Z>    type of value
     * @return the value associated with this key. can be null if holder is null.
     */
    @Contract("null, _, _, _ -> null; !null, _, _, _, -> !null")
    public static @Nullable <T, Z> Z computeIfAbsent(@Nullable PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        if (holder == null) return null;

        return compute(holder, key, type, v -> {
            if (v == null) {
                return value;
            }
            return v;
        });
    }

    /**
     * Compute a value in a {@link PersistentDataContainer} if it is not set.
     *
     * @param holder holder of the {@link PersistentDataContainer}
     * @param key    key to compute
     * @param type   type of key
     * @param value  value which should be set if the key is not present.
     * @param <T>    type of value
     * @param <Z>    type of value
     * @return the value associated with this key. can be null if holder is null.
     */
    @Contract("null, _, _, _ -> null; !null, _, _, _, -> !null")
    public static @Nullable <T, Z> Z computeIfAbsent(@Nullable ItemStack holder, NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        if (holder == null) return null;
        var itemMeta = holder.getItemMeta();
        var z = computeIfAbsent(itemMeta, key, type, value);
        holder.setItemMeta(itemMeta);

        return z;
    }

    /**
     * Compute a value in a {@link PersistentDataContainer} if is set.
     *
     * @param holder          holder of the {@link PersistentDataContainer}
     * @param key             key to compute
     * @param type            type of key
     * @param mappingFunction function to map the current value to the new value.
     * @param <T>             type of value
     * @param <Z>             type of value
     * @return the value associated with this key. can be null if holder is null.
     */
    @Contract("null, _, _, _ -> null; !null, _, _, _, -> !null")
    public static @Nullable <T, Z> Z computeIfPresent(@Nullable PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type, Function<Z, Z> mappingFunction) {
        if (holder == null) return null;

        return compute(holder, key, type, v -> {
            if (v != null) {
                return mappingFunction.apply(v);
            }
            return null;
        });
    }

    /**
     * Compute a value in a {@link PersistentDataContainer} if is set.
     *
     * @param holder          holder of the {@link PersistentDataContainer}
     * @param key             key to compute
     * @param type            type of key
     * @param mappingFunction function to map the current value to the new value.
     * @param <T>             type of value
     * @param <Z>             type of value
     * @return the value associated with this key. can be null if holder is null.
     */
    @Contract("null, _, _, _ -> null; !null, _, _, _, -> !null")
    public static @Nullable <T, Z> Z computeIfPresent(@Nullable ItemStack holder, NamespacedKey key, PersistentDataType<T, Z> type, Function<Z, Z> mappingFunction) {
        if (holder == null) return null;

        var itemMeta = holder.getItemMeta();
        var z = computeIfPresent(itemMeta, key, type, mappingFunction);
        holder.setItemMeta(itemMeta);

        return z;
    }

    /**
     * Get a value from a persistent data holder.
     *
     * @param holder holder of the {@link PersistentDataContainer}
     * @param key    key to get
     * @param type   type of key
     * @param <T>    type of value
     * @param <Z>    type of value
     * @return result wrapped in an optional if present.
     */
    public static <T, Z> Optional<Z> get(@Nullable PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type) {
        if (holder == null) return Optional.empty();

        var container = holder.getPersistentDataContainer();
        if (container.has(key, type)) {
            return Optional.ofNullable(container.get(key, type));
        }
        return Optional.empty();
    }

    /**
     * Get a value from a persistent data holder.
     *
     * @param holder holder of the {@link PersistentDataContainer}
     * @param key    key to get
     * @param type   type of key
     * @param <T>    type of value
     * @param <Z>    type of value
     * @return result wrapped in an optional if present.
     */
    public static <T, Z> Optional<Z> get(@Nullable ItemStack holder, NamespacedKey key, PersistentDataType<T, Z> type) {
        if (holder == null) return Optional.empty();
        return get(holder.getItemMeta(), key, type);
    }

    /**
     * Get a value from a persistent data holder.
     *
     * @param holder       holder of the {@link PersistentDataContainer}
     * @param key          key to get
     * @param type         type of key
     * @param defaultValue default value if key is absent
     * @param <T>          type of value
     * @param <Z>          type of value
     * @return result wrapped in an optional if present.
     */
    public static <T, Z> Z getOrDefault(@Nullable PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type, Z defaultValue) {
        return get(holder, key, type).orElse(defaultValue);
    }

    /**
     * Get a value from a persistent data holder.
     *
     * @param holder       holder of the {@link PersistentDataContainer}
     * @param key          key to get
     * @param type         type of key
     * @param defaultValue default value if key is absent
     * @param <T>          type of value
     * @param <Z>          type of value
     * @return result wrapped in an optional if present.
     */
    public static <T, Z> Z getOrDefault(@Nullable ItemStack holder, NamespacedKey key, PersistentDataType<T, Z> type, Z defaultValue) {
        if (holder == null) return defaultValue;
        return getOrDefault(holder.getItemMeta(), key, type, defaultValue);
    }

    /**
     * Set a value in a {@link PersistentDataContainer}.
     *
     * @param holder holder of the {@link PersistentDataContainer}
     * @param key    key to set
     * @param type   type of key
     * @param value  value to set
     * @param <T>    type of value
     * @param <Z>    type of value
     */
    public static <T, Z> void putValue(@Nullable PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        if (holder == null) return;
        compute(holder, key, type, v -> value);
    }

    /**
     * Set a value in a {@link PersistentDataContainer}.
     *
     * @param holder holder of the {@link PersistentDataContainer}
     * @param key    key to set
     * @param type   type of key
     * @param value  value to set
     * @param <T>    type of value
     * @param <Z>    type of value
     */
    public static <T, Z> void putValue(@Nullable ItemStack holder, NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        if (holder == null) return;
        var itemMeta = holder.getItemMeta();
        putValue(itemMeta, key, type, value);
        holder.setItemMeta(itemMeta);
    }

    /**
     * Check if a key is present in the {@link PersistentDataContainer}.
     *
     * @param holder holder of the {@link PersistentDataContainer}
     * @param key    key to check
     * @param type   type of key
     * @param <T>    type of value
     * @param <Z>    type of value
     * @return true if the key is set
     */
    public static <T, Z> boolean hasKey(@Nullable PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type) {
        if (holder == null) return false;
        return get(holder, key, type).isPresent();
    }

    /**
     * Check if a key is present in the {@link PersistentDataContainer}.
     *
     * @param holder holder of the {@link PersistentDataContainer}
     * @param key    key to check
     * @param type   type of key
     * @param <T>    type of value
     * @param <Z>    type of value
     * @return true if the key is set
     */
    public static <T, Z> boolean hasKey(@Nullable ItemStack holder, NamespacedKey key, PersistentDataType<T, Z> type) {
        if (holder == null) return false;
        var itemMeta = holder.getItemMeta();
        return hasKey(itemMeta, key, type);
    }

    /**
     * Remove a key from the {@link PersistentDataContainer} if it is set.
     *
     * @param holder holder of the {@link PersistentDataContainer}
     * @param key    key to check
     * @param type   type of key
     * @param <T>    type of value
     * @param <Z>    type of value
     * @return true if the key was present and removed.
     */
    public static <T, Z> boolean remove(@Nullable PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type) {
        if (holder == null) return false;
        var container = holder.getPersistentDataContainer();
        if (container.has(key, type)) {
            container.remove(key);
            return true;
        }
        return get(holder, key, type).isPresent();
    }

    /**
     * Remove a key from the {@link PersistentDataContainer} if it is set.
     *
     * @param holder holder of the {@link PersistentDataContainer}
     * @param key    key to check
     * @param type   type of key
     * @param <T>    type of value
     * @param <Z>    type of value
     * @return true if the key was present and removed.
     */
    public static <T, Z> boolean remove(@Nullable ItemStack holder, NamespacedKey key, PersistentDataType<T, Z> type) {
        if (holder == null) return false;
        var itemMeta = holder.getItemMeta();
        if (remove(itemMeta, key, type)) {
            holder.setItemMeta(itemMeta);
            return true;
        }
        return false;
    }

    /**
     * Converts a byte to boolean.
     *
     * @param aByte byte to convert
     * @return byte as boolean. false if null
     */
    public static boolean byteToBoolean(Byte aByte) {
        return aByte != null && aByte == (byte) 1;
    }

    /**
     * Converts a boolean to a byte.
     *
     * @param aBoolean boolean to convert
     * @return boolean as byte.
     */
    public static byte booleanToByte(boolean aBoolean) {
        return (byte) (aBoolean ? 1 : 0);
    }
}
