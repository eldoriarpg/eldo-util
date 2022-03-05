/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.command.util;

import de.eldoria.eldoutilities.localization.MessageComposer;

public abstract class Argument {
    private final String name;

    protected Argument(String name) {
        this.name = name;
    }

    public abstract String formatted();

    public static Argument unlocalizedInput(String name, boolean required) {
        return new InputArgument(name, required);
    }

    public static Argument input(String name, boolean required) {
        return new InputArgument(MessageComposer.escape(name), required);
    }

    public static Argument subCommand(String name) {
        return new SubCommand(name);
    }

    public String name() {
        return name;
    }

    public abstract boolean isRequired();
}
