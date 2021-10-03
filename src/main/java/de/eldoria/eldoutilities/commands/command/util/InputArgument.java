package de.eldoria.eldoutilities.commands.command.util;

public class InputArgument extends Argument{
    private final boolean required;

    public InputArgument(String name, boolean required) {
        super(name);
        this.required = required;
    }

    @Override
    public String formatted() {
        return String.format(required? "<%s>" : "[%s]", name());
    }

    public boolean isRequired() {
        return required;
    }
}
