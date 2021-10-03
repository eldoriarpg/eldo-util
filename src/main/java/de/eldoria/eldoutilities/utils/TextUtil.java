package de.eldoria.eldoutilities.utils;

/**
 * Basic text utilities
 *
 * @since 1.0.0
 */
public final class TextUtil {
    private TextUtil() {
        throw new UnsupportedOperationException("This is a utility class!");
    }

    /**
     * Count how often the char was used inside the string.
     *
     * @param string string to check
     * @param count  char to count
     * @return the number of occurences of the char in the string.
     */
    public static int countChars(String string, char count) {
        int i = 0;
        for (char c : string.toCharArray()) {
            if (c == count) i++;
        }
        return i;
    }


    /**
     * Trims a text to the desired length. Returns unmodified input if max chars is larger or equal string.length().
     *
     * @param string      String to trim
     * @param endSequence end sequence which should be append at the end of the string. included in max chars.
     * @param maxChars    max char length.
     * @param keepWords   true if no word should be cut.
     * @return String with length of maxChars of shorter.
     */
    public static String cropText(String string, String endSequence, int maxChars, boolean keepWords) {
        if (string.length() <= maxChars) {
            return string;
        }
        if (!keepWords) {
            String substring = string.substring(0, Math.max(0, maxChars - endSequence.length()));
            return (substring + endSequence).trim();
        }

        String[] split = string.split("\\s");

        StringBuilder builder = new StringBuilder();

        for (String s : split) {
            if (builder.length() + s.length() + 1 + endSequence.length() > maxChars) {
                return builder.toString().trim() + endSequence;
            }
            builder.append(s).append(" ");
        }
        return builder.toString().trim();
    }
}
