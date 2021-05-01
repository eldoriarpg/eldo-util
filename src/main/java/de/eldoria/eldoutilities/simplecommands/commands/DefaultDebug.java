package de.eldoria.eldoutilities.simplecommands.commands;

import de.eldoria.eldoutilities.debug.DebugSettings;
import de.eldoria.eldoutilities.debug.DebugUtil;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class DefaultDebug extends EldoCommand {

    private final String permission;
    private final DebugSettings settings;

    public DefaultDebug(Plugin plugin, String permission, DebugSettings settings) {
        super(plugin);
        this.permission = permission;
        this.settings = settings;
    }

    public DefaultDebug(Plugin plugin, String permission) {
        super(plugin);
        this.permission = permission;
        settings = DebugSettings.DEFAULT;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (denyAccess(sender, permission, "de.eldoria.eldoutilitites.debug")) {
            return true;
        }

        DebugUtil.dispatchDebug(sender, getPlugin(), DebugSettings.DEFAULT);
        return true;
    }
}
