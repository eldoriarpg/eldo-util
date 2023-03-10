/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.simplecommands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.eldoria.eldoutilities.C;
import de.eldoria.eldoutilities.commands.command.util.CommandAssertions;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import de.eldoria.eldoutilities.utils.Parser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilitiy class to provide util functions for tab completion.
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public final class TabCompleteUtil {
    private static final Set<String> PLAYER_NAMES = new HashSet<>();
    private static final Set<String> ONLINE_NAMES = new HashSet<>();
    public static final long OFFLINE_PLAYER_CACHE_SIZE = 1000L;
    private static Instant lastPlayerRefresh = Instant.now();
    private static final Set<String> SMART_MATS;
    private static final Map<String, List<String>> SMART_SHORT_MATS;
    private static final Map<String, List<String>> SMART_PART_MATS;
    private static final Pattern SHORT_NAME = Pattern.compile("(?:(?:^|_)(.))");
    private static final Cache<String, List<String>> SMART_MAT_RESULTS = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

    private TabCompleteUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    static {
        SMART_MATS = new LinkedHashSet<>();
        SMART_SHORT_MATS = new TreeMap<>();
        SMART_PART_MATS = new TreeMap<>();

        for (var material : Material.values()) {
            var name = material.name();
            if (name.startsWith("LEGACY")) continue;
            SMART_SHORT_MATS.computeIfAbsent(getShortName(material).toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(name);
            SMART_MATS.add(name);
            for (var part : getParts(material)) {
                SMART_PART_MATS.computeIfAbsent(part.toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(name);
            }
        }
    }

    private static String getShortName(Material mat) {
        var matcher = SHORT_NAME.matcher(mat.name());
        var builder = new StringBuilder();
        while (matcher.find()) {
            builder.append(matcher.group(1));
        }
        return builder.toString();
    }

    private static Set<String> getParts(Material mat) {
        return Arrays.stream(mat.name().split("_")).collect(Collectors.toSet());
    }

    /**
     * Complete a material with precomputed result maps
     *
     * @param value     value to complete
     * @param lowerCase true to receive results in lower case
     * @return a list with unique entries
     */
    public static List<String> completeMaterial(String value, boolean lowerCase) {
        value = value.toLowerCase(Locale.ROOT);
        Set<String> results = new LinkedHashSet<>();
        // Smart matches on part have the highest priority
        for (var entry : SMART_SHORT_MATS.entrySet()) {
            if (!entry.getKey().startsWith(value)) continue;
            results.addAll(entry.getValue());
        }

        // Matches on the start of the value have second prio
        results.addAll(complete(value, SMART_MATS));

        // Part matches are nice, but have low priority
        for (var entry : SMART_PART_MATS.entrySet()) {
            if (!entry.getKey().startsWith(value)) continue;
            results.addAll(entry.getValue());
        }

        var finalValue = value.toUpperCase(Locale.ROOT);

        results.addAll(SMART_MATS.stream().filter(mat -> mat.contains(finalValue)).collect(Collectors.toList()));

        return results.stream()
                .map(name -> lowerCase ? name.toLowerCase(Locale.ROOT) : name)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Complete an array of strings.
     *
     * @param value  current value
     * @param inputs possible values
     * @return list of strings
     */
    public static List<String> complete(String value, String... inputs) {
        return ArrayUtil.startingWithInArray(value, inputs)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Complete an stream of strings
     *
     * @param value  current value
     * @param inputs possible values
     * @return list of strings
     */
    public static List<String> complete(String value, Stream<String> inputs) {
        if (value.isEmpty()) return inputs.collect(Collectors.toList());
        var lowerValue = value.toLowerCase(Locale.ROOT);
        return inputs
                .filter(i -> i.toLowerCase().startsWith(lowerValue))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Complete an collection of strings
     *
     * @param value  current value
     * @param inputs possible values
     * @return list of strings
     */
    public static List<String> complete(String value, Collection<String> inputs) {
        return complete(value, inputs.stream());
    }

    /**
     * Complete an object stream.
     *
     * @param value   current value
     * @param inputs  possible values
     * @param mapping mapping of stream objects to string
     * @param <T>     type of stream
     * @return list of strings
     */
    public static <T> List<String> complete(String value, Stream<T> inputs, Function<T, String> mapping) {
        return complete(value, inputs.map(mapping));
    }

    /**
     * Complete a collection of objects
     *
     * @param value   current value
     * @param inputs  possible values
     * @param mapping mapping of collection objects to string
     * @param <T>     type of collection
     * @return list of strings
     */
    public static <T> List<String> complete(String value, Collection<T> inputs, Function<T, String> mapping) {
        return complete(value, inputs.stream(), mapping);
    }

    /**
     * Complete a boolean
     *
     * @param value current value
     * @return list of strings
     */
    public static List<String> completeBoolean(String value) {
        return complete(value, "true", "false");
    }

    /**
     * Complete a world.
     * <p>
     * Will replace spaces with `:`
     *
     * @param value current value
     * @return list of strings
     */
    public static List<String> completeWorlds(String value) {
        return completeWorlds(value, C.SPACE_REPLACE);
    }

    /**
     * Complete a world
     *
     * @param value        current value
     * @param spaceReplace the replacement for spaces
     * @return list of strings
     */
    public static List<String> completeWorlds(String value, String spaceReplace) {
        return complete(value, Bukkit.getWorlds(), w -> w.getName().replace(" ", spaceReplace));
    }

    /**
     * Complete a player
     *
     * @param value current value
     * @return null as this will enable minecraft to standard completion which is nearly always a player
     */
    public static List<String> completePlayers(String value) {
        if (PLAYER_NAMES.isEmpty()) {
            PLAYER_NAMES.addAll(
                    Arrays.stream(Bukkit.getOfflinePlayers())
                            .filter(p -> Instant.ofEpochMilli(p.getLastPlayed()).isAfter(Instant.now().minus(30, ChronoUnit.DAYS)))
                            .sorted(Comparator.comparingLong(OfflinePlayer::getLastPlayed))
                            .limit(OFFLINE_PLAYER_CACHE_SIZE)
                            .map(OfflinePlayer::getName)
                            .collect(Collectors.toSet()));
        }

        Set<String> complete = new LinkedHashSet<>(complete(value, PLAYER_NAMES));
        complete.addAll(completeOnlinePlayers(value));
        return new ArrayList<>(complete);
    }

    /**
     * Complete a player
     *
     * @param value current value
     * @return null as this will enable minecraft to standard completion which is nearly always a player
     */
    public static List<String> completeOnlinePlayers(String value) {
        Set<String> complete = new LinkedHashSet<>(complete(value, PLAYER_NAMES));
        if (ONLINE_NAMES.isEmpty() || lastPlayerRefresh.isBefore(Instant.now().minus(10, ChronoUnit.SECONDS))) {
            ONLINE_NAMES.clear();
            ONLINE_NAMES.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toSet()));
            lastPlayerRefresh = Instant.now();
        }
        complete.addAll(complete(value, ONLINE_NAMES.stream()));
        return new ArrayList<>(complete);
    }


    /**
     * Completes a enum. will return the enum values in lower case with underscores.
     *
     * @param value current value
     * @param clazz enum clazz
     * @param <T>   type of enum
     * @return list of strings
     */
    public static <T extends Enum<T>> List<String> complete(String value, Class<T> clazz) {
        return complete(value, clazz, true, false);
    }

    /**
     * Completes a enum
     *
     * @param value     current value
     * @param clazz     enum clazz
     * @param lowerCase will make values lower case if true
     * @param strip     will strip underscores if true
     * @param <T>       type of enum
     * @return list of strings
     */
    public static <T extends Enum<T>> List<String> complete(String value, Class<T> clazz, boolean lowerCase, boolean strip) {
        return complete(value,
                Arrays.stream(clazz.getEnumConstants())
                        .map(Enum::name)
                        .map(v -> lowerCase ? v.toLowerCase() : v)
                        .map(v -> strip ? v.replace("_", "") : v));
    }

    /**
     * Checks if a value is contained in command
     *
     * @param value   value to check
     * @param command command which should contain value
     * @return true if command contains value
     */
    public static boolean isCommand(String value, String... command) {
        return ArrayUtil.arrayContains(command, value);
    }

    /**
     * Checks if the input is a number and inside the range. Requires {@code error.invalidRange (%MAX%, %MIN%)} and
     * {@code error.invalidNumber} key in locale file
     *
     * @param value current value
     * @param min   min value
     * @param max   max value
     * @return list with range advise or error
     */
    public static List<String> completeDouble(String value, double min, double max) throws CommandException {
        CommandAssertions.isDouble(value);
        var d = Parser.parseDouble(value);
        List<String> result = new ArrayList<>();
        CommandAssertions.range(d.get(), min, max);
        return singleEntryList(min + " » " + max);
    }

    /**
     * Checks if the input is a number and inside the range. Requires {@code error.invalidRange (%MAX%, %MIN%)} and
     * {@code error.invalidNumber} key in locale file
     *
     * @param value current value
     * @param min   min value
     * @param max   max value
     * @return list with range advise or error
     */
    public static List<String> completeInt(String value, int min, int max) throws CommandException {
        CommandAssertions.isInteger(value);
        var d = Parser.parseInt(value);
        CommandAssertions.range(d.get(), min, max);
        return singleEntryList(min + " » " + max);
    }

    /**
     * Checks if the input is a number and inside the range. Requires {@code error.tooSmall (%MIN%)} and
     * {@code error.invalidNumber} key in locale file
     *
     * @param value current value
     * @param min   min value
     * @return list with range advise or error
     */
    public static List<String> completeMinDouble(String value, double min) throws CommandException {
        CommandAssertions.isDouble(value);
        var val = Parser.parseDouble(value);
        CommandAssertions.min(val.get(), min);
        return singleEntryList(String.format("%.2f<", min));
    }

    /**
     * Checks if the input is a number and inside the range. Requires {@code error.tooSmall (%MIN%)} and
     * {@code error.invalidNumber} key in locale file
     *
     * @param value current value
     * @param min   min value
     * @return list with range advise or error
     */
    public static List<String> completeMinInt(String value, int min) throws CommandException {
        CommandAssertions.isInteger(value);
        var val = Parser.parseInt(value);
        CommandAssertions.min(val.get(), min);
        return singleEntryList(min + "<");
    }

    /**
     * Checks if the input is a number and inside the range. Requires {@code error.tooLarge (%MAX%)} and
     * {@code error.invalidNumber} key in locale file
     *
     * @param value current value
     * @param max   max value
     * @return list with range advise or error
     */
    public static List<String> completeMaxDouble(String value, double max) throws CommandException {
        CommandAssertions.isDouble(value);
        var val = Parser.parseDouble(value);
        CommandAssertions.max(val.get(), max);
        return singleEntryList(String.format("%.2f", max) + ">");
    }

    /**
     * Checks if the input is a number and inside the range. Requires {@code error.tooLarge (%MAX%)} and
     * {@code error.invalidNumber} key in locale file
     *
     * @param value current value
     * @param max   max value
     * @return list with range advise or error
     */
    public static List<String> completeMaxInt(String value, int max) throws CommandException {
        CommandAssertions.isInteger(value);
        var val = Parser.parseInt(value);
        CommandAssertions.max(val.get(), max);
        return singleEntryList(max + ">");
    }

    /**
     * Checks if a string is smaller then the current input. Requires {@code error.invalidLength, %MAX%} key in locale
     * file
     *
     * @param value           value to check
     * @param maxLength       max length of string
     * @param defaultComplete default completion output
     * @return list of string with length 1
     */
    public static List<String> completeFreeInput(String value, int maxLength, String defaultComplete) throws CommandException {
        CommandAssertions.invalidLength(value, maxLength);
        return singleEntryList(defaultComplete);
    }

    public static <T> List<T> singleEntryList(T value) {
        List<T> list = new ArrayList<>();
        list.add(value);
        return list;
    }
}
