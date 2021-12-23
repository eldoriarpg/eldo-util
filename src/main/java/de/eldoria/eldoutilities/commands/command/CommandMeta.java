package de.eldoria.eldoutilities.commands.command;

import de.eldoria.eldoutilities.commands.command.util.Argument;
import de.eldoria.eldoutilities.commands.command.util.CommandMetaBuilder;
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
    private final List<Argument> arguments;
    private final AdvancedCommand defaultCommand;
    private final Map<String, AdvancedCommand> subCommands;
    private final Set<String> registeredCommands;
    private final boolean hidden;
    private final int requiredArguments;
    private AdvancedCommand parent;

    public CommandMeta(String name, String[] aliases, Set<String> permissions, Set<Class<? extends CommandSender>> allowedSender, List<Argument> arguments,
                       AdvancedCommand defaultCommand, Map<String, AdvancedCommand> subCommands, AdvancedCommand parent, boolean hidden) {
        this.name = name;
        this.aliases = aliases;
        this.permissions = permissions;
        this.allowedSender = allowedSender;
        this.arguments = arguments;
        this.defaultCommand = defaultCommand;
        this.subCommands = subCommands;
        registeredCommands = subCommands.entrySet().stream()
                .filter(e -> !e.getValue().meta().isHidden())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        this.parent = parent;
        this.hidden = hidden;
        requiredArguments = (int) arguments().stream().filter(Argument::isRequired).count();
    }

    /**
     * Create a command call route based on this command.
     * <p>
     * This method traced back to the parent commands until the end is reached.
     *
     * @return a string containing all commands of the route in the correct order.
     */
    public String createCommandCall() {
        List<String> calls = new ArrayList<>();
        calls.add(name);
        var curr = parent();
        while (curr != null) {
            calls.add(curr.meta().name());
            curr = curr.meta().parent();
        }
        Collections.reverse(calls);
        return String.join(" ", calls);
    }

    /**
     * Get the name of the command
     *
     * @return name of command
     */
    public String name() {
        return name;
    }

    /**
     * A set of permissions, which may be required for this command.
     *
     * @return set of permissions
     */
    public Set<String> permissions() {
        return Collections.unmodifiableSet(permissions);
    }

    /**
     * Set of command senders which are allowed to use this command
     *
     * @return set of command senders
     */
    public Set<Class<? extends CommandSender>> allowedSender() {
        return Collections.unmodifiableSet(allowedSender);
    }

    /**
     * Ordered list of arguments. The list is ordered by creation order.
     *
     * @return list of arguments
     */
    public List<Argument> arguments() {
        return Collections.unmodifiableList(arguments);
    }

    public AdvancedCommand defaultCommand() {
        return defaultCommand;
    }

    public Map<String, AdvancedCommand> subCommands() {
        return Collections.unmodifiableMap(subCommands);
    }

    public Set<String> registeredCommands() {
        return Collections.unmodifiableSet(registeredCommands);
    }

    public int requiredArguments() {
        return requiredArguments;
    }

    public String argumentString() {
        return arguments.stream().map(Argument::formatted).collect(Collectors.joining(" "));
    }

    protected void parent(AdvancedCommand parent) {
        this.parent = parent;
    }

    public AdvancedCommand parent() {
        return parent;
    }

    public boolean isCommand(String value) {
        if (value.equalsIgnoreCase(name())) return true;
        for (var alias : aliases) {
            if (alias.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    public static CommandMetaBuilder builder(String name) {
        return new CommandMetaBuilder(name);
    }

    /**
     * Creates a meta for a internal subcommand. This is for small commands only and has several caveats.
     * <p>
     * Only set arguments on this builder. Everything else wont have any effect.
     *
     * @param name   name of internal subcommand
     * @param parent parent command which contains this subcommand
     * @return a builder which allows to add arguments.
     */
    public CommandMetaBuilder forSubCommand(String name, AdvancedCommand parent) {
        return new CommandMetaBuilder(name).ofParent(parent);
    }

    public String[] aliases() {
        return aliases.clone();
    }

    public boolean isHidden() {
        return hidden;
    }
}
