/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion.function;

import de.eldoria.eldoutilities.crossversion.ServerVersion;
import de.eldoria.eldoutilities.crossversion.UnsupportedVersionException;
import de.eldoria.eldoutilities.crossversion.VersionRange;
import de.eldoria.eldoutilities.utils.Version;

import java.util.Map;

public class BaseVersionFunction<V> {
    private final Map<VersionRange, V> functions;

    public BaseVersionFunction(Map<VersionRange, V> functions) {
        this.functions = functions;
    }

    public V get() {
        return get(ServerVersion.CURRENT_VERSION);
    }

    public V get(Version version) {
        var first = functions.entrySet().stream()
                .filter(func -> func.getKey().contains(version))
                .map(Map.Entry::getValue)
                .findFirst();
        if (first.isEmpty()) {
            throw new UnsupportedVersionException(version);
        }
        return first.get();
    }
}
