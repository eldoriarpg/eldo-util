/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.plugin;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.AdvancedCommandAdapter;
import de.eldoria.eldoutilities.commands.defaultcommands.FailsaveCommand;
import de.eldoria.eldoutilities.configuration.EldoConfig;
import de.eldoria.eldoutilities.core.EldoUtilities;
import de.eldoria.eldoutilities.debug.DebugDataProvider;
import de.eldoria.eldoutilities.debug.data.EntryData;
import de.eldoria.eldoutilities.logging.DebugLogger;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
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

    public EldoPlugin(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description, @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, dataFolder, file);
        registerSelf(this);
    }

    private static void registerSelf(EldoPlugin eldoPlugin) {
        if (instance == null) {
            instance = eldoPlugin;
        }
        for (var clazz : eldoPlugin.getConfigSerialization()) {
            ConfigurationSerialization.registerClass(clazz);
        }
        EldoUtilities.preWarm(eldoPlugin);
        eldoPlugin.failcmd = AdvancedCommandAdapter.wrap(eldoPlugin, new FailsaveCommand(eldoPlugin, eldoPlugin.getDescription().getFullName().toLowerCase()));
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
        getLogger().setLevel(EldoConfig.getLogLevel(this));
    }

    /**
     * Register a tabexecutor for a command.
     * <p>
     * This tabexecutor will handle execution and tab completion.
     *
     * @param command     name of command
     * @param tabExecutor command executor
     */
    public final void registerCommand(String command, TabExecutor tabExecutor) {
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
     * This tabexecutor will handle execution and tab completion.
     *
     * @param command  name of command
     * @param executor command executor
     */
    public final void registerCommand(String command, AdvancedCommand executor) {
        registerCommand(command, (TabExecutor) AdvancedCommandAdapter.wrap(this, executor));
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
     * This task will be executed by the main server thread.
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
     * This task will be executed by the main server thread.
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
    public void onPluginLoad() throws Throwable {

    }

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
            for (var cmd : getDescription().getCommands().keySet()) {
                try {
                    registerCommand(cmd, (TabExecutor) failcmd);
                } catch (Throwable ex) {
                    getLogger().log(Level.WARNING, "Failed to initialize failsafe command", ex);
                }
            }
            return;
        }
        getLogger().config("Scheduling post startup");
        new BukkitRunnable() {
            @Override
            public void run() {
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
        }.runTaskLater(this, 1);
        removeLock();
        var until = start.until(Instant.now(), ChronoUnit.MILLIS);
        getLogger().info("Enabled. Required " + until + " ms.");
    }

    private void initFailsave(String message, Throwable e) {
        getLogger().log(Level.SEVERE, message, e);
        getLogger().log(Level.SEVERE, "Initializing failsave mode.");
        var failcmd = new FailsaveCommand(instance, getDescription().getFullName().toLowerCase());
        for (var cmd : getDescription().getCommands().keySet()) {
            try {
                registerCommand(cmd, failcmd);
            } catch (Throwable ex) {
                getLogger().log(Level.WARNING, "Failed to initialize failsafe command", ex);
            }
        }
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
    public void onPluginReload() throws Throwable {
    }

    /**
     * Called when the server has started completely.
     * <p>
     * Any thrown exception will be catched and make the plugin initializing the failsave mode.
     *
     * @param reload indicated that the call was caused by a server reload
     * @throws Throwable any throwable
     */
    public void onPostStart(boolean reload) throws Throwable {
    }

    /**
     * Called when the server has started completely.
     * <p>
     * Any thrown exception will be catched and make the plugin initializing the failsave mode.
     *
     * @throws Throwable any throwable
     */
    public void onPostStart() throws Throwable {
    }

    /**
     * Called when this plugin is enabled. Replacement for {@link Plugin#onEnable()}
     * <p>
     * Any thrown exception will be catched and make the plugin initializing the failsave mode.
     *
     * @param reload indicated that the call was caused by a server reload
     * @throws Throwable any throwable
     */
    public void onPluginEnable(boolean reload) throws Throwable {
    }

    /**
     * Called when this plugin is enabled. Replacement for {@link Plugin#onEnable()}
     * <p>
     * Any thrown exception will be catched and make the plugin initializing the failsave mode.
     *
     * @throws Throwable any throwable
     */
    public void onPluginEnable() throws Throwable {
    }

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
    public void onPluginDisable() throws Throwable {
    }

    @Override
    public @NotNull EntryData[] getDebugInformations() {
        return new EntryData[0];
    }

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return AdvancedCommandAdapter.wrap(this, failcmd).onCommand(sender, command, label, args);
    }

    private static class ReloadListener implements Listener {
        private boolean reload;

        @EventHandler
        public void onCommand(PlayerCommandPreprocessEvent event) {
            if (event.getPlayer().hasPermission("bukkit.command.reload") || event.getPlayer().isOp()) {
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
