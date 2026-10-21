/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import de.eldoria.eldoutilities.messages.MessageSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public abstract class UpdateDataBuilder<T extends UpdateDataBuilder<?, ?>, V extends UpdateData<?>> {
    protected final Plugin plugin;
    protected String notifyPermission = "eldoutilitites.admin";
    protected boolean notifyUpdate;
    protected boolean autoUpdate;
    protected String updateUrl;
    protected String updateMessage = UpdaterMessages.NOTICE;
    protected @Nullable MessageSender messageSender;

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

    /**
     * The message sender the updater sends its messages through. Without one, the sender registered for the plugin
     * is used.
     *
     * @param messageSender message sender of the plugin
     * @return builder instance
     */
    public T messageSender(MessageSender messageSender) {
        this.messageSender = messageSender;
        return (T) this;
    }

    public T updateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
        return (T) this;
    }

    /**
     * Set the update notification message. Provides some placeholders to modify the message with runtime information
     * <table border="1">
     *   <caption><b>Available placeholder</b></caption>
     *   <tr>
     *     <td>plugin_name</td> <td>The name of the plugin</td>
     *   </tr>
     *   <tr>
     *     <td>new_version</td> <td>The new version string</td>
     *   </tr>
     *   <tr>
     *     <td>current_version</td> <td>The current version string</td>
     *   </tr>
     *   <tr>
     *     <td>website</td> <td>The website set in the plugin.yml or provided by {@link #updateUrl(String)}</td>
     *   </tr>
     * </table>
     *
     * @param updateMessage the update message to be sent
     * @return builder instance
     */
    public T updateMessage(String updateMessage) {
        this.updateMessage = updateMessage;
        return (T) this;
    }

    public abstract V build();
}
