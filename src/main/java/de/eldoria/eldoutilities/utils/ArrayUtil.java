package de.eldoria.eldoutilities.utils;

import com.google.common.collect.ObjectArrays;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This class containts methods to handle arrays and search.
 *
 * @since 1.0.0
 */
public final class ArrayUtil {

    private ArrayUtil() {
        throw new UnsupportedOperationException("This is a utility class!");
    }

    /**
     * Checks if a string in a array matches a pattern.
     *
     * @param strings  strings to check
     * @param contains pattern to check against
     * @return matcher instance wich matches the string
     */
    public static Matcher findInArray(String[] strings, Pattern contains) {
        for (String string : strings) {
            Matcher matcher = contains.matcher(string);
            if (matcher.find()) return matcher;
        }
        return null;
    }

    /**
     * Checks if a array contains any of the values.
     *
     * @param strings string to check
     * @param values  one or more value to check agains
     * @return true if a match was found
     */
    public static boolean arrayContains(String[] strings, String... values) {
        for (String string : strings) {
            for (String contain : values) {
                if (string.equalsIgnoreCase(contain)) return true;
            }
        }
        return false;
    }

    /**
     * Checks if a array contains any of the values.
     *
     * @param chars  chars to check
     * @param values one or more value to check agains
     * @return true if a match was found
     */
    public static boolean arrayContains(char[] chars, char... values) {
        for (char character : chars) {
            for (char contain : values) {
                if (character == contain) return true;
            }
        }
        return false;
    }

    /**
     * Combines two or more arrays.
     *
     * @param array  array to combine
     * @param arrays arrays to combine
     * @return one array
     */
    public static String[] combineArrays(String[] array, String[]... arrays) {
        String[] result = array;
        for (String[] arr : arrays) {
            result = ObjectArrays.concat(arr, result, String.class);
        }
        return result;
    }

    /**
     * Combines two or more arrays.
     *
     * @param array  array to combine
     * @param arrays arrays to combine
     * @param clazz  clazz of array
     * @param <T>    type of array
     * @return one array
     */
    public static <T> T[] combineArrays(Class<T> clazz, T[] array, T[]... arrays) {
        T[] result = array;
        for (T[] arr : arrays) {
            result = ObjectArrays.concat(arr, result, clazz);
        }
        return result;
    }

    /**
     * Searches for strings, which are starting with the provided value
     *
     * @param value start to search for
     * @param array array to check
     * @return list of strings which starts with the provided value
     */
    public static Stream<String> startingWithInArray(String value, String[] array) {
        return Arrays.stream(array).filter(e -> e.toLowerCase().startsWith(value.toLowerCase()));
    }

    /**
     * Checks if a string start with any value in a string.
     *
     * @param value value to check
     * @param array array values.
     * @return true if the value starts with any value in the array
     */
    public static boolean stringStartingWithValueInArray(String value, String[] array) {
        return Arrays.stream(array).anyMatch(value::startsWith);
    }

    /**
     * Checks if a string ends with a value in a array
     *
     * @param value value to check
     * @param array array values.
     * @return true if the value ends with any value in the array
     */
    public static boolean endingWithInArray(String value, String[] array) {
        return Arrays.stream(array).anyMatch(value::endsWith);
    }
}
