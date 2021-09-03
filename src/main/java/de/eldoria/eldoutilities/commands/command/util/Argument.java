package de.eldoria.eldoutilities.commands.command.util;

import de.eldoria.eldoutilities.localization.MessageComposer;

public abstract class Argument {
    private final String name;

    protected Argument(String name) {
        this.name = name;
    }

    public abstract String formatted();

    static Argument unlocalizedInput(String name, boolean required) {
        return new InputArgument(name, required);
    }

    static Argument input(String name, boolean required) {
        return new InputArgument(MessageComposer.escape(name), required);
    }

    static Argument subCommand(String name) {
        return new SubCommand(name);
    }

    public String name() {
        return name;
    }

    public abstract boolean isRequired();
}
