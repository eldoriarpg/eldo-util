/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
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
