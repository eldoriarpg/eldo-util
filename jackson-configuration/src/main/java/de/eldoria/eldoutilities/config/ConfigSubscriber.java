/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.config;

/**
 * This interface may be implemented by plugins in config classes.
 * <p>
 * It provides hooks for read and write operations.
 */
public interface ConfigSubscriber {
    default void postRead(JacksonConfig<?> config) {
    }

    default void preWrite(JacksonConfig<?> config) {
    }
}
