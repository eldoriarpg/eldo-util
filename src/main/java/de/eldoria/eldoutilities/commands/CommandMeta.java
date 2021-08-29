package de.eldoria.eldoutilities.commands;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandMeta {
    private final String name;
    private final String[] aliases;
    private final Set<String> permissions;
    private final Set<Class<? extends CommandSender>> allowedSender;
    private final List<CommandArgument> arguments;
    private final AdvancedCommand defaultCommand;
    private final Map<String, AdvancedCommand> subCommands;
    private final Set<String> registeredCommands;
    private final int requiredArguments;
    private AdvancedCommand parent;

    public CommandMeta(String name, String[] aliases, Set<String> permissions, Set<Class<? extends CommandSender>> allowedSender, List<CommandArgument> arguments,
                       AdvancedCommand defaultCommand, Map<String, AdvancedCommand> subCommands) {
        this.name = name;
        this.aliases = aliases;
        this.permissions = permissions;
        this.allowedSender = allowedSender;
        this.arguments = arguments;
        this.defaultCommand = defaultCommand;
        this.subCommands = subCommands;
        registeredCommands = subCommands.keySet();
        requiredArguments = (int) arguments().stream().filter(CommandArgument::isRequired).count();
    }

    public String createCommandCall() {
        List<String> calls = new ArrayList<>();
        calls.add(name);
        AdvancedCommand curr = parent();
        while (curr != null) {
            calls.add(curr.meta().name());
            curr = curr.meta().parent();
        }
        Collections.reverse(calls);
        return String.join(" ", calls);
    }

    public String name() {
        return name;
    }

    public Set<String> permissions() {
        return permissions;
    }

    public Set<Class<? extends CommandSender>> allowedSender() {
        return allowedSender;
    }

    public List<CommandArgument> arguments() {
        return arguments;
    }

    public AdvancedCommand defaultCommand() {
        return defaultCommand;
    }

    public Map<String, AdvancedCommand> subCommands() {
        return subCommands;
    }

    public Set<String> registeredCommands() {
        return registeredCommands;
    }

    public int requiredArguments() {
        return requiredArguments;
    }

    public String argumentString() {
        return arguments.stream().map(arg -> String.format(arg.isRequired() ? "<%s>" : "[%s]", arg.name())).collect(Collectors.joining(" "));
    }

    protected void parent(AdvancedCommand parent) {
        this.parent = parent;
    }

    public AdvancedCommand parent() {
        return parent;
    }

    public boolean isCommand(String value) {
        if(value.equalsIgnoreCase(name())) return true;
        for (String alias : aliases) {
            if(alias.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

}
