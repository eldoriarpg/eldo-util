package de.eldoria.eldoutilities.commands.executor;

import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IPlayerTabExecutor {
    void onCommand(@NotNull Player player, @NotNull String label, @NotNull Arguments arguments) throws CommandException;

    @Nullable
    List<String> onTabComplete(@NotNull Player player, @NotNull String alias, @NotNull Arguments arguments);
}
