package de.eldoria.eldoutilities.utils;

import de.eldoria.eldoutilities.C;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * This class contains helpful methods for handling of command arguments.
 *
 * @since 1.0.0
 */
public final class ArgumentUtils {
    private ArgumentUtils() {
        throw new UnsupportedOperationException("This is a utility class!");
    }

    /**
     * Get a string value or a default value when the index does not exists.
     *
     * @param arguments    array of string arguments.
     * @param index        index of the requested parameter
     * @param defaultValue default value which will be returned when the index does not exists.
     * @return string at index or default value if the index does not exists.
     */
    public static String getOrDefault(String[] arguments, int index, String defaultValue) {
        String arg = get(arguments, index);
        return arg == null ? defaultValue : arg;
    }

    /**
     * Get a value from string or a default value when the index does not exists or the parse result is null.
     *
     * @param arguments    array of string arguments.
     * @param index        index of the requested parameter
     * @param parse        Function to parse the string to value. Returns null if not parseable.
     * @param defaultValue default value which will be returned when the index does not exists.
     * @param <T>          return type
     * @return string at index or default value if the index does not exists.
     */
    public static <T> T getOrDefault(String[] arguments, int index, Function<String, T> parse, T defaultValue) {
        String s = get(arguments, index);

        if (s == null) {
            return defaultValue;
        }
        return parse.apply(s);
    }

    /**
     * Get the index from the string array .
     *
     * @param arguments array of string arguments.
     * @param index     index of the requested parameter
     * @return string or null if the index does not exists
     */
    public static String get(String[] arguments, int index) {
        if (arguments.length > index) return arguments[index];
        return null;
    }

    /**
     * Get the index parsed.
     *
     * @param arguments array of string arguments.
     * @param index     index of the requested parameter
     * @param parse     function to parse the string.
     * @param <T>       type of optional return value
     * @return the string of the index after the parse function was applied or a empty optional when the index was not
     * found.
     */
    public static <T> Optional<T> get(String[] arguments, int index, Function<String, T> parse) {
        if (arguments.length > index) {
            parse.apply(arguments[index]);
        }
        return Optional.empty();
    }

    /**
     * Gets a optional parameter. Will return the default value when the index is not inside the array.
     *
     * @param arguments    array of string arguments.
     * @param index        index of the requested parameter
     * @param defaultValue default value which will be delivered when the index does not exists.
     * @param parse        function to parse the string.
     * @param <T>          type of the returned parameter.
     * @return parsed string value at index or default value if the index does not exists
     */
    public static <T> T getOptionalParameter(String[] arguments, int index, T defaultValue, Function<String, T> parse) {
        String arg = get(arguments, index);
        if (arg == null) return defaultValue;
        return parse.apply(arg);
    }

    public static <T> T getDefaultFromPlayerOrArg(String[] args, int index, CommandSender sender,
                                                  Function<Player, T> playerFunction, Function<String, T> argFunction) {
        T result = null;
        if (sender instanceof Player) {
            result = playerFunction.apply((Player) sender);
        }

        if (args.length > index) {
            result = argFunction.apply(args[index]);
        }

        return result;
    }

    /**
     * Returns a range of a string array as string.
     *
     * @param delimiter delimiter for string join
     * @param source    source array
     * @param from      start index (included). Use negative counts to count from the last index.
     * @param to        end index (excluded). Use negative counts to count from the last index.
     * @return range as string
     */
    public static String getRangeAsString(String delimiter, String[] source, int from, int to) {
        int finalTo = to;
        if (to < 1) {
            finalTo = source.length + to;
        }
        int finalFrom = from;
        if (from < 0) {
            finalFrom = source.length + from;
        }

        if (finalFrom > finalTo || finalFrom < 0 || finalTo > source.length) {
            return "";
        }

        return String.join(delimiter, Arrays.copyOfRange(source, finalFrom, finalTo)).trim();
    }

    /**
     * Get a array as sublist from 'from' to array.length().
     *
     * @param strings arguments
     * @param from    start index included
     * @return string array delimited with ' '
     */
    public static String getRangeAsString(String[] strings, int from) {
        return getRangeAsString(" ", strings, from, 0);
    }

    public static String unescapeWorldName(String world) {
        return unescapeWorldName(world, C.SPACE_REPLACE);
    }

    public static String unescapeWorldName(String world, String replace) {
        return world.replace(replace, " ");
    }

    public static String escapeWorldName(String world) {
        return escapeWorldName(world, C.SPACE_REPLACE);
    }

    public static String escapeWorldName(String world, String replace) {
        return world.replace(" ", replace);
    }

    public static String escapeWorldName(World world) {
        return escapeWorldName(world, C.SPACE_REPLACE);
    }

    public static String escapeWorldName(World world, String replace) {
        return escapeWorldName(world.getName(), replace);
    }

    public static World getWorld(String world) {
        return Bukkit.getWorld(unescapeWorldName(world));
    }

    public static String[] parseQuotedArgs(String args, boolean strip) {
        if (strip) {
            args = args.replace("\\s+", " ");
        }
        return parseQuotedArgs(args.split("\\s"));
    }

    /**
     * Parse arguments and keep quotes together
     *
     * @param args args to parse
     *
     * @return parsed args
     */
    public static String[] parseQuotedArgs(String[] args) {
        List<String> currArgs = new ArrayList<>();
        List<String> currArg = new ArrayList<>();
        boolean open = false;
        for (String arg : args) {
            if (arg.startsWith("\"") && arg.endsWith("\"")) {
                currArgs.add(arg.replace("\"", ""));
                continue;
            }
            if (arg.startsWith("\"")) {
                open = true;
            }
            if (!open) {
                currArgs.add(arg);
                continue;
            }

            if (arg.endsWith("\"")) {
                currArg.add(arg);
                currArgs.add(String.join(" ", currArg).replace("\"", ""));
                currArg.clear();
                open = false;
                continue;
            }

            currArg.add(arg);
        }
        return currArgs.toArray(new String[0]);
    }

    /**
     * Get a message from string array from 'from' to array.length().
     *
     * @param strings array of strings.
     * @param from    start index (included). Use negative counts to count from the last index.
     *
     * @return array sequence as string
     */
    public static String getMessage(String[] strings, int from) {
        return getMessage(strings, from, 0);
    }

    /**
     * Get a message from string array from 'from' to 'to'.
     *
     * @param strings array of strings.
     * @param from    start index (included). Use negative counts to count from the last index.
     * @param to      end index (excluded). Use negative counts to count from the last index.
     *
     * @return array sequence as string
     */
    public static String getMessage(String[] strings, int from, int to) {
        return TextFormatting.getRangeAsString(" ", strings, from, to);
    }

    /**
     * Get a array as sublist from 'from' to array.length().
     *
     * @param objects arguments
     * @param from    start index included
     * @param <T>     Type of objects
     *
     * @return sublist
     */
    public static <T> List<T> getRangeAsList(T[] objects, int from) {
        return getRangeAsList(objects, from, 0);
    }

    /**
     * Get a sublist from array from 'from' to 'to'.
     *
     * @param objects list of objects.
     * @param from    start index (included). Use negative counts to count from the last index.
     * @param to      end index (excluded). Use negative counts to count from the last index.
     * @param <T>     Type of objects
     *
     * @return sublist.
     */
    public static <T> List<T> getRangeAsList(T[] objects, int from, int to) {
        return getRangeAsList(Arrays.asList(objects), from, to);
    }

    /**
     * Get a sublist from 'from' to list.size()
     *
     * @param objects list of objects.
     * @param from    start index (included). Use negative counts to count from the last index.
     * @param <T>     Type of objects
     *
     * @return sublist.
     */
    public static <T> List<T> getRangeAsList(List<T> objects, int from) {
        return getRangeAsList(objects, from, 0);
    }

    /**
     * Get a sublist of a list.
     *
     * @param objects list of objects.
     * @param from    start index (included). Use negative counts to count from the last index.
     * @param to      end index (excluded). Use negative counts to count from the last index.
     * @param <T>     Type of objects
     *
     * @return sublist.
     */
    public static <T> List<T> getRangeAsList(List<T> objects, int from, int to) {
        int finalTo = to;
        if (to < 1) {
            finalTo = objects.size() + to;
        }
        int finalFrom = from;
        if (from < 0) {
            finalFrom = objects.size() + from;
        }

        if (finalFrom > finalTo || finalFrom < 0 || finalTo > objects.size()) {
            return Collections.emptyList();
        }

        return objects.subList(finalFrom, finalTo);
    }
}
