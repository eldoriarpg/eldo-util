/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.notifier;

import de.eldoria.eldoutilities.updater.UpdateData;
import de.eldoria.eldoutilities.updater.UpdateResponse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

/**
 * Notifier to notice server owners on server join.
 *
 * @since 1.0.0
 */
public abstract class Notifier<T extends UpdateResponse> implements Listener {
    protected final Plugin plugin;
    protected final UpdateData<T> data;
    protected final T latest;

    public Notifier(Plugin plugin, UpdateData<T> data, T latest) {
        this.plugin = plugin;
        this.data = data;
        this.latest = latest;
    }

    public T latest() {
        return latest;
    }

    @EventHandler
    public abstract void onPlayerJoin(PlayerJoinEvent event);
}
