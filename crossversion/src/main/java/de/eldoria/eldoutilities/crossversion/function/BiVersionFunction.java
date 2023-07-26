/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion.function;

import de.eldoria.eldoutilities.crossversion.UnsupportedVersionException;
import de.eldoria.eldoutilities.crossversion.VersionRange;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * A {@link BiFunction} with version sensitive context.
 *
 * @param <A> first Input Type
 * @param <B> second Input Type
 * @param <R> result Type
 */
public class BiVersionFunction<A, B, R> extends BaseVersionFunction<BiFunction<A, B, R>> {

    public BiVersionFunction(Map<VersionRange, BiFunction<A, B, R>> functions) {
        super(functions);
    }

    /**
     * Execute the function for the current version.
     *
     * @param a first parameter of the function.
     * @param b second parameter of the function
     * @return value of the function
     * @throws UnsupportedVersionException when no function is defined for the server version.
     */
    public R apply(A a, B b) {
        return get().apply(a, b);
    }
}
