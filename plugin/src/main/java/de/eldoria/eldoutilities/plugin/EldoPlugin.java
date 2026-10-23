/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.plugin;

import de.eldoria.EldoUtilities;
import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.AdvancedCommandAdapter;
import de.eldoria.eldoutilities.commands.defaultcommands.FailsaveCommand;
import de.eldoria.eldoutilities.debug.DebugDataProvider;
import de.eldoria.eldoutilities.debug.data.EntryData;
import de.eldoria.eldoutilities.logging.DebugLogger;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.serialization.util.PluginSerializationName;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic Plugin implementation of a {@link JavaPlugin}.
 * <p>
 * Provides basic function to wrap some stuff and make it easier to access
 *
 * @since 1.1.0
 */
@SuppressWarnings({"RedundantThrows", "unused"})
public abstract class EldoPlugin extends JavaPlugin implements DebugDataProvider {
    private static EldoPlugin instance;
    private DebugLogger debugLogger;
    private AdvancedCommandAdapter failcmd;
    private ReloadListener reloadListener;

    public EldoPlugin() {
        registerSelf(this);
    }

    public EldoPlugin(
            @NotNull JavaPluginLoader loader,
            @NotNull PluginDescriptionFile description,
            @NotNull File dataFolder,
            @NotNull File file) {
        super(loader, description, dataFolder, file);
        registerSelf(this);
    }

    private static void registerSelf(EldoPlugin eldoPlugin) {
        if (instance == null) {
            instance = eldoPlugin;
        }

        for (var clazz : eldoPlugin.getConfigSerialization()) {
            if (clazz.isAnnotationPresent(PluginSerializationName.class)) {
                var annotation = clazz.getAnnotation(PluginSerializationName.class);
                ConfigurationSerialization.registerClass(
                        clazz,
                        annotation
                                .value()
                                .replace("{plugin}", eldoPlugin.getName().toLowerCase(Locale.ROOT)));
            }
            ConfigurationSerialization.registerClass(clazz);
        }

        EldoUtilities.preWarm(eldoPlugin);
        eldoPlugin.failcmd = AdvancedCommandAdapter.wrap(
                eldoPlugin, new FailsaveCommand(eldoPlugin, eldoPlugin.failsafePermission()));
    }

    public static EldoPlugin getInstance() {
        return instance;
    }

    public static Logger logger() {
        return getInstance().getLogger();
    }

    @Override
    @NotNull
    public Logger getLogger() {
        if (debugLogger == null) {
            debugLogger = new DebugLogger(this, super.getLogger());
            setLoggerLevel();
        }
        return debugLogger;
    }

    protected final void setLoggerLevel() {
        getLogger().setLevel(getLogLevel());
    }

    public abstract Level getLogLevel();

    /**
     * Register a tabexecutor for a command.
     * <p>
     * This tabexecutor will handle execution and tab completion. A command declared in the {@code plugin.yml} gets the
     * executor. Otherwise, as for Paper plugins whose {@code paper-plugin.yml} declares no commands, the command is
     * registered through Paper, which is only possible while the plugin enables.
     *
     * @param command     name of command
     * @param tabExecutor command executor
     */
    public final void registerCommand(String command, TabExecutor tabExecutor) {
        registerCommand(command, List.of(), tabExecutor);
    }

    /**
     * Gives a command of the {@code plugin.yml} its executor, and registers any other command through Paper. Paper
     * plugins declare no commands and may not call {@link #getCommand(String)} while they start, so the descriptor
     * decides which way a command goes.
     */
    private void registerCommand(String command, Collection<String> aliases, TabExecutor tabExecutor) {
        if (!getDescription().getCommands().containsKey(command)) {
            registerCommand(command, aliases, new PaperCommandBridge(command, tabExecutor));
            return;
        }
        var cmd = getCommand(command);
        if (cmd != null) {
            cmd.setExecutor(tabExecutor);
            return;
        }
        getLogger().warning("Command " + command + " not found!");
    }

    /**
     * Register a advanced command.
     * <p>
     * This will register a command exector for the toplevel command.
     *
     * @param command command
     */
    public final void registerCommand(AdvancedCommand command) {
        registerCommand(command.meta().name(), command);
    }

    /**
     * Register a tabexecutor for a command.
     * <p>
     * This tabexecutor will handle execution and tab completion. Without a {@code plugin.yml} entry the command is
     * registered through Paper, with the aliases of the command's {@link AdvancedCommand#meta() meta}.
     *
     * @param command  name of command
     * @param executor command executor
     */
    public final void registerCommand(String command, AdvancedCommand executor) {
        registerCommand(command, List.of(executor.meta().aliases()), AdvancedCommandAdapter.wrap(this, executor));
    }

    /**
     * Registers listener for the plugin
     *
     * @param listener listener to register
     */
    public final void registerListener(Listener... listener) {
        for (var l : listener) {
            getPluginManager().registerEvents(l, this);
        }
    }

    /**
     * Schedules a repeating task.
     * <p>
     * This task will be executed by the main server thread. Not available on Folia, which has no main thread.
     *
     * @param task   Task to be executed
     * @param period Period in server ticks of the task
     * @return Task id number (-1 if scheduling failed)
     */
    public final int scheduleRepeatingTask(Runnable task, int period) {
        return scheduleRepeatingTask(task, 100, period);
    }

    /**
     * Schedules a repeating task.
     * <p>
     * This task will be executed by the main server thread. Not available on Folia, which has no main thread.
     *
     * @param task   Task to be executed
     * @param delay  Delay in server ticks before executing first repeat
     * @param period Period in server ticks of the task
     * @return Task id number (-1 if scheduling failed)
     */
    public final int scheduleRepeatingTask(Runnable task, int delay, int period) {
        return getScheduler().scheduleSyncRepeatingTask(this, task, delay, period);
    }

    /**
     * Get the servers plugin manager.
     *
     * @return plugin manager
     */
    public final PluginManager getPluginManager() {
        return getServer().getPluginManager();
    }

    /**
     * Get the servers scheduler.
     * <p>
     * Not available on Folia. Use the region, entity or async schedulers of the server instead, which work on Paper as
     * well.
     *
     * @return scheduler instance
     */
    public final BukkitScheduler getScheduler() {
        return getServer().getScheduler();
    }

    /**
     * Get a list of classes which should be registered via {@link  ConfigurationSerialization#registerClass(Class)}.
     * <p>
     * These classes will be registered on load plugin initialization.
     *
     * @return list of serializable classes.
     */
    public List<Class<? extends ConfigurationSerializable>> getConfigSerialization() {
        return Collections.emptyList();
    }

    @Override
    public final void onLoad() {
        getLogger().config("Loading plugin.");
        try {
            onPluginLoad();
        } catch (Throwable e) {
            initFailsave("Plugin failed to load.", e);
        }
    }

    /**
     * Executed on load of the plugin. Replacement for {@link Plugin#onLoad()}.
     * <p>
     * Any thrown exception will be catched and make the plugin initializing the failsave mode.
     *
     * @throws Throwable any throwable
     */
    public void onPluginLoad() throws Throwable {}

    @Override
    public final void onEnable() {
        var start = Instant.now();
        var reload = isLocked();
        if (reload) {
            try {
                getLogger().config("Detected plugin reload.");
                onReload();
            } catch (Throwable e) {
                initFailsave("Plugin failed to reload.", e);
                return;
            }
        }
        reloadListener = new ReloadListener();
        registerListener(reloadListener);
        EldoUtilities.ignite(instance);
        try {
            if (!reload) {
                getLogger().config("Detected initial plugin enable.");
            }
            onPluginEnable(reload);
            onPluginEnable();
        } catch (Throwable e) {
            initFailsave("Plugin failed to enable.", e);
            for (var cmd : failsafeCommands()) {
                try {
                    registerCommand(cmd, (TabExecutor) failcmd);
                } catch (Throwable ex) {
                    getLogger().log(Level.WARNING, "Failed to initialize failsafe command", ex);
                }
            }
            return;
        }
        getLogger().config("Scheduling post startup");
        getServer().getGlobalRegionScheduler().runDelayed(this, task -> postStart(reload), 1);
        removeLock();
        var until = start.until(Instant.now(), ChronoUnit.MILLIS);
        getLogger().info("Enabled. Required " + until + " ms.");
    }

    /**
     * Runs the post start hooks one tick after enable. It is scheduled on the global region scheduler, which exists on
     * Paper and Folia alike, so plugins built on this class also load on Folia.
     *
     * @param reload whether the enable was caused by a server reload
     */
    private void postStart(boolean reload) {
        var start = Instant.now();
        try {
            onPostStart(reload);
            onPostStart();
        } catch (Throwable e) {
            initFailsave("Plugin post start failed.", e);
            return;
        }
        var until = start.until(Instant.now(), ChronoUnit.MILLIS);
        getLogger().info("Post startup done. Required " + until + " ms.");
    }

    private void initFailsave(String message, Throwable e) {
        getLogger().log(Level.SEVERE, message, e);
        getLogger().log(Level.SEVERE, "Initializing failsave mode.");
        var failcmd = new FailsaveCommand(this, failsafePermission());
        for (var cmd : failsafeCommands()) {
            try {
                registerCommand(cmd, failcmd);
            } catch (Throwable ex) {
                getLogger().log(Level.WARNING, "Failed to initialize failsafe command", ex);
            }
        }
    }

    /**
     * The permission that allows the debug command of the failsave mode, next to {@code eldoutilities.debug}.
     *
     * @return {@code <plugin>.debug} with the lower case plugin name
     */
    private String failsafePermission() {
        return getName().toLowerCase(Locale.ROOT) + ".debug";
    }

    /**
     * The commands that answer in failsave mode: those of the {@code plugin.yml}, or for a Paper plugin, which declares
     * none, the lower case plugin name.
     *
     * @return command names
     */
    private Collection<String> failsafeCommands() {
        var commands = getDescription().getCommands().keySet();
        return commands.isEmpty() ? List.of(getName().toLowerCase(Locale.ROOT)) : commands;
    }

    private void onReload() throws Throwable {
        getLogger().severe("⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱");
        getLogger().severe("Detected server reload.");
        getLogger().severe("Reloading the server is highly discouraged and can lead to unexpected behaviour.");
        getLogger().severe("Please do not report any bugs caused by reloading the server.");
        getLogger().severe("⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰");
        onPluginReload();
    }

    /**
     * Executed on when the plugin gets reloaded via server reload. This method will be executed before execution of {@link #onPluginEnable()}.
     * <p>
     * Any thrown exception will be catched and make the plugin initializing the failsave mode.
     *
     * @throws Throwable any throwable
     */
    public void onPluginReload() throws Throwable {}

    /**
     * Called when the server has started completely.
     * <p>
     * Any thrown exception will be catched and make the plugin initializing the failsave mode.
     *
     * @param reload indicated that the call was caused by a server reload
     * @throws Throwable any throwable
     */
    public void onPostStart(boolean reload) throws Throwable {}

    /**
     * Called when the server has started completely.
     * <p>
     * Any thrown exception will be catched and make the plugin initializing the failsave mode.
     *
     * @throws Throwable any throwable
     */
    public void onPostStart() throws Throwable {}

    /**
     * Called when this plugin is enabled. Replacement for {@link Plugin#onEnable()}
     * <p>
     * Any thrown exception will be catched and make the plugin initializing the failsave mode.
     *
     * @param reload indicated that the call was caused by a server reload
     * @throws Throwable any throwable
     */
    public void onPluginEnable(boolean reload) throws Throwable {}

    /**
     * Called when this plugin is enabled. Replacement for {@link Plugin#onEnable()}
     * <p>
     * Any thrown exception will be catched and make the plugin initializing the failsave mode.
     *
     * @throws Throwable any throwable
     */
    public void onPluginEnable() throws Throwable {}

    @Override
    public final void onDisable() {
        if (reloadListener.isReload()) {
            getLogger().severe("Plugin is disabled by server reload.");
            createLock();
        }
        EldoUtilities.shutdown();
        try {
            onPluginDisable();
        } catch (Throwable e) {
            getLogger().log(Level.SEVERE, "Plugin failed to shutdown correctly.", e);
        }
        MessageSender.unregister(this);
    }

    private Path getLockFile() {
        return getDataFolder().toPath().resolve("lock");
    }

    private void createLock() {
        try {
            Files.createFile(getLockFile());
        } catch (IOException e) {
            getLogger().config("Could not create lock file");
        }
    }

    private boolean isLocked() {
        return Files.exists(getLockFile());
    }

    private void removeLock() {
        try {
            Files.deleteIfExists(getLockFile());
        } catch (IOException e) {
            getLogger().config("Could not resolve lock");
        }
    }

    /**
     * Called when this plugin is disabled.
     *
     * @throws Throwable any exception
     */
    public void onPluginDisable() throws Throwable {}

    @Override
    public @NotNull EntryData[] getDebugInformations() {
        return new EntryData[0];
    }

    @Override
    public final boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return AdvancedCommandAdapter.wrap(this, failcmd).onCommand(sender, command, label, args);
    }

    private static class ReloadListener implements Listener {
        private boolean reload;

        @EventHandler
        public void onCommand(PlayerCommandPreprocessEvent event) {
            if (event.getPlayer().hasPermission("bukkit.command.reload")
                    || event.getPlayer().isOp()) {
                if (event.getMessage().startsWith("/reload")) {
                    reload = true;
                }
            }
        }

        @EventHandler
        public void onServercommand(ServerCommandEvent event) {
            if (event.getCommand().startsWith("reload")) {
                reload = true;
            }
        }

        public boolean isReload() {
            return reload;
        }
    }
}
