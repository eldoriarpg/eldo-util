/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion;

import de.eldoria.eldoutilities.utils.Version;

/**
 * @deprecated eldo-util targets a single Paper version. This module is removed in 4.0.
 */
@Deprecated(since = "3.6.0", forRemoval = true)
public interface VersionRange {
    @Deprecated(forRemoval = true)
    default boolean isBetween(Version version){
        return contains(version);
    }
    boolean contains(Version version);

    Version lower();

    Version upper();
}
