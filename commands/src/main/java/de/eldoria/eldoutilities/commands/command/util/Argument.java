/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.command.util;

import de.eldoria.eldoutilities.localization.MessageComposer;

public abstract class Argument {
    private final String name;

    protected Argument(String name) {
        this.name = name;
    }

    public static Argument unlocalizedInput(String name, boolean required) {
        return new InputArgument(name, required);
    }

    public static Argument input(String name, boolean required) {
        return new InputArgument(MessageComposer.escape(name), required);
    }

    public static Argument subCommand(String name) {
        return new SubCommand(name);
    }

    public abstract String formatted();

    public String name() {
        return name;
    }

    public abstract boolean isRequired();
}
