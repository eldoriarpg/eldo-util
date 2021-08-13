package de.eldoria.eldoutilities.plugin;

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
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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
public class EldoPlugin extends JavaPlugin implements DebugDataProvider {
    private static EldoPlugin instance;
    private PluginManager pluginManager;
    private BukkitScheduler scheduler;
    private DebugLogger debugLogger;
    private FailsaveCommand failcmd;
    private boolean started;
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
    public Logger getLogger() {
        if (debugLogger == null) {
            debugLogger = new DebugLogger(this, super.getLogger());
            setLoggerLevel();
        }
        return debugLogger;
    }

    protected void setLoggerLevel() {
        getLogger().setLevel(EldoConfig.getLogLevel());
    }

    /**
     * Register a tabexecutor for a command.
     * <p>
     * This tabexecutor will handle execution and tab completion.
     *
     * @param command     name of command
     * @param tabExecutor command executor
     */
    public void registerCommand(String command, TabExecutor tabExecutor) {
        PluginCommand cmd = getCommand(command);
        if (cmd != null) {
            cmd.setExecutor(tabExecutor);
            return;
        }
        getLogger().warning("Command " + command + " not found!");
    }

    /**
     * Registers listener for the plugin
     *
     * @param listener listener to register
     */
    public void registerListener(Listener... listener) {
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
    public int scheduleRepeatingTask(Runnable task, int period) {
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
    public int scheduleRepeatingTask(Runnable task, int delay, int period) {
        return getScheduler().scheduleSyncRepeatingTask(this, task, delay, period);
    }

    /**
     * Get the servers plugin manager
     *
     * @return plugin manager
     */
    public PluginManager getPluginManager() {
        if (pluginManager == null) {
            pluginManager = getServer().getPluginManager();
        }
        return pluginManager;
    }

    /**
     * Get the servers scheduler
     *
     * @return scheduler instance
     */
    public BukkitScheduler getScheduler() {
        if (scheduler == null) {
            scheduler = getServer().getScheduler();
        }
        return scheduler;
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
        if (started) {
            try {
                logger().config("Detected plugin reload.");
                onPluginReload();
            } catch (Throwable e) {
                initFailsave("Plugin failed to reload.", e);
            }
            return;
        }
        registerListener(new ReloadListener());
        started = true;
        EldoUtilities.ignite(instance);
        try {
            logger().config("Detected initial plugin enable.");
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
        }
        logger().config("Scheduling post startup");
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    onPostStart();
                } catch (Throwable e) {
                    initFailsave("Plugin post start failed.", e);
                }
            }
        }.runTaskLater(this, 1);
    }

    private void initFailsave(String message, Throwable e) {
        logger().log(Level.SEVERE, message, e);
        logger().log(Level.SEVERE, "Initializing failsave mode.");
        FailsaveCommand failcmd = new FailsaveCommand(instance, getDescription().getFullName().toLowerCase());
        for (String cmd : getDescription().getCommands().keySet()) {
            try {
                PluginCommand command = getCommand(cmd);
                if (command != null) {
                    registerCommand(cmd, failcmd);
                }
            } catch (Throwable ex) {
                logger().log(Level.WARNING, "Failed to initialize failsafe command", ex);
            }
        }
    }

    private void onReload() {
        try {
            logger().config("");
            logger().severe("Detected server reload.");
            logger().severe("Reloading the server is highly discouraged and can lead to unexpected behaviour.");
            logger().severe("Please do not report any bugs caused by reloading the server.");
            logger().config("");
            onPluginReload();
        } catch (Throwable e) {
            initFailsave("Plugin failed to reload.", e);
        }
    }

    public void onPluginReload() throws Throwable {
    }

    /**
     * Called when the server has started completely.
     */
    public void onPostStart() throws Throwable {
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
        }
        EldoUtilities.shutdown();
        try {
            onPluginDisable();
        } catch (Throwable e) {
            logger().log(Level.SEVERE, "Plugin failed to shutdown correctly.", e);
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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

        @EventHandler
        public void onRconCommand(RemoteServerCommandEvent event) {
        }

        public boolean isReload() {
            return reload;
        }

        public void reset() {
            reload = false;
        }
    }
}
