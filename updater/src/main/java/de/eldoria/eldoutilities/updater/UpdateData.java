/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import de.eldoria.eldoutilities.messages.MessageSender;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

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
    private @Nullable MessageSender messageSender;

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

    /**
     * Creates a new Update data with the message sender the updater sends its messages through.
     *
     * @param plugin           plugin to check the version for
     * @param notifyPermission permission a user needs to get a notification
     * @param notifyUpdate     set to true to notify admins on login
     * @param autoUpdate       true if the updater should attempt to update the plugin
     * @param updateUrl        address shown in the update notice
     * @param updateMessage    update notice, a text or a locale code
     * @param messageSender    message sender to use, or null for the one registered for the plugin
     */
    public UpdateData(Plugin plugin, String notifyPermission, boolean notifyUpdate, boolean autoUpdate, String updateUrl,
                      String updateMessage, @Nullable MessageSender messageSender) {
        this(plugin, notifyPermission, notifyUpdate, autoUpdate, updateUrl, updateMessage);
        this.messageSender = messageSender;
    }

    /**
     * The message sender the updater sends its messages through: the one handed to the builder, or else the one
     * registered for the plugin.
     *
     * @return message sender
     */
    public MessageSender messageSender() {
        return messageSender != null ? messageSender : MessageSender.getPluginMessageSender(plugin);
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

    /**
     * The update notice. The updater's own locale codes become a locale tag or their English default, see
     * {@link UpdaterMessages#resolve(Plugin, String)}. Placeholders written as {@code {name}} are replaced here; written
     * as tags they are resolved through {@link #placeholders(UpdateResponse)} when the message is sent.
     *
     * @param updateResponse the update check response
     * @return message to send
     */
    public String updateMessage(T updateResponse) {
        var message = UpdaterMessages.resolve(plugin, updateMessage);
        for (var entry : replacements(updateResponse).entrySet()) {
            message = message.replace("{%s}".formatted(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return message;
    }

    /**
     * The placeholders of the update messages as tags, shown as plain text.
     *
     * @param updateResponse the update check response
     * @return placeholder tags
     */
    public TagResolver placeholders(T updateResponse) {
        return TagResolver.resolver(replacements(updateResponse).entrySet().stream()
                .map(entry -> Placeholder.unparsed(entry.getKey(), String.valueOf(entry.getValue())))
                .toList());
    }
}
