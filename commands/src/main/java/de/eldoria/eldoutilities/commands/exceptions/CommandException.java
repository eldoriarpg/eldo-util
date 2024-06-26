/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.exceptions;

import de.eldoria.eldoutilities.localization.ILocalizer;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class CommandException extends RuntimeException {
    private TagResolver replacements;
    private boolean silent;
    private static final boolean INCLUDE_STACKTRACE = Boolean.parseBoolean(System.getProperty("eldoria.exception.commandexception.stacktrace", "true"));

    private CommandException(String message, TagResolver replacements) {
        super(message, null, true, INCLUDE_STACKTRACE);
        this.replacements = replacements;
    }

    private CommandException(boolean silent) {
        super("");
        this.silent = silent;
    }

    public static CommandException message(String message, TagResolver replacements) {
        return new CommandException(message, replacements);
    }

    public static CommandException message(String message, TagResolver... replacements) {
        return new CommandException(message, TagResolver.resolver(replacements));
    }

    public static CommandException message(String message) {
        return new CommandException(message, TagResolver.empty());
    }

    public static CommandException silent() {
        return new CommandException(true);
    }

    public String localized(ILocalizer localizer) {
        return localizer.localize(getMessage());
    }

    public TagResolver replacements() {
        return replacements;
    }

    public boolean isSilent() {
        return silent;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
