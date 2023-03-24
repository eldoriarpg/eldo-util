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
import de.eldoria.eldoutilities.updater.lynaupdater.LynaUpdateResponse;
import de.eldoria.eldoutilities.updater.notifier.DownloadedNotifier;
import de.eldoria.eldoutilities.updater.notifier.UpdateNotifier;
import de.eldoria.eldoutilities.updater.spigotupdater.SpigotUpdateChecker;
import de.eldoria.eldoutilities.updater.spigotupdater.SpigotUpdateData;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.net.http.HttpClient;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Base implementation for Updater.
 *
 * @param <T> type of Updater
 */
public abstract class Updater<V extends UpdateResponse, T extends UpdateData<V>> implements Listener, Runnable {
    private final Plugin plugin;
    private final T data;
    private final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        var thread = new Thread(r);
        thread.setName("EldoUtilititesUpdateChecker");
        thread.setDaemon(true);
        return thread;
    });
    private boolean notifyActive;
    private boolean updateAvailable;
    private boolean downloaded;
    private V lastCheck;

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
    public static Updater<DefaultUpdateResponse, SpigotUpdateData> spigot(SpigotUpdateData data) {
        return new SpigotUpdateChecker(data);
    }

    /**
     * Create a new update butler checker
     *
     * @param data butler plugin data
     * @return Updater instance
     * @deprecated Lyna is the prefered way to check for updates.
     */
    @Deprecated
    public static Updater<DefaultUpdateResponse, ButlerUpdateData> butler(ButlerUpdateData data) {
        return new ButlerUpdateChecker(data);
    }

    /**
     * Create a new lyna update check.
     * <p>
     * A Lyna update check is made to work together with a <a href="https://github.com/rainbowdashlabs/lyna">Lyna instance</a>.
     * <p>
     * Additionally, a {@code build.data} file is required, which is created by the <a href="https://github.com/rainbowdashlabs/publishdata">publishdata plugin</a>.
     *
     * @param data lyna plugin data
     * @return Updater instance
     */
    public static Updater<LynaUpdateResponse, LynaUpdateData> lyna(LynaUpdateData data) {
        return new LynaUpdateChecker(data);
    }

    @Override
    public void run() {
        performCheck(true);
    }

    /**
     * Performs an update check with the saved data. This can be repeated in a scheduler.
     */
    public final void performCheck(boolean silent) {
        // don't check if update is already available
        if (updateAvailable) return;

        if (!silent) {
            plugin.getLogger().info("§2Checking for new Version...");
        }

        var optLatest = checkUpdate(data);
        if (optLatest.isPresent()) {
            lastCheck = optLatest.get();
            updateAvailable = optLatest.get().isOutdated();
        } else {
            plugin.getLogger().info("Could not check latest version.");
            return;
        }

        if (updateAvailable) {
            logUpdateMessage();
            if (data.isAutoUpdate()) {
                if (!downloaded) {
                    downloaded = update();
                }
                registerListener(new DownloadedNotifier<>(plugin, data, lastCheck, downloaded));
            } else {
                registerListener(new UpdateNotifier<>(plugin, data, lastCheck));
            }
        } else {
            if (!silent) {
                plugin.getLogger().info("§2Plugin is up to date.");
            }
        }
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
    protected abstract Optional<V> checkUpdate(T data);

    /**
     * Start the update check thread.
     * <p>
     * This will check every 6 hours if an update is available.
     */
    public void start() {
        executor.scheduleAtFixedRate(this, 0, 6, TimeUnit.HOURS);
    }

    private void logUpdateMessage() {
        data.updateMessage(lastCheck).lines().forEach(line -> {
            plugin.getLogger().info(line.replaceAll("§[0-9a-fklmnor]", ""));
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
