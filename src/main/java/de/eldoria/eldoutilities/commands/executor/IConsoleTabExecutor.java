package de.eldoria.eldoutilities.commands.executor;

import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IConsoleTabExecutor {
    void onCommand(@NotNull ConsoleCommandSender console, @NotNull String label, @NotNull Arguments arguments) throws CommandException;

    @Nullable
    List<String> onTabComplete(@NotNull ConsoleCommandSender console, @NotNull String alias, @NotNull Arguments arguments);

}
