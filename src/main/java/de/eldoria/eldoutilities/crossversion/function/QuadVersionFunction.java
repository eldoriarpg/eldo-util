/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion.function;

import de.eldoria.eldoutilities.crossversion.ServerVersion;
import de.eldoria.eldoutilities.crossversion.UnsupportedVersionException;
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
public class QuadVersionFunction<A, B, C, D, R> {
    private final Map<ServerVersion, QuadFunction<A, B, C, D, R>> functions;

    public QuadVersionFunction(Map<ServerVersion, QuadFunction<A, B, C, D, R>> functions) {
        this.functions = functions;
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
        var function = functions.get(ServerVersion.CURRENT_VERSION);
        if (function == null) {
            throw new UnsupportedVersionException();
        }
        return function.apply(a, b, c, d);
    }
}
