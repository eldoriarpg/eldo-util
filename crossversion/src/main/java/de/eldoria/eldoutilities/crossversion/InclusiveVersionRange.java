/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion;

import de.eldoria.eldoutilities.utils.Version;

public record InclusiveVersionRange(Version lower, Version upper) implements VersionRange {
    public boolean contains(Version version) {
        return version.isBetweenInclusive(lower, upper);
    }
}
