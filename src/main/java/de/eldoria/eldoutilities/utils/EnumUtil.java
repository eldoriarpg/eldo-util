package de.eldoria.eldoutilities.utils;

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
     * @param string enum as string value
     * @param values enum values.
     * @param <T>    type of enum.
     * @return enum value or null if no mathing value was found.
     */
    public static <T extends Enum<T>> T parse(String string, Class<T> values) {
        return parse(string, values, false);
    }

    /**
     * Searches for a enum value by string with a case insensitive search.
     *
     * @param string       enum as string value
     * @param values       enum values.
     * @param stripStrings if true underscores will be removed before checking
     * @param defaultValue Default value which will be returned when enum could not be parsed
     * @param <T>          type of enum.
     * @return enum value or default when value was null
     */
    public static <T extends Enum<T>> T parse(String string, Class<T> values, boolean stripStrings, T defaultValue) {
        return ObjUtil.nonNull(parse(string, values, stripStrings), defaultValue);
    }

    /**
     * Searches for a enum value by string with a case insensitive search.
     *
     * @param string       enum as string value
     * @param values       enum values.
     * @param defaultValue Default value which will be returned when enum could not be parsed
     * @param <T>          type of enum.
     * @return enum value or default when value was null
     */
    public static <T extends Enum<T>> T parse(String string, Class<T> values, T defaultValue) {
        return ObjUtil.nonNull(parse(string, values, false), defaultValue);
    }

    /**
     * Searches for a enum value by string with a case insensitive search.
     *
     * @param string       enum as string value
     * @param values       enum values.
     * @param stripStrings if true underscores will be removed before checking
     * @param <T>          type of enum.
     * @return enum value or null if no mathing value was found.
     */
    public static <T extends Enum<T>> T parse(String string, Class<T> values, boolean stripStrings) {
        for (T value : values.getEnumConstants()) {
            if (string.equalsIgnoreCase(stripStrings ? value.name().replace("_", "") : value.name())) {
                return value;
            }
        }
        return null;
    }
}
