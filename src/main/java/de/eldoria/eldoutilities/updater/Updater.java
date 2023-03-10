/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import de.eldoria.eldoutilities.updater.butlerupdater.ButlerUpdateChecker;
import de.eldoria.eldoutilities.updater.butlerupdater.ButlerUpdateData;
import de.eldoria.eldoutilities.updater.lynaupdater.LynaUpdateChecker;
import de.eldoria.eldoutilities.updater.lynaupdater.LynaUpdateData;
import de.eldoria.eldoutilities.updater.notifier.DownloadedNotifier;
import de.eldoria.eldoutilities.updater.notifier.UpdateNotifier;
import de.eldoria.eldoutilities.updater.spigotupdater.SpigotUpdateChecker;
import de.eldoria.eldoutilities.updater.spigotupdater.SpigotUpdateData;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.net.http.HttpClient;
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
    private boolean notifyActive;
    private HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private boolean updateAvailable;
    private boolean downloaded;

    protected Updater(T data) {
        this.plugin = data.plugin();
        this.data = data;
    }

    /**
     * Create a new spigot update checker
     *
     * @param data spigot plugin data
     * @return Updater instance
     */
    public static Updater<?> spigot(SpigotUpdateData data) {
        return new SpigotUpdateChecker(data);
    }

    /**
     * Create a new update butler checker
     *
     * @param data butler plugin data
     * @return Updater instance
     */
    public static Updater<?> butler(ButlerUpdateData data) {
        return new ButlerUpdateChecker(data);
    }

    /**
     * Create a new lyna update check
     *
     * @param data lyna plugin data
     * @return Updater instance
     */
    public static Updater<?> lyna(LynaUpdateData data) {
        return new LynaUpdateChecker(data);
    }

    @Override
    public void run() {
        performCheck(true);
    }

    /**
     * Performs an update check with the saved data. This can be repeatet in a scheduler.
     *
     * @return true when the check was successful and a new version is available
     */
    public final boolean performCheck(boolean silent) {
        // dont check if update is already available
        if (updateAvailable) return true;

        if (!silent) {
            plugin.getLogger().info("§2Checking for new Version...");
        }

        var optLatest = checkUpdate(data);
        if (optLatest.isPresent()) {
            latestVersion = optLatest.get().latestVersion();
            updateAvailable = optLatest.get().isOutdated();
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
                registerListener(new DownloadedNotifier(plugin, data, latestVersion, downloaded));
            } else {
                registerListener(new UpdateNotifier(plugin, data, latestVersion));
            }
        } else {
            if (!silent) {
                plugin.getLogger().info("§2Plugin is up to date.");
            }
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
    protected abstract Optional<UpdateResponse> checkUpdate(T data);

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
        data.updateMessage(latestVersion).lines().forEach(line -> {
            plugin.getLogger().info(line);
        });
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

    protected HttpClient client() {
        return client;
    }
}
