/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion.function;

import de.eldoria.eldoutilities.crossversion.ServerVersion;
import de.eldoria.eldoutilities.crossversion.UnsupportedVersionException;
import de.eldoria.eldoutilities.crossversion.VersionRange;

import java.util.Map;

public class BaseVersionFunction<V> {
    private final Map<VersionRange, V> functions;

    public BaseVersionFunction(Map<VersionRange, V> functions) {
        this.functions = functions;
    }

    protected V get() {
        var first = functions.entrySet().stream()
                .filter(func -> func.getKey().isBetween(ServerVersion.CURRENT_VERSION))
                .map(Map.Entry::getValue)
                .findFirst();
        if (first.isEmpty()) {
            throw new UnsupportedVersionException();
        }
        return first.get();
    }
}
