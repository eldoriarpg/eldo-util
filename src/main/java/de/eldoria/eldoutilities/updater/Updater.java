package de.eldoria.eldoutilities.updater;

import de.eldoria.eldoutilities.updater.butlerupdater.ButlerUpdateChecker;
import de.eldoria.eldoutilities.updater.butlerupdater.ButlerUpdateData;
import de.eldoria.eldoutilities.updater.notifier.DownloadedNotifier;
import de.eldoria.eldoutilities.updater.notifier.UpdateNotifier;
import de.eldoria.eldoutilities.updater.spigotupdater.SpigotUpdateChecker;
import de.eldoria.eldoutilities.updater.spigotupdater.SpigotUpdateData;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;

/**
 * Base implementation for Updater.
 *
 * @param <T> type of Updater
 */
public abstract class Updater<T extends UpdateData> extends BukkitRunnable implements Listener {
    private final Plugin plugin;
    private final T data;
    private String latestVersion;
    private boolean notifyActive = false;
    private boolean updateAvailable = false;
    private boolean downloaded = false;

    public Updater(T data) {
        this.plugin = data.getPlugin();
        this.data = data;
    }

    public static Updater<?> Spigot(SpigotUpdateData data) {
        return new SpigotUpdateChecker(data);
    }

    public static Updater<?> Butler(ButlerUpdateData data) {
        return new ButlerUpdateChecker(data);
    }

    @Override
    public void run() {
        performCheck();
    }

    /**
     * Performs a update check with the saved data. This can be repeatet in a scheduler.
     *
     * @return true when the check was successful and a new version is available
     */
    public final boolean performCheck() {
        plugin.getLogger().info("§2Checking for new Version...");
        // dont check if update is already available
        if (updateAvailable) return true;

        Optional<String> optLatest = getLatestVersion(data);
        if (optLatest.isPresent()) {
            latestVersion = optLatest.get();
            updateAvailable = evaluate(this.latestVersion);
        } else {
            plugin.getLogger().info("Could not check latest version.");
            return false;
        }

        if (updateAvailable) {
            logUpdateMessage();
            if (data.isAutoUpdate()) {
                if (!downloaded) {
                    downloaded = update();
                }
                registerListener(new DownloadedNotifier(plugin, data.getNotifyPermission(), latestVersion, downloaded));
            } else {
                registerListener(new UpdateNotifier(plugin, data.getNotifyPermission(), latestVersion));
            }
        } else {
            plugin.getLogger().info("§2Plugin is up to date.");
        }
        return updateAvailable;
    }

    /**
     * The check method will be called after the constructor is called.
     * <p>
     * This method should be implemented as follows:
     * <p>
     * Retrieve the latest version of the plugin from any update service.
     * <p>
     * return the latest version or a empty optional if the version could not be checked.
     *
     * @param data data for plugin updates
     * @return empty optional if the version could not be checked or the latest version.
     */
    protected abstract Optional<String> getLatestVersion(T data);

    /**
     * Evaluates the result from request.
     *
     * @param latestVersion optional with latest version
     * @return true if a update is available.
     */
    private boolean evaluate(String latestVersion) {
        return !plugin.getDescription().getVersion().equalsIgnoreCase(latestVersion);
    }

    /**
     * This version should update the plugin. If not implemented set the {@link UpdateData#isAutoUpdate()} to false.
     *
     * @return true if the update was succesful.
     */
    protected boolean update() {

        return false;
    }

    /**
     * Start the update scheduler.
     * <p>
     * This will check every 6 hours if a update is available.
     *
     * @return Bukkit task that contains the id number
     */
    public BukkitTask start() {
        return runTaskTimerAsynchronously(plugin, 40, 432000);
    }

    private void logUpdateMessage() {
        plugin.getLogger().info("§2New version of §6" + plugin.getName() + "§2 available.");
        plugin.getLogger().info("§2Newest version: §3" + latestVersion + "! Current version: §c" + plugin.getDescription().getVersion() + "§2!");
        if (data.isAutoUpdate()) {
            plugin.getLogger().info("§2Download new version here: §6" + plugin.getDescription().getWebsite());
        }
    }

    private void registerListener(Listener listener) {
        if (data.isNotifyUpdate() && !notifyActive) {
            notifyActive = true;
            plugin.getServer().getPluginManager()
                    .registerEvents(listener, plugin);
        }
    }

    public T getData() {
        return data;
    }
}
