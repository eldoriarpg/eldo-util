package de.eldoria.eldoutilities.simplecommands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.internal.LinkedHashTreeMap;
import de.eldoria.eldoutilities.C;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import de.eldoria.eldoutilities.utils.Parser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilitiy class to provide util functions for tab completion.
 *
 * @since 1.0.0
 */
public final class TabCompleteUtil {
    private static final Set<String> smartMats;
    private static final Map<String, List<String>> smartShortMats;
    private static final Map<String, List<String>> smartPartMats;
    private static final Pattern SHORT_NAME = Pattern.compile("(?:(?:^|_)(.))");
    private static final Cache<String, List<String>> SMART_MAT_RESULTS = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

    private TabCompleteUtil() {
    }

    static {
        smartMats = new LinkedHashSet<>();
        smartShortMats = new LinkedHashTreeMap<>();
        smartPartMats = new LinkedHashTreeMap<>();

        for (Material material : Material.values()) {
            String name = material.name();
            if (name.startsWith("LEGACY")) continue;
            smartShortMats.computeIfAbsent(getShortName(material).toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(name);
            smartMats.add(name);
            for (String part : getParts(material)) {
                smartPartMats.computeIfAbsent(part.toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(name);
            }
        }
    }

    private static String getShortName(Material mat) {
        Matcher matcher = SHORT_NAME.matcher(mat.name());
        StringBuilder builder = new StringBuilder();
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
        for (Map.Entry<String, List<String>> entry : smartShortMats.entrySet()) {
            if (!entry.getKey().startsWith(value)) continue;
            results.addAll(entry.getValue());
        }

        // Matches on the start of the value have second prio
        results.addAll(complete(value, smartMats));

        // Part matches are nice, but have low priority
        for (Map.Entry<String, List<String>> entry : smartPartMats.entrySet()) {
            if (!entry.getKey().startsWith(value)) continue;
            results.addAll(entry.getValue());
        }

        String finalValue = value;

        results.addAll(smartMats.stream().filter(mat -> mat.contains(finalValue)).collect(Collectors.toList()));

        return results.stream().map(name -> lowerCase ? name.toLowerCase(Locale.ROOT) : name).collect(Collectors.toList());
    }

    /**
     * Complete an array of strings.
     *
     * @param value  current value
     * @param inputs possible values
     * @return list of strings
     */
    public static List<String> complete(String value, String... inputs) {
        return ArrayUtil.startingWithInArray(value, inputs).collect(Collectors.toList());
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
        return inputs
                .filter(i -> i.toLowerCase().startsWith(value))
                .collect(Collectors.toList());
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
    @Nullable
    public static List<String> completePlayers(String value) {
        return null;
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
     * @param loc   localizer instance
     * @return list with range advise or error
     */
    public static List<String> completeDouble(String value, double min, double max, ILocalizer loc) {
        OptionalDouble d = Parser.parseDouble(value);
        if (d.isPresent()) {
            if (d.getAsDouble() > max || d.getAsDouble() < min) {
                return Collections.singletonList(loc.getMessage("error.invalidRange",
                        Replacement.create("MIN", min).addFormatting('6'),
                        Replacement.create("MAX", max).addFormatting('6')));

            }
            return Collections.singletonList(min + "-" + max);
        }
        return Collections.singletonList(loc.getMessage("error.invalidNumber"));
    }

    /**
     * Checks if the input is a number and inside the range. Requires {@code error.invalidRange (%MAX%, %MIN%)} and
     * {@code error.invalidNumber} key in locale file
     *
     * @param value current value
     * @param min   min value
     * @param max   max value
     * @param loc   localizer instance
     * @return list with range advise or error
     */
    public static List<String> completeInt(String value, int min, int max, ILocalizer loc) {
        OptionalInt d = Parser.parseInt(value);
        if (d.isPresent()) {
            if (d.getAsInt() > max || d.getAsInt() < min) {
                return Collections.singletonList(loc.getMessage("error.invalidRange",
                        Replacement.create("MIN", min).addFormatting('6'),
                        Replacement.create("MAX", max).addFormatting('6')));

            }
            return Collections.singletonList(min + "-" + max);
        }
        return Collections.singletonList(loc.getMessage("error.invalidNumber"));
    }

    /**
     * Checks if a string is smaller then the current input. Requires {@code error.invalidLength, %MAX%} key in locale
     * file
     *
     * @param value           value to check
     * @param maxLength       max length of string
     * @param defaultComplete default completion output
     * @param loc             localizer instance
     * @return list of string with length 1
     */
    public static List<String> completeFreeInput(String value, int maxLength, String defaultComplete, ILocalizer loc) {
        if (value.length() > maxLength) {
            return Collections.singletonList(loc.getMessage("error.invalidLength",
                    Replacement.create("MAX", maxLength).addFormatting('6')));
        }
        return Collections.singletonList(defaultComplete);
    }
}
