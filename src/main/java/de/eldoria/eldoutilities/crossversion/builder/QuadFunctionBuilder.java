/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion.builder;

import de.eldoria.eldoutilities.crossversion.ServerVersion;
import de.eldoria.eldoutilities.crossversion.function.QuadVersionFunction;
import de.eldoria.eldoutilities.functions.QuadFunction;

import java.util.EnumMap;
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
public class QuadFunctionBuilder<A, B, C, D, R> {
    private final Map<ServerVersion, QuadFunction<A, B, C, D, R>> functions = new EnumMap<>(ServerVersion.class);

    protected QuadFunctionBuilder() {
    }

    /**
     * Add a version function which should be used on one or more versions.
     *
     * @param function function to execute
     * @param version  versions which should use this function
     * @return builder instance with function applied for versions
     */
    public QuadFunctionBuilder<A, B, C, D, R> addVersionFunction(QuadFunction<A, B, C, D, R> function, ServerVersion... version) {
        for (var serverVersion : version) {
            functions.put(serverVersion, function);
        }
        return this;
    }

    /**
     * Add a version functions for all versions between two versions.
     *
     * @param oldest   oldest version (inclusive)
     * @param newest   newest version (inclusive)
     * @param function function to execute
     * @return builder instance with function applied for versions
     */
    public QuadFunctionBuilder<A, B, C, D, R> addVersionFunctionBetween(ServerVersion oldest, ServerVersion newest, QuadFunction<A, B, C, D, R> function) {
        addVersionFunction(function, ServerVersion.versionsBetween(oldest, newest));
        return this;
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
