package de.eldoria.eldoutilities.commands.command.util;

import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.EnumUtil;
import de.eldoria.eldoutilities.utils.FlagContainer;
import de.eldoria.eldoutilities.utils.Parser;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class Arguments implements Iterable<String> {
    private final FlagContainer flags;
    Plugin plugin;
    private String[] args;

    private Arguments(Plugin plugin, String[] args, FlagContainer flags) {
        this.args = args;
        this.flags = flags;
    }

    /**
     * Create a new argument
     *
     * @param plugin plugin instance
     * @param args   argument array
     * @return new argument instance
     */
    public static Arguments create(Plugin plugin, String[] args) {
        FlagContainer flags = FlagContainer.of(args);
        return new Arguments(plugin, args, flags);
    }

    /**
     * Asserts that the commands have at least the required amount of arguments.
     *
     * @param index minimal length
     * @throws CommandException when not enough arguments are present.
     */
    public void assertLength(int index) throws CommandException {
        CommandAssertions.missingArgument(args, index);
    }

    /**
     * Checks if enough arguments are present
     *
     * @param index minimal length
     * @return true if enough arguments are present
     */
    public boolean hasArg(int index) {
        return args.length > index;
    }

    /**
     * Size of the arguments.
     *
     * @return the amount of arguments
     */
    public int size() {
        return args.length;
    }

    /**
     * Checks if no arguments are present
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return args.length == 0;
    }

    /**
     * Parses the arguments as quoted args. This will group arguments in quotes.
     * <p>
     * Arguments are not parsed quoted by default.
     * <p>
     * Use {@link #splitArgs()} to revert this change
     */
    public void parseQuoted() {
        args = ArgumentUtils.parseQuotedArgs(args);
    }

    /**
     * Splits the arguments if they were grouped by {@link #parseQuoted()}
     */
    public void splitArgs() {
        args = Arrays.stream(args)
                .map(arg -> arg.contains(" ") ? String.format("\"%s\"", arg) : arg)
                .collect(Collectors.joining(" "))
                .split(" ");
    }

    /**
     * Get the argument as string
     *
     * @param index index of argument
     * @return argument as string
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    public @NotNull String asString(int index) throws IndexOutOfBoundsException {
        if (index < 0) {
            return args[args.length - index];
        }
        return args[index];
    }

    /**
     * Get the argument as string
     *
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return argument as string
     */
    public @NotNull String asString(int index, String def) {
        if (hasArg(index)) return asString(index);
        return def;
    }

    /**
     * Get the argument as integer
     *
     * @param index index of argument
     * @return index as integer
     * @throws CommandException          when the argument is not an integer
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    public int asInt(int index) throws CommandException, IndexOutOfBoundsException {
        return Parser.parseInt(asString(index))
                .orElseThrow(() -> CommandException.message("error.invalidNumber"));
    }

    /**
     * Get the argument as integer
     *
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as integer
     * @throws CommandException when the argument is not an integer
     */
    public int asInt(int index, int def) throws CommandException {
        if (hasArg(index)) return asInt(index);
        return def;
    }

    /**
     * @param index index of argument
     * @return index as long
     * @throws CommandException          when the argument is not a long
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    public long asLong(int index) throws CommandException, IndexOutOfBoundsException {
        return Parser.parseLong(asString(index))
                .orElseThrow(() -> CommandException.message("error.invalidNumber"));
    }

    /**
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as long
     * @throws CommandException when the argument is not a long
     */
    public long asLong(int index, long def) throws CommandException {
        if (hasArg(index)) return asLong(index);
        return def;
    }

    /**
     * @param index index of argument
     * @return index as double
     * @throws CommandException          when the argument is not a double
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    public double asDouble(int index) throws CommandException, IndexOutOfBoundsException {
        return Parser.parseDouble(asString(index))
                .orElseThrow(() -> CommandException.message("error.invalidNumber"));
    }

    /**
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as double
     * @throws CommandException when the argument is not a double
     */
    public double asDouble(int index, double def) throws CommandException {
        if (hasArg(index)) return asDouble(index);
        return def;
    }

    /**
     * Get the argument as a boolean
     *
     * @param index index of argument
     * @return index as boolean
     * @throws CommandException          when the argument is not a boolean
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    public boolean asBoolean(int index) throws CommandException, IndexOutOfBoundsException {
        return asBoolean(index, "true", "false");
    }

    /**
     * Get the argument as a boolean
     *
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as boolean
     * @throws CommandException when the argument is not a boolean
     */
    public boolean asBoolean(int index, boolean def) throws CommandException {
        if (hasArg(index)) return asBoolean(index);
        return def;
    }

    /**
     * Get the argument as a boolean
     *
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as boolean
     * @throws CommandException when the argument is not a boolean
     */
    public boolean asBoolean(int index, String aTrue, String aFalse, boolean def) throws CommandException {
        if (hasArg(index)) return asBoolean(index, aTrue, aFalse);
        return def;
    }

    /**
     * Get the argument as a boolean
     *
     * @param index  index of argument
     * @param aTrue  value of true
     * @param aFalse value of false
     * @return index as boolean
     * @throws CommandException          when the argument is not a boolean
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    public boolean asBoolean(int index, String aTrue, String aFalse) throws CommandException, IndexOutOfBoundsException {
        return Parser.parseBoolean(asString(index), aTrue, aFalse)
                .orElseThrow(() -> CommandException.message("error.invalidBoolean",
                        Replacement.create("true", aTrue), Replacement.create("false", aFalse)));
    }

    /**
     * Get the argument as a material.
     * <p>
     * This will send a custom message without listing all possible values.
     *
     * @param index index of argument
     * @return index as material
     * @throws CommandException          when the argument is not a material
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    @NotNull
    public Material asMaterial(int index) throws CommandException, IndexOutOfBoundsException {
        return asMaterial(index, false);
    }

    /**
     * Get the argument as a material.
     * <p>
     * This will send a custom message without listing all possible values.
     *
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as material
     * @throws CommandException when the argument is not a material
     */
    @NotNull
    public Material asMaterial(int index, Material def) throws CommandException {
        if (hasArg(index)) return asMaterial(index);
        return def;
    }

    /**
     * Get the argument as a material.
     * <p>
     * This will send a custom message without listing all possible values.
     *
     * @param index        index of argument
     * @param stripStrings if true underscores will be removed before checking
     * @return index as material
     * @throws CommandException          when the argument is not a material
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    @NotNull
    public Material asMaterial(int index, boolean stripStrings) throws CommandException, IndexOutOfBoundsException {
        return EnumUtil.parse(asString(index), Material.class, stripStrings)
                .orElseThrow(() -> CommandException.message("error.invalidMaterial"));
    }

    /**
     * Get the argument as a material.
     * <p>
     * This will send a custom message without listing all possible values.
     *
     * @param index        index of argument
     * @param def          returned if the index is not valid
     * @param stripStrings if true underscores will be removed before checking
     * @return index as material
     * @throws CommandException when the argument is not a material
     */
    @NotNull
    public Material asMaterial(int index, boolean stripStrings, Material def) throws CommandException {
        if (hasArg(index)) return asMaterial(index, stripStrings);
        return def;
    }

    /**
     * Get the argument as an enum
     *
     * @param index index of argument
     * @param clazz enum clazz to parse
     * @param <T>   type of enum
     * @return index as enum value
     * @throws CommandException          When the string could not be parsed to an enum
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    @NotNull
    public <T extends Enum<T>> T asEnum(int index, Class<T> clazz) throws CommandException, IndexOutOfBoundsException {
        return asEnum(index, clazz, false);
    }

    /**
     * Get the argument as an enum
     *
     * @param index index of argument
     * @param clazz enum clazz to parse
     * @param def   returned if the index is not valid
     * @param <T>   type of enum
     * @return index as enum value
     * @throws CommandException When the string could not be parsed to an enum
     */
    @NotNull
    public <T extends Enum<T>> T asEnum(int index, Class<T> clazz, T def) throws CommandException {
        if (hasArg(index)) return asEnum(index, clazz);
        return def;
    }

    /**
     * Get the argument as an enum
     *
     * @param index        index of argument
     * @param clazz        enum clazz to parse
     * @param stripStrings if true underscores will be removed before checking
     * @param <T>          type of enum
     * @return index as enum value
     * @throws CommandException          When the string could not be parsed to an enum
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    @NotNull
    public <T extends Enum<T>> T asEnum(int index, Class<T> clazz, boolean stripStrings) throws CommandException, IndexOutOfBoundsException {
        return EnumUtil.parse(asString(index), clazz, stripStrings)
                .orElseThrow(() -> CommandException.message("error.invalidEnumValue",
                        Replacement.create("VALUES", EnumUtil.enumValues(clazz)).addFormatting('6')));
    }

    /**
     * Get the argument as an enum
     *
     * @param index        index of argument
     * @param clazz        enum clazz to parse
     * @param stripStrings if true underscores will be removed before checking
     * @param def          returned if the index is not valid
     * @param <T>          type of enum
     * @return index as enum value
     * @throws CommandException When the string could not be parsed to an enum
     */
    @NotNull
    public <T extends Enum<T>> T asEnum(int index, Class<T> clazz, boolean stripStrings, T def) throws CommandException {
        if (hasArg(index)) return asEnum(index, clazz, stripStrings);
        return def;
    }

    /**
     * Get the argument as a player
     *
     * @param index index of argument
     * @return index as player
     * @throws CommandException          when no player with this name is online
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    @NotNull
    public Player asPlayer(int index) throws CommandException, IndexOutOfBoundsException {
        Player player = plugin.getServer().getPlayer(asString(index));
        if (player == null) throw CommandException.message("error.notOnline");
        return player;
    }

    /**
     * Get the argument as a player
     *
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as player
     * @throws CommandException when no player with this name is online
     */
    @NotNull
    public Player asPlayer(int index, Player def) throws CommandException {
        if (hasArg(index)) return asPlayer(index);
        return def;
    }

    /**
     * Get the argument as a offline player
     *
     * @param index index of argument
     * @return index as offline player
     * @throws CommandException          when no player with this name was on this server previously
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    @NotNull
    public OfflinePlayer asOfflinePlayer(int index) throws CommandException, IndexOutOfBoundsException {
        String name = asString(index);
        for (OfflinePlayer player : plugin.getServer().getOfflinePlayers()) {
            if (name.equalsIgnoreCase(player.getName())) return player;
        }
        throw CommandException.message("error.unkownPlayer");
    }

    /**
     * Get the argument as a offline player
     *
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as offline player
     * @throws CommandException when no player with this name was on this server previously
     */
    @NotNull
    public OfflinePlayer asOfflinePlayer(int index, OfflinePlayer def) throws CommandException {
        if (hasArg(index)) return asOfflinePlayer(index);
        return def;
    }

    /**
     * Get the argument as a world
     *
     * @param index index of argument
     * @return index as world
     * @throws CommandException          When the string is not the name of a world
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    @NotNull
    public World asWorld(int index) throws CommandException, IndexOutOfBoundsException {
        String name = asString(index);
        World world = plugin.getServer().getWorld(name);
        if (world == null) throw CommandException.message("error.unkownWorld");
        return world;
    }

    /**
     * Get the argument as a world
     *
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as world
     * @throws CommandException When the string is not the name of a world
     */
    @NotNull
    public World asWorld(int index, World def) throws CommandException {
        if (hasArg(index)) return asWorld(index);
        return def;
    }

    /**
     * Get the arguments starting from an index till the end as a list
     *
     * @param from the first index to be returned
     * @return a list of arguments
     */
    public List<String> args(int from) {
        return ArgumentUtils.getRangeAsList(args, from);
    }

    /**
     * Returns a range of arguments as string.
     *
     * @return range as string
     */
    public String join() {
        return join(" ");
    }

    /**
     * Returns a range of arguments as string.
     *
     * @param from start index (included). Use negative counts to count from the last index.
     * @return range as string
     */
    public String join(int from) {
        return join(" ", from);
    }

    /**
     * Returns a range of arguments as string.
     *
     * @param from start index (included). Use negative counts to count from the last index.
     * @param to   end index (excluded). Use negative counts to count from the last index.
     * @return range as string
     */
    public String join(int from, int to) {
        return join(" ", from, to);
    }

    /**
     * Returns a range of arguments as string.
     *
     * @param delimiter delimiter to join
     * @return range as string
     */
    public String join(String delimiter) {
        return String.join(delimiter, args);
    }

    /**
     * Returns a range of arguments as string.
     *
     * @param delimiter delimiter to join
     * @param from      start index (included). Use negative counts to count from the last index.
     * @return range as string
     */
    public String join(String delimiter, int from) {
        return ArgumentUtils.getMessage(args, from);
    }

    /**
     * Returns a range of arguments as string.
     *
     * @param delimiter delimiter to join
     * @param from      start index (included). Use negative counts to count from the last index.
     * @param to        end index (excluded). Use negative counts to count from the last index.
     * @return range as string
     */
    public String join(String delimiter, int from, int to) {
        return ArgumentUtils.getMessage(args, from, to);
    }

    /**
     * Get the arguments as a list
     *
     * @return arguments as list
     */
    public List<String> args() {
        return Arrays.asList(args);
    }

    /**
     * Get a copy of the arguments array
     *
     * @return new arguments array
     */
    public String[] asArray() {
        return args.clone();
    }

    /**
     * Get the arguments between two indices
     *
     * @param from from inclusive
     * @param to   to exclusive
     * @return arguments as list
     */
    public List<String> args(int from, int to) {
        return ArgumentUtils.getRangeAsList(args, from, to);
    }

    private <T> Optional<T> parseArg(int index, Function<String, Optional<T>> map) {
        String value = asString(index);
        return map.apply(value);
    }

    /**
     * Get the subarguments. This will return all arguments except the first one.
     *
     * @return arguments without the first arguments.
     */
    public Arguments subArguments() {
        return Arguments.create(plugin, ArgumentUtils.getRangeAsList(args, 1).toArray(new String[0]));
    }

    /**
     * Checks if the command has a flag
     *
     * @param flag flag to check
     * @return true when flag is present
     */
    public boolean hasFlag(@NotNull String flag) {
        return flags.has(flag);
    }

    /**
     * Check if the flag has a value.
     *
     * @param flag flag to check
     * @return true when the flag is present and has a value
     */
    public boolean hasFlagValue(String flag) {
        return flags.hasValue(flag);
    }

    /**
     * Get the value of a flag
     *
     * @param flag flag
     * @param map  function to map the value
     * @param <T>  type of value
     * @return parsed flag
     */
    public <T> T getFlag(@NotNull String flag, Function<@Nullable String, T> map) {
        return flags.get(flag, map);
    }

    /**
     * Get the string value of a flag
     *
     * @param flag flag
     * @return value of flag when present
     */
    @Nullable
    public String getFlag(String flag) {
        return flags.get(flag);
    }

    /**
     * Get the value of a flag if present
     *
     * @param flag flag
     * @return optional value
     */
    public Optional<String> getFlagValueIfPresent(String flag) {
        return flags.getIfPresent(flag);
    }

    /**
     * Get the value of a flag is present
     *
     * @param flag flag
     * @param map  value parser
     * @param <T>  value of return type
     * @return optional parsed value
     */
    public <T> Optional<T> getFlagValueIfPresent(@NotNull String flag, Function<String, T> map) {
        return flags.getIfPresent(flag, map);
    }

    @NotNull
    @Override
    public Iterator<String> iterator() {
        return args().iterator();
    }

    public Spliterator<String> spliterator() {
        return args().spliterator();
    }

    public Stream<String> stream() {
        return args().stream();
    }

    public Stream<String> parallelStream() {
        return args().parallelStream();
    }
}
