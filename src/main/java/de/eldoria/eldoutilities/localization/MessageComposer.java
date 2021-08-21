package de.eldoria.eldoutilities.localization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public MessageComposer text(Object object) {
        stringBuilder.append(object);
        return this;
    }

    public MessageComposer space() {
        stringBuilder.append(" ");
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
