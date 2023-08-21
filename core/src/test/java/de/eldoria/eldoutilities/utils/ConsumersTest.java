/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

class ConsumersTest {

    @Test
    void emptyConsumer() {
        consumer(Consumers.emptyConsumer());
    }

    @Test
    void emptyBiConsumer() {
        consumer(Consumers.emptyBiConsumer());
    }

    public void consumer(Consumer<String> consumer) {
        new GenericTest<>("", "").consumer(Consumers.emptyConsumer());
    }

    public void consumer(BiConsumer<String, String> consumer) {
        new GenericTest<>("", "").consumer(Consumers.emptyBiConsumer());
        Futures.whenComplete(Consumers.emptyConsumer(), Consumers.emptyConsumer());
    }

    class GenericTest<T, V> {
        private T t;
        private V v;

        public GenericTest(T t, V v) {
            this.t = t;
            this.v = v;
        }

        public void consumer(Consumer<T> consumer) {

        }

        public void consumer(BiConsumer<T, V> consumer) {

        }
    }
}
