/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion;

import de.eldoria.eldoutilities.utils.Version;

public record InclusiveVersionRange(Version lower, Version upper) implements VersionRange {
    public boolean isBetween(Version version) {
        return version.isBetweenInclusive(lower, upper);
    }
}
