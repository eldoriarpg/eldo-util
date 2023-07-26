/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion.builder;

import de.eldoria.eldoutilities.crossversion.VersionRange;
import de.eldoria.eldoutilities.crossversion.function.VersionFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A builder for a  {@link VersionFunction} with version sensitive context.
 *
 * @param <A> first Input Type
 * @param <R> result Type
 */
public class FunctionBuilder<A, R> extends VersionFunctionBuilder<VersionFunction<A, R>, Function<A, R>> {
    private final Map<VersionRange, Function<A, R>> functions = new HashMap<>();

    protected FunctionBuilder() {
    }

    /**
     * Build the version function.
     *
     * @return version functions with applied functions for versions.
     */
    public VersionFunction<A, R> build() {
        return new VersionFunction<>(functions);
    }
}
