package de.eldoria.eldoutilities.localization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class to compose localized messages.
 * Handles escaping and concatenation of messages.
 */
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

    public MessageComposer space() {
        stringBuilder.append(" ");
        return this;
    }

    public MessageComposer space(int spaces) {
        // TODO: waiting for java 11 migration
        stringBuilder.append(IntStream.of(spaces).mapToObj(i -> " ").collect(Collectors.joining()));
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
