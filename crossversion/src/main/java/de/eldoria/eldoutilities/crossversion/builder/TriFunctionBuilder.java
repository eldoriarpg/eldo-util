/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion.builder;

import de.eldoria.eldoutilities.crossversion.VersionRange;
import de.eldoria.eldoutilities.crossversion.function.TriVersionFunction;
import de.eldoria.eldoutilities.functions.TriFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated eldo-util targets a single Paper version. This module is removed in 4.0.
 */
@Deprecated(since = "3.6.0", forRemoval = true)
public class TriFunctionBuilder<A, B, C, R> extends VersionFunctionBuilder<TriVersionFunction<A, B, C, R>, TriFunction<A, B, C, R>> {
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
