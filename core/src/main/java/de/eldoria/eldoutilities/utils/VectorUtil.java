/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class VectorUtil {
    private VectorUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }


    public static Vector getDirectionVector(Location start, Location target) {
        return getDirectionVector(start.toVector(), target.toVector());
    }

    public static Vector getDirectionVector(Vector start, Vector target) {
        return new Vector(target.getX() - start.getX(), target.getY() - start.getY(), target.getZ() - start.getZ());
    }
}
