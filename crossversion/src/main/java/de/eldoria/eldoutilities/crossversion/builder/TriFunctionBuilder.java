/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion.builder;

import de.eldoria.eldoutilities.crossversion.VersionRange;
import de.eldoria.eldoutilities.crossversion.function.TriVersionFunction;
import de.eldoria.eldoutilities.functions.TriFunction;

import java.util.HashMap;
import java.util.Map;

public class TriFunctionBuilder<A, B, C, R> extends VersionFunctionBuilder<TriVersionFunction<A, B, C, R>, TriFunction<A, B, C, R>> {
    private final Map<VersionRange, TriFunction<A, B, C, R>> functions = new HashMap<>();

    protected TriFunctionBuilder() {
    }

    /**
     * Build the version function.
     *
     * @return version functions with applied functions for versions.
     */
    public TriVersionFunction<A, B, C, R> build() {
        return new TriVersionFunction<>(functions);
    }
}
