/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
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
     * @param style format which should be applied on the replacement.
     * @return replacement with registered replacement
     */
    public static TagResolver create(String key, String value, Style style) {
        return Placeholder.component(sanatizeKey(key), Component.text(value).style(style));
    }

    /**
     * Creates a new replacement.
     *
     * @param key   key of replacement
     * @param value value for replacement
     * @return replacement with registered replacement
     */
    public static TagResolver create(String key, String value) {
        return Placeholder.component(sanatizeKey(key), Component.text(value));
    }

    /**
     * Creates a new replacement.
     *
     * @param key   key of replacement
     * @param value value for replacement
     * @return replacement with registered replacement
     */
    public static TagResolver create(String key, Object value) {
        return Placeholder.component(sanatizeKey(key), Component.text(String.valueOf(value)));
    }

    public static TagResolver create(String key, Double value, Style style) {
        return create(key, String.format("%.2f", value), style);
    }

    /**
     * Creates a new replacement.
     *
     * @param key   key of replacement
     * @param value value which provides a string via {@link Object#toString()}
     * @param style format which should be applied on the replacement.
     * @return replacement with registered replacement
     */
    public static TagResolver create(String key, Object value, Style style) {
        return create(key, value.toString(), style);
    }

    /**
     * Creates a new replacement.
     *
     * @param key    key of replacement
     * @param anEnum value which provides a string via {@link Enum#name()}
     * @param style  format which should be applied on the replacement.
     * @return replacement with registered replacement
     */
    public static TagResolver create(String key, Enum<?> anEnum, Style style) {
        return create(key, anEnum.name(), style);
    }

    /**
     * Creates a new replacement for a player.
     *
     * @param key    key of replacement
     * @param player value which provides the name of the player
     * @param style  format which should be applied on the replacement.
     * @return replacement with registered replacement
     */
    public static TagResolver create(String key, Player player, Style style) {
        return create(key, player.getName(), style);
    }

    /**
     * Creates a new replacement for a player.
     *
     * @param key   key of replacement
     * @param world world which provides the name of the world
     * @param style format which should be applied on the replacement.
     * @return replacement with registered replacement
     */
    public static TagResolver create(String key, World world, Style style) {
        return create(key, world.getName(), style);
    }

    private static String sanatizeKey(String key){
        return key.toLowerCase(Locale.ROOT).replaceAll(" ", "_");
    }
}
