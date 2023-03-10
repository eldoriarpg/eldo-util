/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import org.bukkit.plugin.Plugin;

public abstract class UpdateDataBuilder<T extends UpdateDataBuilder<?,?>, V extends UpdateData> {
    protected final Plugin plugin;
    protected String notifyPermission = "eldoutilitites.admin";
    protected boolean notifyUpdate;
    protected boolean autoUpdate;
    protected String updateUrl;
    protected String updateMessage = """
            New version of §b {plugin_name} §r available.
            Newest version: §a" + {new_version} + "§r! Current version: §c{current_version}§r!
            Download new version here: §b{website}
            """.stripIndent();

    public UpdateDataBuilder(Plugin plugin) {
        this.plugin = plugin;
        updateUrl = plugin.getDescription().getWebsite();
    }

    public T notifyPermission(String notifyPermission) {
        this.notifyPermission = notifyPermission;
        return (T) this;
    }

    public T notifyUpdate(boolean notifyUpdate) {
        this.notifyUpdate = notifyUpdate;
        return (T) this;
    }

    public T autoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        return (T) this;
    }

    public T updateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
        return (T) this;
    }

    public T updateMessage(String updateMessage) {
        this.updateMessage = updateMessage;
        return (T) this;
    }

    public abstract V build();
}
