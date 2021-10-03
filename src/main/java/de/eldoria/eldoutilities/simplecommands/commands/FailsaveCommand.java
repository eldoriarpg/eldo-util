package de.eldoria.eldoutilities.simplecommands.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class FailsaveCommand extends DefaultDebug {
    public FailsaveCommand(Plugin plugin, String permission) {
        super(plugin, permission);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cThe plugin failed to load correctly. Please use /" + label + "§c debug to create a debug paste and send it to the developer.");
            return true;
        }
        if ("debug".equalsIgnoreCase(args[0])) {
            super.onCommand(sender, command, label, args);
            return true;
        }
        sender.sendMessage("§cThe plugin failed to load correctly. Please use §a/" + label + "§c debug to create a debug paste and send it to the developer.");
        return true;
    }
}
