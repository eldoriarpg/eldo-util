/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.functions;

public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
}
