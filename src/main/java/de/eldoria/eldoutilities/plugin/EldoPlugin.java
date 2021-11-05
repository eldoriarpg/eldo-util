package de.eldoria.eldoutilities.plugin;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.AdvancedCommandAdapter;
import de.eldoria.eldoutilities.configuration.EldoConfig;
import de.eldoria.eldoutilities.core.EldoUtilities;
import de.eldoria.eldoutilities.debug.DebugDataProvider;
import de.eldoria.eldoutilities.debug.data.EntryData;
import de.eldoria.eldoutilities.logging.DebugLogger;
import de.eldoria.eldoutilities.simplecommands.commands.FailsaveCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
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
public abstract class EldoPlugin extends JavaPlugin implements DebugDataProvider {
    private static EldoPlugin instance;
    private DebugLogger debugLogger;
    private FailsaveCommand failcmd;
    private ReloadListener reloadListener;

    public EldoPlugin() {
        registerSelf(this);
    }

    public EldoPlugin(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description, @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, dataFolder, file);
        registerSelf(this);
    }

    private static void registerSelf(EldoPlugin eldoPlugin) {
        instance = eldoPlugin;
        for (Class<? extends ConfigurationSerializable> clazz : eldoPlugin.getConfigSerialization()) {
            ConfigurationSerialization.registerClass(clazz);
        }
        EldoUtilities.preWarm(instance);
        instance.failcmd = new FailsaveCommand(instance, instance.getDescription().getFullName().toLowerCase());
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
        PluginCommand cmd = getCommand(command);
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
        registerCommand(command, AdvancedCommandAdapter.wrap(this, executor));
    }

    /**
     * Registers listener for the plugin
     *
     * @param listener listener to register
     */
    public final void registerListener(Listener... listener) {
        for (Listener l : listener) {
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
     * Get the servers plugin manager
     *
     * @return plugin manager
     */
    public final PluginManager getPluginManager() {
        return getServer().getPluginManager();
    }

    /**
     * Get the servers scheduler
     *
     * @return scheduler instance
     */
    public final BukkitScheduler getScheduler() {
        return getServer().getScheduler();
    }

    public List<Class<? extends ConfigurationSerializable>> getConfigSerialization() {
        return Collections.emptyList();
    }

    @Override
    public final void onLoad() {
        logger().config("Loading plugin.");
        try {
            onPluginLoad();
        } catch (Throwable e) {
            initFailsave("Plugin failed to load.", e);
        }
    }

    public void onPluginLoad() throws Throwable {

    }

    @Override
    public final void onEnable() {
        Instant start = Instant.now();
        boolean reload = isLocked();
        if (reload) {
            try {
                logger().config("Detected plugin reload.");
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
                logger().config("Detected initial plugin enable.");
            }
            onPluginEnable(reload);
            onPluginEnable();
        } catch (Throwable e) {
            initFailsave("Plugin failed to enable.", e);
            for (String cmd : getDescription().getCommands().keySet()) {
                try {
                    registerCommand(cmd, failcmd);
                } catch (Throwable ex) {
                    logger().log(Level.WARNING, "Failed to initialize failsafe command", ex);
                }
            }
            return;
        }
        logger().config("Scheduling post startup");
        new BukkitRunnable() {
            @Override
            public void run() {
                Instant start = Instant.now();
                try {
                    onPostStart(reload);
                    onPostStart();
                } catch (Throwable e) {
                    initFailsave("Plugin post start failed.", e);
                    return;
                }
                long until = start.until(Instant.now(), ChronoUnit.MILLIS);
                logger().info("Post startup done. Required " + until + " ms.");

            }
        }.runTaskLater(this, 1);
        removeLock();
        long until = start.until(Instant.now(), ChronoUnit.MILLIS);
        logger().info("Enabled. Required " + until + " ms.");
    }

    private void initFailsave(String message, Throwable e) {
        logger().log(Level.SEVERE, message, e);
        logger().log(Level.SEVERE, "Initializing failsave mode.");
        FailsaveCommand failcmd = new FailsaveCommand(instance, getDescription().getFullName().toLowerCase());
        for (String cmd : getDescription().getCommands().keySet()) {
            try {
                registerCommand(cmd, failcmd);
            } catch (Throwable ex) {
                logger().log(Level.WARNING, "Failed to initialize failsafe command", ex);
            }
        }
    }

    private void onReload() throws Throwable {
        logger().severe("⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱ ⟱");
        logger().severe("Detected server reload.");
        logger().severe("Reloading the server is highly discouraged and can lead to unexpected behaviour.");
        logger().severe("Please do not report any bugs caused by reloading the server.");
        logger().severe("⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰ ⟰");
        onPluginReload();
    }

    public void onPluginReload() throws Throwable {
    }

    /**
     * Called when the server has started completely.
     *
     * @param reload indicated that the call was caused by a server reload
     */
    public void onPostStart(boolean reload) throws Throwable {
    }

    /**
     * Called when the server has started completely.
     */
    public void onPostStart() throws Throwable {
    }

    /**
     * Called when this plugin is enabled
     *
     * @param reload indicated that the call was caused by a server reload
     */
    public void onPluginEnable(boolean reload) throws Throwable {
    }

    /**
     * Called when this plugin is enabled
     */
    public void onPluginEnable() throws Throwable {
    }

    @Override
    public final void onDisable() {
        if (reloadListener.isReload()) {
            logger().severe("Plugin is disabled by server reload.");
            createLock();
        }
        EldoUtilities.shutdown();
        try {
            onPluginDisable();
        } catch (Throwable e) {
            logger().log(Level.SEVERE, "Plugin failed to shutdown correctly.", e);
        }
    }

    private Path getLockFile() {
        return getDataFolder().toPath().resolve("lock");
    }

    private void createLock() {
        try {
            Files.createFile(getLockFile());
        } catch (IOException e) {
            logger().config("Could not create lock file");
        }
    }

    private boolean isLocked() {
        return Files.exists(getLockFile());
    }

    private void removeLock() {
        try {
            Files.deleteIfExists(getLockFile());
        } catch (IOException e) {
            logger().config("Could not resolve lock");
        }
    }

    /**
     * Called when this plugin is disabled.
     */
    public void onPluginDisable() throws Throwable {
    }

    @Override
    public @NotNull EntryData[] getDebugInformations() {
        return new EntryData[0];
    }

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return failcmd.onCommand(sender, command, label, args);
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
