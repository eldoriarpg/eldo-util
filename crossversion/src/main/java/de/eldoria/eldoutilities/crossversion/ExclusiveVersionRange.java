/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
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
