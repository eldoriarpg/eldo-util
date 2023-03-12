/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Update data base implementation.
 *
 * @since 1.0.0
 */
public abstract class UpdateData<T extends UpdateResponse> {
    private final String notifyPermission;
    private final boolean notifyUpdate;
    private final Plugin plugin;
    private final boolean autoUpdate;
    private final String updateUrl;
    private final String updateMessage;

    /**
     * Creates a new Update data.
     *
     * @param plugin           plugin to check the version for
     * @param notifyPermission enter the permission which a user should have to get a notification. null to disable
     *                         login notification.
     * @param notifyUpdate     set to true to notify admins on login
     * @param autoUpdate       true if the updater should attempt to update the plugin. If true the updater should
     *                         implement the {@link Updater#update()} method.
     */
    public UpdateData(Plugin plugin, String notifyPermission, boolean notifyUpdate, boolean autoUpdate, String updateUrl, String updateMessage) {
        this.plugin = plugin;
        this.notifyPermission = notifyPermission;
        this.notifyUpdate = notifyUpdate;
        this.autoUpdate = autoUpdate;
        this.updateUrl = updateUrl;
        this.updateMessage = updateMessage;
    }

    public String notifyPermission() {
        return notifyPermission;
    }

    public boolean isNotifyUpdate() {
        return notifyUpdate;
    }

    public Plugin plugin() {
        return plugin;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    protected Map<String, Object> replacements(T updateResponse) {
        var replacements = new HashMap<String, Object>();
        replacements.put("plugin_name", plugin.getName());
        replacements.put("new_version", updateResponse.latestVersion());
        replacements.put("current_version", plugin.getDescription().getVersion());
        replacements.put("website", updateUrl);
        return replacements;
    }

    public String updateMessage(T updateResponse) {
        var message = updateMessage;
        for (var entry : replacements(updateResponse).entrySet()) {
            message = message.replace("{%s}".formatted(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return message;
    }
}
