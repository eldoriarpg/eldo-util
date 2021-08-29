package de.eldoria.eldoutilities.commands.command.util;

public class Argument {
    private final String name;
    private final boolean required;

    public Argument(String name, boolean required) {
        this.name = name;
        this.required = required;
    }

    public static Argument of(String name, boolean required){
        return new Argument(name, required);
    }

    public String name() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }
}
