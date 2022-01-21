/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.exceptions;

import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.localization.Replacement;

public class CommandException extends Exception {
    private Replacement[] replacements;
    private boolean silent;

    private CommandException(String message, Replacement... replacements) {
        super(message);
        this.replacements = replacements;
    }

    private CommandException(boolean silent) {
        super("");
        this.silent = silent;
    }

    public static CommandException message(String message, Replacement... replacements) {
        return new CommandException(message, replacements);
    }

    public static CommandException silent() {
        return new CommandException(true);
    }

    public String localized(ILocalizer localizer) {
        return localizer.localize(getMessage(), replacements);
    }

    public Replacement[] replacements() {
        return replacements;
    }

    public boolean isSilent() {
        return silent;
    }
}
