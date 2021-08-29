package de.eldoria.eldoutilities.commands;

import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ITabExecutor {
    void onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull Arguments arguments) throws CommandException;

    @Nullable
    List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments arguments);
}
