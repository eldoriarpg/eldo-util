/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Consumers {
    public static <T> Consumer<T> emptyConsumer() {
        return t -> {
        };
    }

    public static <T, U> BiConsumer<T, U> emptyBiConsumer() {
        return (t, u) -> {
        };
    }
}
