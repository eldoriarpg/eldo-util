/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion.builder;

import de.eldoria.eldoutilities.crossversion.ExclusiveVersionRange;
import de.eldoria.eldoutilities.crossversion.VersionRange;
import de.eldoria.eldoutilities.utils.Version;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface to create different function builders.
 */
public abstract class VersionFunctionBuilder<T, V> {
    protected final Map<VersionRange, V> functions = new HashMap<>();

    /**
     * Get a function builder.
     *
     * @param a   first input type class
     * @param r   result type class
     * @param <A> first input type
     * @param <R> result type
     * @return a new function builder instance
     */
    public static <A, R> FunctionBuilder<A, R> functionBuilder(Class<A> a, Class<R> r) {
        return new FunctionBuilder<>();
    }

    /**
     * Get a bi function builder
     *
     * @param a   first input type class
     * @param b   second input type class
     * @param r   result type class
     * @param <A> first input type
     * @param <B> second input type
     * @param <R> result type
     * @return a new bi function builder instance
     */
    public static <A, B, R> BiFunctionBuilder<A, B, R> biFunctionBuilder(Class<A> a, Class<B> b, Class<R> r) {
        return new BiFunctionBuilder<>();
    }

    /**
     * Get a tri function builder.
     *
     * @param a   first input type class
     * @param b   second input type class
     * @param c   third input type class
     * @param r   result type class
     * @param <A> first input type
     * @param <B> second input type
     * @param <C> third input type
     * @param <R> result type
     * @return new tri function builder instance
     */
    public static <A, B, C, R> TriFunctionBuilder<A, B, C, R> triFunctionBuilder(Class<A> a, Class<B> b, Class<C> c, Class<R> r) {
        return new TriFunctionBuilder<>();
    }

    /**
     * Get a quad function builder.
     *
     * @param a   first input type class
     * @param b   second input type class
     * @param c   third input type class
     * @param d   fourth input type class
     * @param r   result type class
     * @param <A> first input type
     * @param <B> second input type
     * @param <C> third input type
     * @param <D> fourth input type
     * @param <R> result type
     * @return new quad function builder instance
     */
    public static <A, B, C, D, R> QuadFunctionBuilder<A, B, C, D, R> quadFunctionBuilder(
            Class<A> a, Class<B> b, Class<C> c, Class<D> d, Class<R> r) {
        return new QuadFunctionBuilder<>();
    }

    /**
     * Add a version function which should be used on one or more versions.
     *
     * @param function function to execute
     * @param version  versions which should use this function
     * @return builder instance with function applied for versions
     */
    public VersionFunctionBuilder<T, V> addVersionFunction(V function, VersionRange version) {
        functions.put(version, function);
        return this;
    }

    /**
     * Add a version functions for all versions between two versions.
     *
     * @param oldest   oldest version (inclusive)
     * @param newest   newest version (exclusive)
     * @param function function to execute
     * @return builder instance with function applied for versions
     */
    public VersionFunctionBuilder<T, V> addVersionFunctionBetween(Version oldest, Version newest, V function) {
        addVersionFunction(function, new ExclusiveVersionRange(oldest, newest));
        return this;
    }

    /**
     * Build the version function.
     *
     * @return version functions with applied functions for versions.
     */
    public abstract T build();
}
