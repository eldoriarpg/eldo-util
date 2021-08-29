package de.eldoria.eldoutilities.commands;

import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IPlayerTabExecutor extends TabCompleter {
    void onCommand(@NotNull Player sender, @NotNull String label, @NotNull Arguments arguments) throws CommandException;

    @Nullable
    List<String> onTabComplete(@NotNull Player sender, @NotNull String alias, @NotNull Arguments arguments);
}
