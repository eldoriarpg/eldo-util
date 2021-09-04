package de.eldoria.eldoutilities.localization;

import de.eldoria.eldoutilities.utils.TextUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class to compose localized messages.
 * Handles escaping and concatenation of messages.
 */
@SuppressWarnings("unused")
public class MessageComposer {
    private final StringBuilder stringBuilder = new StringBuilder();
    private final List<Replacement> replacements = new ArrayList<>();

    public static MessageComposer create() {
        return new MessageComposer();
    }

    private MessageComposer() {
    }

    public MessageComposer localeCode(String propertyKey, Replacement... replacements) {
        stringBuilder.append(escape(propertyKey));
        this.replacements.addAll(Arrays.asList(replacements));
        return this;
    }

    /**
     * Add a string to the message.
     * <p>
     * This also allows to use a string format like {@link String#format(String, Object...)}
     *
     * @param text    text to add
     * @param objects objects for placeholder
     * @return this instance
     */
    public MessageComposer text(String text, Object... objects) {
        stringBuilder.append(String.format(text.toString(), objects));
        return this;
    }

    /**
     * Add a object to the message as string.
     *
     * @param object object to add
     * @return this instance
     */
    public MessageComposer text(Object object) {
        stringBuilder.append(object.toString());
        return this;
    }

    /**
     * Add a list of messages with a delimiter as string.
     *
     * @param messages messages to add
     * @return this instance
     */
    public MessageComposer text(Collection<String> messages) {
        return text(messages, "\n");
    }

    /**
     * Add a list of messages with a delimiter as string.
     *
     * @param messages  messages to add
     * @param delimiter delimiter to join
     * @return this instance
     */
    public MessageComposer text(Collection<String> messages, String delimiter) {
        stringBuilder.append(String.join(delimiter, messages));
        return this;
    }

    public MessageComposer space() {
        stringBuilder.append(" ");
        return this;
    }

    public MessageComposer space(int spaces) {
        // TODO: waiting for java 11 migration
        stringBuilder.append(IntStream.of(spaces).mapToObj(i -> " ").collect(Collectors.joining()));
        return this;
    }

    public MessageComposer fillLines() {
        return fillLines(25);
    }

    public MessageComposer fillLines(int lines) {
        int lineCount = TextUtil.countChars(stringBuilder.toString(), '\n');
        // TODO: waiting for java 11 migration
        String newLines = IntStream.of(Math.max(lines - lineCount, 0)).mapToObj(i -> " \n").collect(Collectors.joining());
        stringBuilder.insert(0, newLines);
        return this;
    }

    public MessageComposer newLine() {
        stringBuilder.append(String.format("%n"));
        return this;
    }

    public String build() {
        return stringBuilder.toString();
    }

    public String buildLocalized(ILocalizer localizer) {
        return localizer.localize(stringBuilder.toString(), replacements.toArray(new Replacement[0]));
    }

    public static String escape(String propertyKey) {
        return String.format("$%s$", propertyKey);
    }
}
