/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import de.eldoria.eldoutilities.updater.lynaupdater.LynaUpdateChecker;
import de.eldoria.eldoutilities.updater.lynaupdater.LynaUpdateData;
import de.eldoria.eldoutilities.updater.lynaupdater.LynaUpdateDataBuilder;
import de.eldoria.eldoutilities.updater.lynaupdater.LynaUpdateResponse;
import de.eldoria.eldoutilities.updater.lynaupdater.UpgradeTokenStore;
import de.eldoria.eldoutilities.updater.notifier.DownloadedNotifier;
import de.eldoria.eldoutilities.updater.notifier.UpdateNotifier;
import de.eldoria.eldoutilities.updater.spigotupdater.SpigotUpdateChecker;
import de.eldoria.eldoutilities.updater.spigotupdater.SpigotUpdateData;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.net.http.HttpClient;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Base implementation for Updater.
 * <p>
 * Checks and downloads run on the updater's own thread, one after another. {@link #reconfigure(UpdateData)} hands
 * the updater new settings, for example after a config reload, and {@link #stop()} ends it.
 *
 * @param <T> type of Updater
 */
public abstract class Updater<V extends UpdateResponse, T extends UpdateData<V>> implements Listener, Runnable {
    private final Plugin plugin;
    private volatile T data;
    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
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
    private Listener notifier;
    private String downloadCommand;

    protected Updater(T data) {
        this.plugin = data.plugin();
        this.data = data;
        UpdaterMessages.register(plugin);
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
     * Create a new lyna update check.
     * <p>
     * A Lyna update check is made to work together with a <a href="https://github.com/rainbowdashlabs/lyna">Lyna instance</a>.
     * <p>
     * Additionally, a {@code build.data} file is required, which is created by the <a href="https://github.com/rainbowdashlabs/publishdata">publishdata plugin</a>.
     * <p>
     * With auto update enabled, new stable releases are downloaded: free products directly, premium products with an
     * upgrade token from the account area of the lyna instance, see {@link LynaUpdateDataBuilder#tokenStore(UpgradeTokenStore)}.
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
            plugin.getLogger().info("Checking for new Version...");
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
            if (data.isAutoUpdate() && updatesAutomatically()) {
                if (!downloaded) {
                    downloaded = update();
                }
                registerListener(new DownloadedNotifier<>(plugin, data, lastCheck, downloaded));
            } else {
                var command = offersDownload() ? downloadCommand() : null;
                if (command != null) {
                    plugin.getLogger().info("Run " + command + " to download it.");
                }
                registerListener(new UpdateNotifier<>(plugin, data, lastCheck, command));
            }
        } else {
            if (!silent) {
                plugin.getLogger().info("Plugin is up to date.");
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
     * Downloads the available update on request, as offered by the download button of the update message.
     * <p>
     * Runs on the update checker thread and does nothing unless {@link #offersDownload()} allows the last checked
     * version.
     *
     * @return future completing with true once the update is downloaded
     */
    public final CompletableFuture<Boolean> download() {
        if (executor.isShutdown()) return CompletableFuture.completedFuture(downloaded);
        return CompletableFuture.supplyAsync(() -> {
            if (!downloaded && lastCheck != null && offersDownload()) {
                downloaded = update();
                if (downloaded) {
                    replaceListener(new DownloadedNotifier<>(plugin, data, lastCheck, true));
                }
            }
            return downloaded;
        }, executor);
    }

    /**
     * Whether the last checked version may be installed without anybody asking for it, when auto update is enabled.
     * Otherwise, the update is announced and, if {@link #offersDownload()} allows it, offered for download.
     *
     * @return true when the update may be installed automatically
     */
    protected boolean updatesAutomatically() {
        return true;
    }

    /**
     * Whether the last checked version can be downloaded on request through {@link #download()}. Updaters that
     * implement {@link #update()} should allow it.
     *
     * @return true when a download button is offered
     */
    protected boolean offersDownload() {
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
     * Replaces the settings of the updater, for example after the plugin reloaded its config.
     * <p>
     * The current notification is removed and a check with the new settings runs right away, so an update that the
     * new settings allow to install automatically is installed then. An update that was already downloaded is not
     * downloaded again.
     *
     * @param data new settings
     * @return future completing once the check with the new settings is done
     */
    public final CompletableFuture<Void> reconfigure(T data) {
        if (executor.isShutdown()) return CompletableFuture.completedFuture(null);
        return CompletableFuture.runAsync(() -> {
            this.data = data;
            updateAvailable = false;
            removeListener();
            performCheck(true);
        }, executor);
    }

    /**
     * Stops the updater for good: no further checks run, and the notification is removed. Use it when the plugin is
     * disabled or its config switches update checks off.
     */
    public final void stop() {
        executor.shutdownNow();
        removeListener();
    }

    /**
     * Start the update check thread.
     * <p>
     * This will check every 6 hours if an update is available.
     */
    public void start() {
        executor.scheduleAtFixedRate(this, 0, 6, TimeUnit.HOURS);
    }

    private void logUpdateMessage() {
        var message = data.updateMessage(lastCheck);
        String text;
        try {
            text = data.messageSender().translatePlain(message, data.placeholders(lastCheck));
        } catch (Throwable e) {
            text = message.replaceAll("(§[0-9a-fklmnor])|(<[^>]*>)", "");
        }
        text.lines().forEach(plugin.getLogger()::info);
    }

    private synchronized String downloadCommand() {
        if (downloadCommand == null) {
            downloadCommand = DownloadUpdateCommand.register(plugin, this);
        }
        return downloadCommand;
    }

    private void registerListener(Listener listener) {
        if (data.isNotifyUpdate() && !notifyActive) {
            notifyActive = true;
            notifier = listener;
            ServerTasks.runGlobal(plugin, () -> plugin.getServer().getPluginManager().registerEvents(listener, plugin));
        }
    }

    private void replaceListener(Listener listener) {
        removeListener();
        registerListener(listener);
    }

    private void removeListener() {
        var previous = notifier;
        notifier = null;
        notifyActive = false;
        if (previous != null) {
            ServerTasks.runGlobal(plugin, () -> HandlerList.unregisterAll(previous));
        }
    }

    public T getData() {
        return data;
    }

    protected HttpClient client() {
        return client;
    }
}
