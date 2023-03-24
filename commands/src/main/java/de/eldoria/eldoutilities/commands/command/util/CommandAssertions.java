/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.command.util;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.messages.Replacement;
import de.eldoria.eldoutilities.utils.EnumUtil;
import de.eldoria.eldoutilities.utils.Parser;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Util class to throw exceptions based on conditions.
 */
@SuppressWarnings("unused")
public final class CommandAssertions {

    private CommandAssertions() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static void unexpectedRouteEnd(CommandMeta meta, Arguments arguments) throws CommandException {
        if (arguments.isEmpty() && meta.defaultCommand() == null) {
            var subcommands = meta.subCommands().values().stream().map(c -> "/" + c.meta().createCommandCall()).collect(Collectors.joining("\n"));
            throw CommandException.message("error.endOfRoute", Replacement.create("COMMANDS", subcommands, Style.style(NamedTextColor.GOLD)));
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static void unexpectedRouteEnd(CommandMeta meta, Optional<AdvancedCommand> command) throws CommandException {
        if (command.isEmpty()) {
            var subcommands = meta.subCommands().values().stream().map(c -> "/" + c.meta().createCommandCall()).collect(Collectors.joining("\n"));
            throw CommandException.message("error.endOfRoute", Replacement.create("COMMANDS", subcommands, Style.style(NamedTextColor.GOLD)));
        }
    }

    /**
     * Checks that the arguments have the required length
     *
     * @param meta meta
     * @param args arguments
     * @throws CommandException when the arguments are not sufficient.
     */
    public static void invalidArguments(CommandMeta meta, String[] args) throws CommandException {
        isFalse(args.length < meta.requiredArguments(), "error.invalidArguments",
                Replacement.create("SYNTAX", meta.name() + " " + meta.argumentString(), Style.style(NamedTextColor.GOLD)));
    }

    /**
     * Checks that the arguments have the required length
     *
     * @param meta meta
     * @param args arguments
     * @throws CommandException when the arguments are not sufficient.
     */
    public static void invalidArguments(CommandMeta meta, Arguments args) throws CommandException {
        isFalse(args.size() < meta.requiredArguments(), "error.invalidArguments",
                Replacement.create("SYNTAX", meta.createCommandCall() + " " + meta.argumentString(), Style.style(NamedTextColor.GOLD)));
    }

    /**
     * Checks that the arguments have the required length
     *
     * @param meta      meta
     * @param args      arguments
     * @param arguments arguments which are required and optional in correct order
     * @throws CommandException when the arguments are not sufficient.
     */
    public static void invalidArguments(CommandMeta meta, Arguments arguments, Argument... args) throws CommandException {
        var required = Arrays.stream(args).filter(Argument::isRequired).count();
        var argumentString = Arrays.stream(args).map(Argument::formatted).collect(Collectors.joining(" "));
        isFalse(arguments.size() < required, "error.invalidArguments",
                Replacement.create("SYNTAX", meta.createCommandCall() + " " + argumentString, Style.style(NamedTextColor.GOLD)));
    }

    /**
     * Checks that the arguments have the required length.
     * <p>
     * This method will not create a command call chain.
     *
     * @param args      arguments
     * @param arguments arguments which are required and optional in correct order
     * @throws CommandException when the arguments are not sufficient.
     * @see CommandAssertions#invalidArguments(CommandMeta, String[])
     */
    public static void invalidArguments(Arguments arguments, Argument... args) throws CommandException {
        var required = Arrays.stream(args).filter(Argument::isRequired).count();
        var argumentString = Arrays.stream(args).map(Argument::formatted).collect(Collectors.joining(" "));
        isFalse(arguments.size() < required, "error.invalidArguments",
                Replacement.create("SYNTAX", argumentString, Style.style(NamedTextColor.GOLD)));
    }

    /**
     * Assert that the sender is a player
     *
     * @param sender sender
     * @throws CommandException when the sender is not a player
     */
    public static void player(CommandSender sender) throws CommandException {
        isTrue(sender instanceof Player, "error.onlyPlayer");
    }

    /**
     * Assert that the sender is of type {@link ConsoleCommandSender}.
     *
     * @param sender sender
     * @throws CommandException when the sender is not the console
     */
    public static void console(CommandSender sender) throws CommandException {
        isTrue(sender instanceof ConsoleCommandSender, "error.onlyConsole");
    }

    /**
     * Assert that the sender is one of the {@link CommandMeta#allowedSender()}
     *
     * @param sender sender
     * @param meta   CommandMeta
     * @throws CommandException when the sender is not listed
     */
    public static void sender(CommandSender sender, CommandMeta meta) throws CommandException {
        for (var clazz : meta.allowedSender()) {
            if (sender.getClass().isInstance(clazz)) return;
        }
        throw CommandException.message("Invalid sender");
    }

    /**
     * Checks if the user has at least one of the permissions in {@link CommandMeta#permissions()}.
     *
     * @param sender sender
     * @param meta   command meta
     * @param silent true if the permission error should not be reported
     * @throws CommandException when the user has none of the required permissions
     */
    public static void permission(CommandSender sender, CommandMeta meta, boolean silent) throws CommandException {
        permission(sender, silent, meta.permissions().toArray(new String[0]));
    }

    /**
     * Checks if the user has at least one of the permissions in {@link CommandMeta#permissions()}.
     *
     * @param sender      sender
     * @param silent      true if the permission error should not be reported
     * @param permissions permissions to check
     * @throws CommandException when the user has none of the required permissions
     */
    public static void permission(CommandSender sender, boolean silent, String... permissions) throws CommandException {
        if (permissions.length == 0) return;
        for (var permission : permissions) {
            if (sender.hasPermission(permission)) return;
        }
        if (silent) {
            throw CommandException.silent();
        }
        throw CommandException.message("error.permission", Replacement.create("permission", String.join(", ", permissions)));
    }

    /**
     * Checks if a value is in a invalid Range.
     *
     * @param value current value
     * @param min   min value
     * @param max   max value
     */
    public static void range(double value, double min, double max) throws CommandException {
        isTrue(value <= max && value >= min, "error.invalidRange",
                Replacement.create("MIN", min, Style.style(NamedTextColor.GOLD)),
                Replacement.create("MAX", max, Style.style(NamedTextColor.GOLD)));
    }

    /**
     * Checks if a value is in a invalid Range.
     *
     * @param value current value
     * @param min   min value
     * @param max   max value
     */
    public static void range(int value, int min, int max) throws CommandException {
        isTrue(value <= max && value >= min, "error.invalidRange",
                Replacement.create("MIN", min, Style.style(NamedTextColor.GOLD)),
                Replacement.create("MAX", max, Style.style(NamedTextColor.GOLD)));
    }

    /**
     * Checks if a value value is larger.
     *
     * @param value current value
     * @param min   min value
     */
    public static void min(int value, int min) throws CommandException {
        isTrue(value >= min, "error.tooLow",
                Replacement.create("MIN", min, Style.style(NamedTextColor.GOLD)));
    }

    /**
     * Checks if a value is larger.
     *
     * @param value current value
     * @param min   min value
     */
    public static void min(double value, double min) throws CommandException {
        isTrue(value >= min, "error.tooLow",
                Replacement.create("MIN", min, Style.style(NamedTextColor.GOLD)));
    }

    /**
     * Checks if a value is smaller.
     *
     * @param value current value
     * @param max   max value (inclusive)
     */
    public static void max(int value, int max) throws CommandException {
        isTrue(value <= max, "error.tooLarge",
                Replacement.create("MAX", max, Style.style(NamedTextColor.GOLD)));
    }

    /**
     * Checks if a value is smaller.
     *
     * @param value current value
     * @param max   max value (inclusive)
     */
    public static void max(double value, double max) throws CommandException {
        isTrue(value <= max, "error.tooLarge",
                Replacement.create("MAX", max, Style.style(NamedTextColor.GOLD)));
    }

    /**
     * Checks if a enum value is invalid. Will send a message based on {@code error.invalidEnumValue} locale key which
     * should provide a placeholder for {@code VALUE} which will be replaced with an array of possible inputs
     *
     * @param value value of enum
     * @param clazz clazz of enum
     * @param <T>   type of enum
     */
    public static <T extends Enum<T>> void enumValue(String value, Class<T> clazz) throws CommandException {
        isTrue(EnumUtil.parse(value, clazz).isPresent(), "error.invalidEnumValue", Replacement.create("VALUES",
                Replacement.create("VALUES", EnumUtil.enumValues(clazz), Style.style(NamedTextColor.GOLD))));
    }

    public static void invalidNumber() throws CommandException {
        throw CommandException.message("error.invalidNumber");
    }

    public static void isInteger(String value) throws CommandException {
        isTrue(Parser.parseInt(value).isPresent(), "error.invalidNumber");
    }

    public static void isLong(String value) throws CommandException {
        isTrue(Parser.parseLong(value).isPresent(), "error.invalidNumber");
    }

    public static void isDouble(String value) throws CommandException {
        isTrue(Parser.parseDouble(value).isPresent(), "error.invalidNumber");
    }

    public static void isBoolean(String value) throws CommandException {
        isTrue(Parser.parseBoolean(value).isPresent(), "error.invalidBoolen");
    }

    public static void isBoolean(String value, String isTrue, String isFalse) throws CommandException {
        isTrue(Parser.parseBoolean(value, isTrue, isFalse).isPresent(), "error.invalidBoolen");
    }

    public static void invalidLength(String input, int max) throws CommandException {
        isTrue(input.length() < max, "error.invalidLength", Replacement.create("max", max));
    }

    public static void missingArgument(String[] args, int index) throws CommandException {
        isTrue(args.length > index, "error.missingArgument", Replacement.create("index", index));
    }

    public static void missingArgument(Collection<?> args, int index) throws CommandException {
        isTrue(args.size() > index, "error.missingArgument", Replacement.create("index", index));
    }

    /**
     * Throws a command exception when the evaluation is true
     *
     * @param eval         value
     * @param message      message to send
     * @param replacements replacements
     * @throws CommandException when eval is true
     */
    public static void isFalse(boolean eval, String message, TagResolver replacements) throws CommandException {
        isTrue(!eval, message, replacements);
    }

    /**
     * Throws a command exception when the evaluation is false.
     *
     * @param eval         value
     * @param message      message to send
     * @param replacements replacements
     * @throws CommandException when eval is false
     */
    public static void isTrue(boolean eval, String message, TagResolver... replacements) throws CommandException {
        if (eval) return;
        throw CommandException.message(message, TagResolver.resolver(replacements));
    }

    public static void evalAssertSilent(boolean eval) throws CommandException {
        if (!eval) return;
        throw CommandException.silent();
    }

    public static void allowedSender(CommandMeta meta, CommandSender sender) throws CommandException {
        if (meta.allowedSender().isEmpty()) return;
        if (meta.allowedSender().contains(sender.getClass())) return;
        throw CommandException.message("error.invalidSender");
    }
}
