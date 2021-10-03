package de.eldoria.eldoutilities.commands.command.util;

public class SubCommand extends Argument{
    protected SubCommand(String name) {
        super(name);
    }

    @Override
    public String formatted() {
        return name();
    }

    @Override
    public boolean isRequired() {
        return true;
    }
}
