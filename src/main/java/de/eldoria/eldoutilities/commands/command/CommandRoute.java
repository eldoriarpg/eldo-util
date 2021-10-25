package de.eldoria.eldoutilities.commands.command;

import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public interface CommandRoute {
    default void commandRoute(CommandSender sender, String label, Arguments arguments) throws CommandException {
        throw CommandException.message("No command type is implemented");
    }

    default @Nullable List<String> tabCompleteRoute(CommandSender sender, String label, Arguments arguments) throws CommandException {
        return Collections.emptyList();
    }
}
