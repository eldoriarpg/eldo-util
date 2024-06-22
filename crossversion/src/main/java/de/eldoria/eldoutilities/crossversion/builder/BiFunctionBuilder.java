/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion.builder;

import de.eldoria.eldoutilities.crossversion.VersionRange;
import de.eldoria.eldoutilities.crossversion.function.BiVersionFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * A builder for a  {@link BiVersionFunction} with version sensitive context.
 *
 * @param <A> first Input Type
 * @param <B> second Input Type
 * @param <R> result Type
 */
public class BiFunctionBuilder<A, B, R> extends VersionFunctionBuilder<BiVersionFunction<A, B, R>, BiFunction<A, B, R>> {
    protected BiFunctionBuilder() {
    }

    /**
     * Build the version function.
     *
     * @return version functions with applied functions for versions.
     */
    public BiVersionFunction<A, B, R> build() {
        return new BiVersionFunction<>(functions);
    }
}
