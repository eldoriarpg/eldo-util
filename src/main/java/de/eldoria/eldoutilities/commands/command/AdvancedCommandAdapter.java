package de.eldoria.eldoutilities.commands.command;

import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.localization.DummyLocalizer;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Wraps a {@link AdvancedCommand} into a {@link TabExecutor}
 */
public class AdvancedCommandAdapter implements TabExecutor {
    private final Plugin plugin;
    private final AdvancedCommand advancedCommand;
    private ILocalizer localizer;
    private MessageSender messageSender;

    public static AdvancedCommandAdapter wrap(Plugin plugin, AdvancedCommand advancedCommand) {
        return new AdvancedCommandAdapter(plugin, advancedCommand);
    }

    private AdvancedCommandAdapter(Plugin plugin, AdvancedCommand advancedCommand) {
        this.plugin = plugin;
        this.advancedCommand = advancedCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            executeCommand(sender, label, args);
        } catch (CommandException e) {
            messageSender().sendLocalizedError(sender, e.getMessage(), e.replacements());
            plugin.getLogger().log(Level.CONFIG, "Command exception occured.", e);
        }
        return true;
    }

    private void executeCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) throws CommandException {
        var arguments = Arguments.create(plugin, sender, args);
        advancedCommand.commandRoute(sender, label, arguments);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var arguments = Arguments.create(plugin, sender, args);
        List<String> strings;
        try {
            strings = advancedCommand.tabCompleteRoute(sender, label, arguments);
        } catch (CommandException e) {
            strings = Collections.singletonList(localizer().localize(e.getMessage(), e.replacements()));
            plugin.getLogger().log(Level.CONFIG, "Command exception occured.", e);
        }
        return strings;
    }

    /**
     * Get a instance of the localizer.
     *
     * @return localizer instance
     */
    protected final ILocalizer localizer() {
        if (localizer == null || localizer instanceof DummyLocalizer) {
            localizer = ILocalizer.getPluginLocalizer(plugin);
        }
        return localizer;
    }

    /**
     * Get a instance of the message sender.
     *
     * @return message sender instance
     */
    protected final MessageSender messageSender() {
        if (messageSender == null || messageSender.isDefault()) {
            messageSender = MessageSender.getPluginMessageSender(plugin);
        }
        return messageSender;
    }

}
