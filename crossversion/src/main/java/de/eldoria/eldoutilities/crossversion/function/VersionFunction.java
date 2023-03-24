/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion.function;

import de.eldoria.eldoutilities.crossversion.UnsupportedVersionException;
import de.eldoria.eldoutilities.crossversion.VersionRange;

import java.util.Map;
import java.util.function.Function;

/**
 * A {@link Function} with version sensitive context.
 *
 * @param <A> input Type
 * @param <R> result Type
 */
public class VersionFunction<A, R> extends BaseVersionFunction<Function<A, R>> {

    public VersionFunction(Map<VersionRange, Function<A, R>> functions) {
        super(functions);
    }

    /**
     * Execute the function for the current version.
     *
     * @param a first parameter of the function.
     * @return value of the function
     * @throws UnsupportedVersionException when no function is defined for the server version.
     */
    public R apply(A a) {
        return get().apply(a);
    }
}
