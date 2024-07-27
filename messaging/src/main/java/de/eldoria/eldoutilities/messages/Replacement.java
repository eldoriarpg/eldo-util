/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * A replacement represents a text placeholder and its replacement.
 */
public final class Replacement {
    private Replacement() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Creates a new replacement.
     *
     * @param key   key of replacement
     * @param value value for replacement
     * @return replacement with registered replacement
     */
    public static TagResolver create(String key, String value) {
        return Placeholder.parsed(sanitizeKey(key), value);
    }

    /**
     * Creates a new replacement.
     *
     * @param key   key of replacement
     * @param value value for replacement
     * @return replacement with registered replacement
     */
    public static TagResolver replacement(String key, String value) {
        return create(key, value);
    }

    /**
     * Creates a new replacement.
     *
     * @param key   key of replacement
     * @param value value for replacement
     * @return replacement with registered replacement
     */
    public static TagResolver create(String key, Object value) {
        return Placeholder.parsed(sanitizeKey(key), String.valueOf(value));
    }

    /**
     * Creates a new replacement.
     *
     * @param key   key of replacement
     * @param value value for replacement
     * @return replacement with registered replacement
     */
    public static TagResolver replacement(String key, Object value) {
        return create(key, value);
    }

    public static TagResolver create(String key, Double value) {
        return create(key, String.format("%.2f", value));
    }

    public static TagResolver create(String key, Float value) {
        return create(key, String.format("%.2f", value));
    }

    public static TagResolver number(String key, Double value) {
        return create(key, value);
    }

    public static TagResolver number(String key, Float value) {
        return create(key, value);
    }

    /**
     * Creates a new replacement.
     *
     * @param key    key of replacement
     * @param anEnum value which provides a string via {@link Enum#name()}
     * @return replacement with registered replacement
     */
    public static TagResolver create(String key, Enum<?> anEnum) {
        return create(key, anEnum.name());
    }

    /**
     * Creates a new replacement.
     *
     * @param key    key of replacement
     * @param anEnum value which provides a string via {@link Enum#name()}
     * @return replacement with registered replacement
     */
    public static TagResolver name(String key, Enum<?> anEnum) {
        return create(key, anEnum);
    }

    /**
     * Creates a new replacement for a player.
     *
     * @param key    key of replacement
     * @param player value which provides the name of the player
     * @return replacement with registered replacement
     */
    public static TagResolver create(String key, Player player) {
        return create(key, player.getName());
    }

    /**
     * Creates a new replacement for a player.
     *
     * @param key    key of replacement
     * @param player value which provides the name of the player
     * @return replacement with registered replacement
     */
    public static TagResolver player(String key, Player player) {
        return create(key, player);
    }

    /**
     * Creates a new replacement for a player.
     *
     * @param key   key of replacement
     * @param world world which provides the name of the world
     * @return replacement with registered replacement
     */
    public static TagResolver create(String key, World world) {
        return create(key, world.getName());
    }

    /**
     * Creates a new replacement for a player.
     *
     * @param key   key of replacement
     * @param world world which provides the name of the world
     * @return replacement with registered replacement
     */
    public static TagResolver world(String key, World world) {
        return create(key, world);
    }

    private static String sanitizeKey(String key) {
        return key.toLowerCase(Locale.ROOT).replaceAll(" ", "_");
    }
}
