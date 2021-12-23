package de.eldoria.eldoutilities.commands.command.util;

import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.FlagContainer;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class Arguments implements Iterable<Input> {
    private static final Pattern FLAG = Pattern.compile("^-[a-zA-Z]");
    private final FlagContainer flags;
    private final Plugin plugin;
    private final CommandSender sender;
    private final String[] rawArgs;
    private final List<Input> args = new ArrayList<>();

    private Arguments(Plugin plugin, CommandSender sender, String[] args, FlagContainer flags) {
        this.plugin = plugin;
        this.sender = sender;
        this.rawArgs = args;
        this.flags = flags;
        splitArgs();
    }

    /**
     * Create a new argument
     *
     * @param plugin plugin instance
     * @param args   argument array
     * @return new argument instance
     */
    public static Arguments create(Plugin plugin, CommandSender sender, String[] args) {
        var flags = FlagContainer.of(plugin, args);
        return new Arguments(plugin, sender, args, flags);
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
        return args.size() > index;
    }

    /**
     * Size of the arguments.
     *
     * @return the amount of arguments
     */
    public int size() {
        return args.size();
    }

    /**
     * Size of the arguments.
     *
     * @return the amount of arguments
     */
    public boolean sizeIs(int i) {
        return args.size() == i;
    }

    /**
     * Checks if no arguments are present
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return args.isEmpty();
    }

    /**
     * Parses the arguments as quoted args. This will group arguments in quotes.
     * <p>
     * Arguments are not parsed quoted by default.
     * <p>
     * Use {@link #splitArgs()} to revert this change
     */
    public void parseQuoted() {
        args.clear();
        for (var s : ArgumentUtils.parseQuotedArgs(rawArgs)) {
            if (FlagContainer.isFlag(s)) break;
            args.add(Input.of(plugin, s));
        }
    }

    /**
     * Splits the arguments if they were grouped by {@link #parseQuoted()}
     */
    public void splitArgs() {
        for (var s : Arrays.stream(rawArgs).collect(Collectors.toList())) {
            if (FlagContainer.isFlag(s)) break;
            args.add(Input.of(plugin, s));
        }
    }

    public Input get(int index) {
        if (index < 0) {
            return args.get(size() + index);
        }
        return args.get(index);
    }

    /**
     * Get the argument as string
     *
     * @param index index of argument
     * @return argument as string
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    public @NotNull String asString(int index) throws IndexOutOfBoundsException {
        return get(index).asString();
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
     * Get the argument as string
     *
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return argument as string
     */
    public @NotNull String asString(int index, Supplier<String> def) {
        if (hasArg(index)) return asString(index);
        return def.get();
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
        return get(index).asInt();
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
     * Get the argument as integer
     *
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as integer
     * @throws CommandException when the argument is not an integer
     */
    public int asInt(int index, Supplier<Integer> def) throws CommandException {
        if (hasArg(index)) return asInt(index);
        return def.get();
    }

    /**
     * @param index index of argument
     * @return index as long
     * @throws CommandException          when the argument is not a long
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    public long asLong(int index) throws CommandException, IndexOutOfBoundsException {
        return get(index).asLong();
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
     * @param def   returned if the index is not valid
     * @return index as long
     * @throws CommandException when the argument is not a long
     */
    public long asLong(int index, Supplier<Long> def) throws CommandException {
        if (hasArg(index)) return asLong(index);
        return def.get();
    }

    /**
     * @param index index of argument
     * @return index as double
     * @throws CommandException          when the argument is not a double
     * @throws IndexOutOfBoundsException when the index is equal or larger than {@link #size()}
     */
    public double asDouble(int index) throws CommandException, IndexOutOfBoundsException {
        return get(index).asDouble();
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
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as double
     * @throws CommandException when the argument is not a double
     */
    public double asDouble(int index, Supplier<Double> def) throws CommandException {
        if (hasArg(index)) return asDouble(index);
        return def.get();
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
        return get(index).asBoolean();
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
    public boolean asBoolean(int index, Supplier<Boolean> def) throws CommandException {
        if (hasArg(index)) return asBoolean(index);
        return def.get();
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
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as boolean
     * @throws CommandException when the argument is not a boolean
     */
    public boolean asBoolean(int index, String aTrue, String aFalse, Supplier<Boolean> def) throws CommandException {
        if (hasArg(index)) return asBoolean(index, aTrue, aFalse);
        return def.get();
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
        return get(index).asBoolean(aTrue, aFalse);
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
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as material
     * @throws CommandException when the argument is not a material
     */
    @NotNull
    public Material asMaterial(int index, Supplier<Material> def) throws CommandException {
        if (hasArg(index)) return asMaterial(index);
        return def.get();
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
        return get(index).asMaterial(stripStrings);
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
    public Material asMaterial(int index, boolean stripStrings, Supplier<Material> def) throws CommandException {
        if (hasArg(index)) return asMaterial(index, stripStrings);
        return def.get();
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
        return get(index).asEnum(clazz, false);
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
     * @param index index of argument
     * @param clazz enum clazz to parse
     * @param def   returned if the index is not valid
     * @param <T>   type of enum
     * @return index as enum value
     * @throws CommandException When the string could not be parsed to an enum
     */
    @NotNull
    public <T extends Enum<T>> T asEnum(int index, Class<T> clazz, Supplier<T> def) throws CommandException {
        if (hasArg(index)) return asEnum(index, clazz);
        return def.get();
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
        return get(index).asEnum(clazz, stripStrings);
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
    public <T extends Enum<T>> T asEnum(int index, Class<T> clazz, boolean stripStrings, Supplier<T> def) throws CommandException {
        if (hasArg(index)) return asEnum(index, clazz, stripStrings);
        return def.get();
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
        return get(index).asPlayer();
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
     * Get the argument as a player
     *
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as player
     * @throws CommandException when no player with this name is online
     */
    @NotNull
    public Player asPlayer(int index, Supplier<Player> def) throws CommandException {
        if (hasArg(index)) return asPlayer(index);
        return def.get();
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
        return get(index).asOfflinePlayer();
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
     * Get the argument as a offline player
     *
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as offline player
     * @throws CommandException when no player with this name was on this server previously
     */
    @NotNull
    public OfflinePlayer asOfflinePlayer(int index, Supplier<OfflinePlayer> def) throws CommandException {
        if (hasArg(index)) return asOfflinePlayer(index);
        return def.get();
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
        return get(index).asWorld();
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
     * Get the argument as a world
     *
     * @param index index of argument
     * @param def   returned if the index is not valid
     * @return index as world
     * @throws CommandException When the string is not the name of a world
     */
    @NotNull
    public World asWorld(int index, Supplier<World> def) throws CommandException {
        if (hasArg(index)) return asWorld(index);
        return def.get();
    }

    /**
     * Get the arguments starting from an index till the end as a list
     *
     * @param from the first index to be returned
     * @return a list of arguments
     */
    public List<Input> args(int from) {
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
        return args.stream().map(Input::asString).collect(Collectors.joining(delimiter));
    }

    /**
     * Returns a range of arguments as string.
     *
     * @param delimiter delimiter to join
     * @param from      start index (included). Use negative counts to count from the last index.
     * @return range as string
     */
    public String join(String delimiter, int from) {
        return ArgumentUtils.getMessage(args.stream().map(Input::asString).collect(Collectors.toList()), from);
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
        return ArgumentUtils.getMessage(args.stream().map(Input::asString).collect(Collectors.toList()), from, to);
    }

    /**
     * Get the arguments as a list
     *
     * @return arguments as list
     */
    public List<Input> args() {
        return Collections.unmodifiableList(args);
    }

    /**
     * Get a copy of the arguments array
     *
     * @return new arguments array
     */
    public Input[] asArray() {
        return args.toArray(new Input[0]);
    }

    /**
     * Get the arguments between two indices
     *
     * @param from from inclusive
     * @param to   to exclusive
     * @return arguments as list
     */
    public List<Input> args(int from, int to) {
        return ArgumentUtils.getRangeAsList(args, from, to);
    }

    private <T> Optional<T> parseArg(int index, Function<String, Optional<T>> map) {
        var value = asString(index);
        return map.apply(value);
    }

    /**
     * Get the subarguments. This will return all arguments except the first one.
     *
     * @return arguments without the first arguments.
     */
    public Arguments subArguments() {
        return subArguments(1);
    }

    /**
     * Get the subarguments. This will return all arguments except the first one.
     *
     * @param nesting the amount of arguments which should get removed
     * @return arguments without the first arguments.
     */
    public Arguments subArguments(int nesting) {
        return Arguments.create(plugin, sender, ArgumentUtils.getRangeAsList(rawArgs, nesting).toArray(new String[0]));
    }

    public FlagContainer flags() {
        return flags;
    }

    public Input last() {
        if (isEmpty()) return null;
        return get(size() - 1);
    }

    public CommandSender sender() {
        return sender;
    }

    @NotNull
    @Override
    public Iterator<Input> iterator() {
        return args().iterator();
    }

    public Spliterator<Input> spliterator() {
        return args().spliterator();
    }

    public Stream<Input> stream() {
        return args().stream();
    }

    public Stream<Input> parallelStream() {
        return args().parallelStream();
    }

    @Override
    public String toString() {
        return "Arguments{" +
               "flags=" + flags +
               ", plugin=" + plugin.getName() +
               ", args=" + Arrays.toString(rawArgs) +
               '}';
    }
}
