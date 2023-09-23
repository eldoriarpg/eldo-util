/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.pdc;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

/**
 * A wrapper class for {@link PersistentDataContainer}.
 * Provides convenient methods for interacting with the persistent data of an object.
 */
public class PersistentDataWrapper {
    private final PersistentDataContainer container;

    public PersistentDataWrapper(PersistentDataContainer container) {
        this.container = container;
    }

    public static PersistentDataWrapper create(PersistentDataHolder holder) {
        return new PersistentDataWrapper(holder.getPersistentDataContainer());
    }

    /**
     * Sets a value in a data container if it is absent.
     *
     * @param key   key to set
     * @param type  type of key
     * @param value value of key
     * @param <T>   type of key
     * @param <Z>   type of value
     */
    public <T, Z> void setIfAbsent(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        DataContainerUtil.setIfAbsent(container, key, type, value);
    }

    /**
     * Compute a value in a {@link PersistentDataContainer} based on the current value
     *
     * @param key  key to compute
     * @param type type of key
     * @param map  map the current value to the new value. Current value is null if key is not set.
     * @param <T>  type of value
     * @param <Z>  type of value
     * @return the mapped value. can be null if mapping function returns null or holder is null
     */
    public @Nullable <T, Z> Z compute(NamespacedKey key, PersistentDataType<T, Z> type, Function<@Nullable Z, Z> map) {
        return DataContainerUtil.compute(container, key, type, map);
    }

    /**
     * Compute a value in a {@link PersistentDataContainer} if it is not set.
     *
     * @param key   key to compute
     * @param type  type of key
     * @param value value which should be set if the key is not present.
     * @param <T>   type of value
     * @param <Z>   type of value
     * @return the value associated with this key. can be null if holder is null.
     */
    public @Nullable <T, Z> Z computeIfAbsent(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        return DataContainerUtil.computeIfAbsent(container, key, type, value);
    }

    /**
     * Compute a value in a {@link PersistentDataContainer} if is set.
     *
     * @param key             key to compute
     * @param type            type of key
     * @param mappingFunction function to map the current value to the new value.
     * @param <T>             type of value
     * @param <Z>             type of value
     * @return the value associated with this key. can be null if holder is null.
     */
    public @Nullable <T, Z> Z computeIfPresent(NamespacedKey key, PersistentDataType<T, Z> type, Function<Z, Z> mappingFunction) {
        return DataContainerUtil.computeIfPresent(container, key, type, mappingFunction);
    }

    /**
     * Get a value from a persistent data holder.
     *
     * @param key  key to get
     * @param type type of key
     * @param <T>  type of value
     * @param <Z>  type of value
     * @return result wrapped in an optional if present.
     */
    public <T, Z> Optional<Z> get(NamespacedKey key, PersistentDataType<T, Z> type) {
        return DataContainerUtil.get(container, key, type);
    }

    /**
     * Get a value from a persistent data holder.
     *
     * @param key          key to get
     * @param type         type of key
     * @param defaultValue default value if key is absent
     * @param <T>          type of value
     * @param <Z>          type of value
     * @return result wrapped in an optional if present.
     */
    public <T, Z> Z getOrDefault(NamespacedKey key, PersistentDataType<T, Z> type, Z defaultValue) {
        return DataContainerUtil.getOrDefault(container, key, type, defaultValue);
    }

    /**
     * Set a value in a {@link PersistentDataContainer}.
     *
     * @param key   key to set
     * @param type  type of key
     * @param value value to set
     * @param <T>   type of value
     * @param <Z>   type of value
     */
    public <T, Z> void put(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        DataContainerUtil.putValue(container, key, type, value);
    }

    /**
     * Check if a key is present in the {@link PersistentDataContainer}.
     *
     * @param key  key to check
     * @param type type of key
     * @param <T>  type of value
     * @param <Z>  type of value
     * @return true if the key is set
     */
    public <T, Z> boolean hasKey(NamespacedKey key, PersistentDataType<T, Z> type) {
        return DataContainerUtil.get(container, key, type).isPresent();
    }

    /**
     * Remove a key from the {@link PersistentDataContainer} if it is set.
     *
     * @param key       key to check
     * @param type      type of key
     * @param <T>       type of value
     * @param <Z>       type of value
     * @return true if the key was present and removed.
     */
    public <T, Z> boolean remove(NamespacedKey key, PersistentDataType<T, Z> type) {
        return DataContainerUtil.remove(container, key, type);
    }
}
