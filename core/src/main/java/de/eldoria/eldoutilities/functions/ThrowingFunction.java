/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.functions;

public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
}
