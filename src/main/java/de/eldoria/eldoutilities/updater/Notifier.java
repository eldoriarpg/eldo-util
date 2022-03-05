/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
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
    protected final String permission;
    protected final String newestVersion;

    public Notifier(Plugin plugin, String permission, String latestVersion) {
        this.plugin = plugin;
        this.permission = permission;
        this.newestVersion = latestVersion;
    }

    @EventHandler
    public abstract void onPlayerJoin(PlayerJoinEvent event);
}
