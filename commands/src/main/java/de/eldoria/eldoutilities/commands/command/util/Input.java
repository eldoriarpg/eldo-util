/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.command.util;

import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.messages.Replacement;
import de.eldoria.eldoutilities.utils.EnumUtil;
import de.eldoria.eldoutilities.utils.Parser;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Input {
    private final Plugin plugin;
    private final String arg;

    private Input(Plugin plugin, String arg) {
        this.plugin = plugin;
        this.arg = arg;
    }

    public static Input of(Plugin plugin, String arg) {
        return new Input(plugin, arg);
    }

    /**
     * Get the argument as string
     *
     * @return argument as string
     */
    public @NotNull String asString() {
        return arg;
    }

    /**
     * Get the argument as integer
     *
     * @return index as integer
     * @throws CommandException when the argument is not an integer
     */
    public int asInt() throws CommandException, IndexOutOfBoundsException {
        return Parser.parseInt(asString())
                .orElseThrow(() -> CommandException.message("error.invalidNumber"));
    }

    /**
     * @return index as long
     * @throws CommandException when the argument is not a long
     */
    public long asLong() throws CommandException, IndexOutOfBoundsException {
        return Parser.parseLong(asString())
                .orElseThrow(() -> CommandException.message("error.invalidNumber"));
    }

    /**
     * @return index as double
     * @throws CommandException when the argument is not a double
     */
    public double asDouble() throws CommandException, IndexOutOfBoundsException {
        return Parser.parseDouble(asString())
                .orElseThrow(() -> CommandException.message("error.invalidNumber"));
    }

    /**
     * Get the argument as a boolean
     *
     * @return index as boolean
     * @throws CommandException when the argument is not a boolean
     */
    public boolean asBoolean() throws CommandException, IndexOutOfBoundsException {
        return asBoolean("true", "false");
    }

    /**
     * Get the argument as a boolean
     *
     * @param aTrue  value of true
     * @param aFalse value of false
     * @return index as boolean
     * @throws CommandException when the argument is not a boolean
     */
    public boolean asBoolean(String aTrue, String aFalse) throws CommandException, IndexOutOfBoundsException {
        return Parser.parseBoolean(asString(), aTrue, aFalse)
                .orElseThrow(() -> CommandException.message("error.invalidBoolean",
                        Replacement.create("true", aTrue), Replacement.create("false", aFalse)));
    }

    /**
     * Get the argument as a material.
     * <p>
     * This will send a custom message without listing all possible values.
     *
     * @return index as material
     * @throws CommandException when the argument is not a material
     */
    @NotNull
    public Material asMaterial() throws CommandException, IndexOutOfBoundsException {
        return asMaterial(false);
    }

    /**
     * Get the argument as a material.
     * <p>
     * This will send a custom message without listing all possible values.
     *
     * @param stripStrings if true underscores will be removed before checking
     * @return index as material
     * @throws CommandException when the argument is not a material
     */
    @NotNull
    public Material asMaterial(boolean stripStrings) throws CommandException, IndexOutOfBoundsException {
        return EnumUtil.parse(asString(), Material.class, stripStrings)
                .orElseThrow(() -> CommandException.message("error.invalidMaterial"));
    }

    /**
     * Get the argument as an enum
     *
     * @param clazz enum clazz to parse
     * @param <T>   type of enum
     * @return index as enum value
     * @throws CommandException When the string could not be parsed to an enum
     */
    @NotNull
    public <T extends Enum<T>> T asEnum(Class<T> clazz) throws CommandException, IndexOutOfBoundsException {
        return asEnum(clazz, false);
    }

    /**
     * Get the argument as an enum
     *
     * @param clazz        enum clazz to parse
     * @param stripStrings if true underscores will be removed before checking
     * @param <T>          type of enum
     * @return index as enum value
     * @throws CommandException When the string could not be parsed to an enum
     */
    @NotNull
    public <T extends Enum<T>> T asEnum(Class<T> clazz, boolean stripStrings) throws CommandException, IndexOutOfBoundsException {
        return EnumUtil.parse(asString(), clazz, stripStrings)
                .orElseThrow(() -> CommandException.message("error.invalidEnumValue",
                        Replacement.create("VALUES", EnumUtil.enumValues(clazz), Style.style(NamedTextColor.GOLD))));
    }

    /**
     * Get the argument as a player
     *
     * @return index as player
     * @throws CommandException when no player with this name is online
     */
    @NotNull
    public Player asPlayer() throws CommandException, IndexOutOfBoundsException {
        var player = plugin.getServer().getPlayer(asString());
        if (player == null) throw CommandException.message("error.notOnline");
        return player;
    }

    /**
     * Get the argument as a offline player
     *
     * @return index as offline player
     * @throws CommandException when no player with this name was on this server previously
     */
    @NotNull
    public OfflinePlayer asOfflinePlayer() throws CommandException, IndexOutOfBoundsException {
        var name = asString();
        for (var player : plugin.getServer().getOfflinePlayers()) {
            if (name.equalsIgnoreCase(player.getName())) return player;
        }
        throw CommandException.message("error.unkownPlayer");
    }

    /**
     * Get the argument as a world
     *
     * @return index as world
     * @throws CommandException When the string is not the name of a world
     */
    @NotNull
    public World asWorld() throws CommandException, IndexOutOfBoundsException {
        var name = asString();
        var world = plugin.getServer().getWorld(name);
        if (world == null) throw CommandException.message("error.unkownWorld");
        return world;
    }
}
