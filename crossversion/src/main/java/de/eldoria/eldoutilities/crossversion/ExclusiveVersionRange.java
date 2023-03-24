/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion;

import de.eldoria.eldoutilities.utils.Version;

public record ExclusiveVersionRange(Version lower, Version upper) implements VersionRange {
    @Override
    public boolean isBetween(Version version) {
        return version.isBetween(lower, upper);
    }
}
