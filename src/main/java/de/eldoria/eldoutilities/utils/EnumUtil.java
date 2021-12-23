package de.eldoria.eldoutilities.utils;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class contains methods to parse string to enums.
 *
 * @since 1.0.0
 */
public final class EnumUtil {
    private EnumUtil() {
        throw new UnsupportedOperationException("This is a utility class!");
    }

    /**
     * Searches for a enum value by string with a case insensitive search.
     *
     * @param value enum as string value
     * @param values enum values.
     * @param <T>    type of enum.
     * @return enum value or null if no mathing value was found.
     */
    public static <T extends Enum<T>> Optional<T> parse(String value, Class<T> values) {
        return parse(value, values, false);
    }

    /**
     * Searches for a enum value by string with a case insensitive search.
     *
     * @param value       enum as string value
     * @param values       enum values.
     * @param stripStrings if true underscores will be removed before checking
     * @param defaultValue Default value which will be returned when enum could not be parsed
     * @param <T>          type of enum.
     * @return enum value or default when value was null
     */
    public static <T extends Enum<T>> T parse(String value, Class<T> values, boolean stripStrings, T defaultValue) {
        return parse(value, values, stripStrings).orElse(defaultValue);
    }

    /**
     * Searches for a enum value by string with a case insensitive search.
     *
     * @param value       enum as string value
     * @param values       enum values.
     * @param defaultValue Default value which will be returned when enum could not be parsed
     * @param <T>          type of enum.
     * @return enum value or default when value was null
     */
    public static <T extends Enum<T>> T parse(String value, Class<T> values, T defaultValue) {
        return parse(value, values, false).orElse(defaultValue);
    }

    /**
     * Searches for a enum value by string with a case insensitive search.
     *
     * @param mat       enum as string value
     * @param values       enum values.
     * @param stripStrings if true underscores will be removed before checking
     * @param <T>          type of enum.
     * @return enum value or null if no mathing value was found.
     */
    public static <T extends Enum<T>> Optional<T> parse(String mat, Class<T> values, boolean stripStrings) {
        for (var value : values.getEnumConstants()) {
            if (mat.equalsIgnoreCase(stripStrings ? value.name().replace("_", "") : value.name())) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    public static <T extends Enum<T>> String enumValues(Class<T> clazz) {
        return enumValues(clazz, ", ");
    }

    public static <T extends Enum<T>> String enumValues(Class<T> clazz, String delimiter){
       return Arrays.stream(clazz.getEnumConstants())
                .map(e -> e.name().toLowerCase())
                .collect(Collectors.joining(delimiter));
    }
}
