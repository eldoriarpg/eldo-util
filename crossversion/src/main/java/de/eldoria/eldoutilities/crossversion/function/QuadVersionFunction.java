/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion.function;

import de.eldoria.eldoutilities.crossversion.UnsupportedVersionException;
import de.eldoria.eldoutilities.crossversion.VersionRange;
import de.eldoria.eldoutilities.functions.QuadFunction;

import java.util.Map;

/**
 * A {@link QuadFunction} with version sensitive context.
 *
 * @param <A> first Input Type
 * @param <B> second Input Type
 * @param <C> third Input Type
 * @param <D> fourth Input Type
 * @param <R> result Type
 */
public class QuadVersionFunction<A, B, C, D, R> extends BaseVersionFunction<QuadFunction<A, B, C, D, R>> {
    public QuadVersionFunction(Map<VersionRange, QuadFunction<A, B, C, D, R>> functions) {
        super(functions);
    }

    /**
     * Execute the function for the current version.
     *
     * @param a first parameter of the function
     * @param b second parameter of the function
     * @param c third parameter of the function
     * @param d fourth parameter of the function
     * @return value of the function
     * @throws UnsupportedVersionException when no function is defined for the server version.
     */
    public R apply(A a, B b, C c, D d) {
        return get().apply(a, b, c, d);
    }
}
