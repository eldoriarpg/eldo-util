/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.command.util;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class CommandMetaBuilder {

    private final String name;
    private final Set<String> alias = new HashSet<>();
    private final Set<String> permissions = new HashSet<>();
    private final Set<Class<? extends CommandSender>> allowedSender = new HashSet<>();
    private final List<Argument> arguments = new ArrayList<>();
    private final Map<String, AdvancedCommand> subCommands = new HashMap<>();
    private AdvancedCommand defaultCommand;
    private AdvancedCommand parent;
    private boolean hidden = false;

    public CommandMetaBuilder(String name) {
        this.name = name;
    }

    public CommandMetaBuilder withPermission(String... permission) {
        permissions.addAll(Arrays.asList(permission));
        return this;
    }

    public CommandMetaBuilder allowPlayer() {
        allowedSender.add(Player.class);
        return this;
    }

    public CommandMetaBuilder addAlias(String... aliases) {
        alias.addAll(Arrays.asList(aliases));
        return this;
    }

    public CommandMetaBuilder allowConsole() {
        allowedSender.add(ConsoleCommandSender.class);
        return this;
    }

    @SafeVarargs
    public final CommandMetaBuilder allowCommandSender(Class<? extends CommandSender>... senders) {
        allowedSender.addAll(Arrays.asList(senders));
        return this;
    }

    public CommandMetaBuilder addArgument(String name, boolean reqired) {
        arguments.add(Argument.input(name, reqired));
        return this;
    }

    public CommandMetaBuilder addUnlocalizedArgument(String name, boolean reqired) {
        arguments.add(Argument.unlocalizedInput(name, reqired));
        return this;
    }

    public CommandMetaBuilder withArguments(Argument... arguments) {
        this.arguments.addAll(Arrays.asList(arguments));
        return this;
    }

    public CommandMetaBuilder withSubCommand(AdvancedCommand advancedCommand) {
        subCommands.put(advancedCommand.meta().name(), advancedCommand);
        for (var alias : advancedCommand.meta().aliases()) {
            subCommands.put(alias, advancedCommand);
        }
        return this;
    }

    /**
     * Sets the command as hidden. It will not be suggested in tab completion.
     *
     * @return this instance
     */
    public CommandMetaBuilder hidden() {
        hidden = true;
        return this;
    }

    /**
     * Use this consumer when you need more complex command setup.
     * <p>
     * Add subcommands to the provided list.
     * <p>
     * Set the default command via the provided builder
     *
     * @param commandConsumer consumer
     * @return this instance
     */
    public CommandMetaBuilder buildSubCommands(BiConsumer<List<AdvancedCommand>, CommandMetaBuilder> commandConsumer) {
        List<AdvancedCommand> subCommands = new ArrayList<>();
        commandConsumer.accept(subCommands, this);
        for (var subCommand : subCommands) {
            withSubCommand(subCommand);
        }
        return this;
    }

    public CommandMetaBuilder withDefaultCommand(AdvancedCommand defaultCommand) {
        this.defaultCommand = defaultCommand;
        return this;
    }

    public CommandMetaBuilder ofParent(AdvancedCommand parent) {
        this.parent = parent;
        return this;
    }

    public CommandMeta build() {
        return new CommandMeta(name, alias.toArray(new String[0]), permissions, allowedSender, arguments, defaultCommand, subCommands, parent, hidden);
    }

    public CommandMetaBuilder allowCommandSender(Set<Class<? extends CommandSender>> allowedSender) {
        this.allowedSender.addAll(allowedSender);
        return this;
    }

    public CommandMetaBuilder withPermission(Set<String> permissions) {
        this.permissions.addAll(permissions);
        return this;
    }
}
