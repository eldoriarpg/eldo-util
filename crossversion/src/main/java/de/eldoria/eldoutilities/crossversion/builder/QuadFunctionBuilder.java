/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion.builder;

import de.eldoria.eldoutilities.crossversion.VersionRange;
import de.eldoria.eldoutilities.crossversion.function.QuadVersionFunction;
import de.eldoria.eldoutilities.functions.QuadFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * A builder for a  {@link QuadVersionFunction} with version sensitive context.
 *
 * @param <A> first Input Type
 * @param <B> second Input Type
 * @param <C> third Input Type
 * @param <D> fourth Input Type
 * @param <R> result Type
 */
public class QuadFunctionBuilder<A, B, C, D, R> extends VersionFunctionBuilder<QuadVersionFunction<A, B, C, D, R>, QuadFunction<A, B, C, D, R>> {
    private final Map<VersionRange, QuadFunction<A, B, C, D, R>> functions = new HashMap<>();

    protected QuadFunctionBuilder() {
    }

    /**
     * Build the version function.
     *
     * @return version functions with applied functions for versions.
     */
    public QuadVersionFunction<A, B, C, D, R> build() {
        return new QuadVersionFunction<>(functions);
    }
}
