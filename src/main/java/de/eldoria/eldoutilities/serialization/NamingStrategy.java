/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.serialization;

/**
 * Interface to define naming strategies.
 *
 * @since 1.0.0
 */
public interface NamingStrategy {

    String adapt(Class<?> type);
}