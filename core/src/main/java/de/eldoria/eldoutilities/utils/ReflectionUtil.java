/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import com.google.common.collect.ObjectArrays;

import java.lang.reflect.Field;

/**
 * Utils used for reflections on classes.
 *
 * @since 1.8.8
 */
public final class ReflectionUtil {
    /**
     * Get all declared fields from a class including superclasses
     *
     * @param obj object of class
     * @return array of fields
     */
    public static Field[] getAllFields(Object obj) {
        return getAllFields(obj.getClass());
    }

    /**
     * Get all declared fields from a class including superclasses
     *
     * @param clazz clazz to check
     * @return array of fields
     */
    public static Field[] getAllFields(Class<?> clazz) {
        var fields = new Field[0];
        var currClazz = clazz;
        while (currClazz != null) {
            fields = ObjectArrays.concat(fields, currClazz.getDeclaredFields(), Field.class);
            currClazz = currClazz.getSuperclass();
        }
        return fields;
    }

    public static boolean isPaper() {
        return hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration");
    }

    public static boolean hasClass(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
