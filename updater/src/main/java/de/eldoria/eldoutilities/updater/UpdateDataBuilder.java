/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import de.eldoria.eldoutilities.messages.MessageSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class UpdateDataBuilder<T extends UpdateDataBuilder<?, ?>, V extends UpdateData<?>> {
    protected final Plugin plugin;
    protected String notifyPermission = "eldoutilities.admin";
    protected boolean notifyUpdate = true;
    protected boolean autoUpdate;
    protected String updateUrl;
    protected String updateMessage = UpdaterMessages.NOTICE;
    protected @Nullable MessageSender messageSender;
    protected boolean askForAutoUpdate = true;
    protected @Nullable Consumer<AutoUpdateDecision> autoUpdateHook;

    public UpdateDataBuilder(Plugin plugin) {
        this.plugin = plugin;
        updateUrl = plugin.getDescription().getWebsite();
    }

    public T notifyPermission(String notifyPermission) {
        this.notifyPermission = notifyPermission;
        return (T) this;
    }

    /**
     * Whether players with the notify permission get the update notice when they join. The console always gets it.
     * Defaults to true.
     *
     * @param notifyUpdate false to tell only the console
     * @return builder instance
     */
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

    /**
     * Whether admins may be asked in chat to switch automatic updates on. Pass {@code false} once the hook received
     * {@link AutoUpdateDecision#NEVER_ASK}.
     *
     * @param askForAutoUpdate true to offer automatic updates in chat
     * @return builder instance
     */
    public T askForAutoUpdate(boolean askForAutoUpdate) {
        this.askForAutoUpdate = askForAutoUpdate;
        return (T) this;
    }

    /**
     * Lets admins switch automatic updates on from chat: the update notice gets a button for it, and after a manual
     * download they are asked whether future updates should be installed automatically. The hook receives their
     * decision so the plugin can store it in its config; it runs on the server's global thread. Without a hook,
     * nothing is offered.
     *
     * @param autoUpdateHook receives the decisions
     * @return builder instance
     */
    public T autoUpdateHook(Consumer<AutoUpdateDecision> autoUpdateHook) {
        this.autoUpdateHook = autoUpdateHook;
        return (T) this;
    }

    /**
     * Applies the settings every update data shares to newly built data. Builders call this on the data they build.
     *
     * @param data built data
     * @return the same data
     */
    protected V applyCommon(V data) {
        data.autoUpdateOptIn(askForAutoUpdate, autoUpdateHook);
        return data;
    }

    public abstract V build();
}
