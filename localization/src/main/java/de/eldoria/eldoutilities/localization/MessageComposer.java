/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.localization;

import de.eldoria.eldoutilities.utils.TextUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
    private final List<TagResolver> replacements = new ArrayList<>();

    private MessageComposer() {
    }

    public static MessageComposer create() {
        return new MessageComposer();
    }

    public static String escape(String propertyKey) {
        return String.format("<l18n:%s>", propertyKey);
    }

    public MessageComposer localeCode(String propertyKey, TagResolver... replacements) {
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
        stringBuilder.append(String.format(text, objects));
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
        stringBuilder.append("".repeat(spaces));
        return this;
    }

    public MessageComposer fillLines() {
        return fillLines(25);
    }

    public MessageComposer fillLines(int lines) {
        var lineCount = TextUtil.countChars(stringBuilder.toString(), '\n') + 1;
        prependLines(Math.max(lines - lineCount, 0));
        return this;
    }

    public MessageComposer prependLines(int lines) {
        stringBuilder.insert(0, "\n".repeat(lines));
        return this;
    }

    public MessageComposer prependLines() {
        prependLines(25);
        return this;
    }

    public MessageComposer newLine() {
        stringBuilder.append("\n");
        return this;
    }

    public String build() {
        return stringBuilder.toString();
    }

    public List<TagResolver> replacements() {
        return Collections.unmodifiableList(replacements);
    }

    public String buildLocalized(ILocalizer localizer) {
        return localizer.localize(stringBuilder.toString());
    }
}
