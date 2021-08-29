package de.eldoria.eldoutilities.commands;

import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IConsoleTabExecutor {
    void onCommand(@NotNull ConsoleCommandSender sender, @NotNull String label, @NotNull Arguments arguments) throws CommandException;

    @Nullable
    List<String> onTabComplete(@NotNull ConsoleCommandSender sender, @NotNull String alias, @NotNull Arguments arguments);

}
