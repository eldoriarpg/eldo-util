/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

/**
 * Notifier to notice server owners on server join.
 *
 * @since 1.0.0
 */
public abstract class Notifier implements Listener {
    protected final Plugin plugin;
    protected final UpdateData data;
    protected final String latest;

    public Notifier(Plugin plugin, UpdateData data, String latest) {
        this.plugin = plugin;
        this.data = data;
        this.latest = latest;
    }

    public String latest() {
        return latest;
    }

    @EventHandler
    public abstract void onPlayerJoin(PlayerJoinEvent event);
}
