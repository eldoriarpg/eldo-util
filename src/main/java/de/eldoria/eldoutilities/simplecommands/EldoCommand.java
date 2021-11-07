package de.eldoria.eldoutilities.simplecommands;

import de.eldoria.eldoutilities.localization.DummyLocalizer;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.messages.MessageChannel;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.messages.MessageType;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class for command creation.
 * <p>
 * Features automatic tab completion and sub commands.
 *
 * @since 1.1.0
 */
public abstract class EldoCommand implements TabExecutor {
    private final Map<String, TabExecutor> subCommands = new HashMap<>();
    private final Plugin plugin;
    private ILocalizer localizer;
    private MessageSender messageSender;
    private String[] registeredCommands = new String[0];
    private TabExecutor defaultCommand;

    public EldoCommand(Plugin plugin) {
        this.plugin = plugin;
    }


    /**
     * Checks if the provided arguments are invalid.
     *
     * @param sender        user which executed the command.
     * @param messageSender message sender for calling home.
     * @param localizer     localizer for localization stuff.
     * @param args          arguments to check
     * @param length        min amount of arguments.
     * @param syntax        correct syntax
     * @return true if the arguments are invalid
     */
    protected static boolean argumentsInvalid(CommandSender sender, MessageSender messageSender, ILocalizer localizer, String[] args, int length, String syntax) {
        if (args.length < length) {
            messageSender.sendError(sender, localizer.getMessage("error.invalidArguments",
                    Replacement.create("SYNTAX", localizer.localize(syntax)).addFormatting('6')));
            return true;
        }
        return false;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (defaultCommand != null) {
                return defaultCommand.onCommand(sender, command, label, args);
            }
            return true;
        }

        final var newArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

        return getCommand(args[0]).map(c -> c.onCommand(sender, command, args[0], newArgs)).orElse(false);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return ArrayUtil.startingWithInArray(args[0], registeredCommands).collect(Collectors.toList());
        }
        final var newArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

        if (args.length == 0) return Collections.emptyList();

        return getCommand(args[0]).map(c -> c.onTabComplete(sender, command, args[0], newArgs))
                .orElse(Collections.singletonList(localizer().getMessage("error.invalidCommand")));
    }

    private Optional<TabExecutor> getCommand(String command) {
        for (var entry : subCommands.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(command)) {
                return Optional.ofNullable(entry.getValue());
            }
        }
        return Optional.empty();
    }

    /**
     * Registers a command as sub command.
     *
     * @param command  name of the command.
     * @param executor executor
     */
    public final void registerCommand(String command, TabExecutor executor) {
        subCommands.put(command, executor);
        registeredCommands = new String[subCommands.size()];
        subCommands.keySet().toArray(registeredCommands);
    }

    /**
     * Sets the default command of not arguments are present to determine a subcommand.
     *
     * @param defaultCommand executor for default command
     */
    public final void setDefaultCommand(TabExecutor defaultCommand) {
        this.defaultCommand = defaultCommand;
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

    /**
     * Checks if the provided arguments are invalid.
     *
     * @param sender user which executed the command.
     * @param args   arguments to check
     * @param length min amount of arguments.
     * @param syntax correct syntax
     * @return true if the arguments are invalid
     */
    protected boolean argumentsInvalid(CommandSender sender, String[] args, int length, String syntax) {
        return argumentsInvalid(sender, messageSender(), localizer(), args, length, syntax);
    }

    /**
     * Returns true if the sender is a player and sends an error message
     * <p>
     * Uses the {@code error.notAsPlayer} error code
     *
     * @param sender sender to check
     * @return true if sender is player
     */
    protected boolean denyPlayer(CommandSender sender) {
        if (isPlayer(sender)) {
            messageSender().sendLocalized(MessageChannel.CHAT, MessageType.ERROR, sender, "error.notAsPlayer");
            return true;
        }
        return false;
    }

    /**
     * Returns true if the sender is a console and sends an error message.
     * <p>
     * Uses the {@code error.notAsConsole} error code
     *
     * @param sender sender to check
     * @return true if sender is console
     */
    protected boolean denyConsole(CommandSender sender) {
        if (isConsole(sender)) {
            messageSender().sendLocalized(MessageChannel.CHAT, MessageType.ERROR, sender, "error.notAsConsole");
            return true;
        }
        return false;
    }

    /**
     * Checks if the user has at least one of the provided permissions. This will send a message to the player which
     * uses the {@code error.permission} locale key which should provide a placeholder {@code PERMISSION} for an array
     * of required permissions, if the user lacks all of the permissions.
     *
     * @param actor       actor which wants to execute this action
     * @param permissions one or more permissions to check
     * @return true if the user has no of the required permission
     */
    protected boolean denyAccess(CommandSender actor, String... permissions) {
        return denyAccess(actor, false, permissions);
    }

    /**
     * Checks if the user has at least one of the provided permissions. This will send a message to the player which
     * uses the {@code error.permission} locale key which should provide a placeholder {@code PERMISSION} for an array
     * of required permissions, if the user lacks all of the permissions.
     *
     * @param actor       actor which wants to execute this action
     * @param silent      set to true if no message should be send to the player
     * @param permissions one or more permissions to check
     * @return true if the user has none of the required permission
     */
    protected boolean denyAccess(CommandSender actor, boolean silent, String... permissions) {
        if (actor == null) {
            return false;
        }

        Player player = null;

        if (actor instanceof Player) {
            player = (Player) actor;
        }

        if (player == null) {
            return false;
        }
        for (var permission : permissions) {
            if (player.hasPermission(permission)) {
                return false;
            }
        }
        if (!silent) {
            messageSender().sendMessage(player,
                    localizer().getMessage("error.permission",
                            Replacement.create("PERMISSION", String.join(", ", permissions)).addFormatting('6')));
        }
        return true;
    }

    /**
     * Get the player from a sender if {@link #isPlayer(CommandSender)} returns true.
     *
     * @param sender sender to cast
     * @return player or null if sender is not player
     */
    protected Player getPlayerFromSender(CommandSender sender) {
        return isPlayer(sender) ? (Player) sender : null;
    }

    /**
     * Checks if a command sender is the console.
     *
     * @param sender sender to check
     * @return true if the sender is the console
     */
    protected boolean isConsole(CommandSender sender) {
        return !(sender instanceof Player);
    }

    /**
     * Checks if a command sender is a player
     *
     * @param sender sender to check
     * @return true if the sender is a player
     */
    protected boolean isPlayer(CommandSender sender) {
        return (sender instanceof Player);
    }

    /**
     * Checks if a value value is in a invalid Range. Will send a message based on {@code error.invalidRange} locale key
     * which should provide a placeholder for {@code MIN} and {@code MAX} which will be replaced with the corresponding
     * values.
     *
     * @param sender sender of command
     * @param value  current value
     * @param min    min value
     * @param max    max value
     * @return true if the range is invalid
     */
    protected boolean invalidRange(CommandSender sender, double value, double min, double max) {
        if (value > max || value < min) {
            messageSender().sendError(sender, localizer().getMessage("error.invalidRange",
                    Replacement.create("MIN", min).addFormatting('6'),
                    Replacement.create("MAX", max).addFormatting('6')));
            return true;
        }
        return false;
    }

    /**
     * Checks if a enum value is invalid. Will send a message based on {@code error.invalidEnumValue} locale key which
     * should provide a placeholder for {@code VALUE} which will be replaced with an array of possible inputs
     *
     * @param sender sender of command
     * @param value  value of enum
     * @param clazz  clazz of enum
     * @param <T>    type of enum
     * @return true if the enum is invalid
     */
    protected <T extends Enum<T>> boolean invalidEnumValue(CommandSender sender, T value, Class<T> clazz) {
        if (value == null) {
            messageSender().sendError(sender, localizer().getMessage("error.invalidEnumValue",
                    Replacement.create("VALUES",
                            Arrays.stream(clazz.getEnumConstants())
                                    .map(e -> e.name().toLowerCase())
                                    .collect(Collectors.joining(" ")))
                            .addFormatting('6')));
            return true;
        }
        return false;
    }

    protected boolean invalidRange(CommandSender sender, int value, int min, int max) {
        return invalidRange(sender, (double) value, min, max);
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
