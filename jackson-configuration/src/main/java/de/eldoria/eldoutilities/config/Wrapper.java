/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.config;

import java.io.Closeable;

/**
 * Class allowing to access a configuration file.
 * <p>
 * The file itself will be accessed via the underlying configuration, making it stable for reloads.
 * <p>
 * Using this class inside an auto closable will save the file afterwards.
 *
 * @param <T> type of config
 */
public class Wrapper<T> implements Closeable {
    private final ConfigKey<T> key;
    private final JacksonConfig<?> config;

    public Wrapper(ConfigKey<T> key, JacksonConfig<?> config) {
        this.key = key;
        this.config = config;
    }

    public static <V> Wrapper<V> of(ConfigKey<V> key, JacksonConfig<?> config) {
        return new Wrapper<>(key, config);
    }

    /**
     * Get the wrapped config file.
     *
     * @return config file
     */
    public T config() {
        return config.secondary(key);
    }

    @Override
    public void close() {
        config.save(key);
    }
}
