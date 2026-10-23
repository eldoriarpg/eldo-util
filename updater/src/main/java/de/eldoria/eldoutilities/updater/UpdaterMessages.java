/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import de.eldoria.eldoutilities.localization.ILocalizer;
import org.bukkit.plugin.Plugin;

import java.util.Map;

import static java.util.Map.entry;

/**
 * The texts the updater shows to players, as locale codes with English defaults.
 * <p>
 * With a localizer the codes are added to the plugin's locale files, so server owners can translate them and every
 * player reads them in their own language. Plugins can ship translations for these codes in their bundled locale
 * files. Without a localizer the English defaults are sent.
 * <p>
 * Texts use MiniMessage. Placeholders are tags: {@code <plugin_name>}, {@code <new_version>},
 * {@code <current_version>} and {@code <website>}, and for Lyna additionally {@code <new_time>},
 * {@code <new_date_time>}, {@code <current_time>} and {@code <current_date_time>}.
 */
public final class UpdaterMessages {
    public static final String NOTICE = "eldoutilities.updater.notice";
    public static final String LYNA_NOTICE = "eldoutilities.updater.lyna-notice";
    public static final String DOWNLOADED = "eldoutilities.updater.downloaded";
    public static final String DOWNLOAD_BUTTON = "eldoutilities.updater.download-button";
    public static final String DOWNLOAD_HOVER = "eldoutilities.updater.download-hover";
    public static final String DOWNLOAD_DENIED = "eldoutilities.updater.download-denied";
    public static final String DOWNLOADING = "eldoutilities.updater.downloading";
    public static final String DOWNLOAD_DONE = "eldoutilities.updater.download-done";
    public static final String DOWNLOAD_FAILED = "eldoutilities.updater.download-failed";
    public static final String AUTO_BUTTON = "eldoutilities.updater.auto-button";
    public static final String AUTO_HOVER = "eldoutilities.updater.auto-hover";
    public static final String AUTO_PROMPT = "eldoutilities.updater.auto-prompt";
    public static final String AUTO_YES = "eldoutilities.updater.auto-yes";
    public static final String AUTO_LATER = "eldoutilities.updater.auto-later";
    public static final String AUTO_NEVER = "eldoutilities.updater.auto-never";
    public static final String AUTO_ENABLED = "eldoutilities.updater.auto-enabled";
    public static final String AUTO_ASK_LATER = "eldoutilities.updater.auto-ask-later";
    public static final String AUTO_NOT_ASKED = "eldoutilities.updater.auto-not-asked";
    public static final String AUTO_UNAVAILABLE = "eldoutilities.updater.auto-unavailable";
    public static final String UNKNOWN_ACTION = "eldoutilities.updater.unknown-action";
    public static final String MAJOR_NOTICE = "eldoutilities.updater.major-notice";
    public static final String MAJOR_CONFIRM = "eldoutilities.updater.major-confirm";
    public static final String MAJOR_CONFIRM_BUTTON = "eldoutilities.updater.major-confirm-button";

    private static final Map<String, String> DEFAULTS = Map.ofEntries(
            entry(NOTICE, """
                    New version of <aqua><plugin_name></aqua> available.
                    Newest version: <green><new_version></green>! Current version: <red><current_version></red>!
                    Download the new version here: <aqua><website>"""),
            entry(LYNA_NOTICE, """
                    New version of <gold><plugin_name></gold> available.
                    New version: <gold><new_version></gold>, published <gold><new_time></gold> ago. <new_date_time>
                    Current version: <gold><current_version></gold>, published <red><current_time></red> ago. <current_date_time>
                    Download the new version: <gold><website>"""),
            entry(DOWNLOADED, """
                    New version of <aqua><plugin_name></aqua> downloaded.
                    Newest version: <green><new_version></green>! Current version: <red><current_version></red>!
                    Restart the server to apply the update. Patch notes: <aqua><website>"""),
            entry(DOWNLOAD_BUTTON, "<green>[Download]</green>"),
            entry(DOWNLOAD_HOVER, "<green>Download version <new_version>"),
            entry(DOWNLOAD_DENIED, "You are not allowed to download updates of <plugin_name>."),
            entry(DOWNLOADING, "Downloading the update of <plugin_name>..."),
            entry(DOWNLOAD_DONE, "<green>Update of <plugin_name> downloaded. Restart the server to apply it."),
            entry(DOWNLOAD_FAILED, "The update of <plugin_name> could not be downloaded. See the console for details."),
            entry(AUTO_BUTTON, "<aqua>[Enable auto updates]</aqua>"),
            entry(AUTO_HOVER, "<aqua>Install new stable versions of <plugin_name> automatically"),
            entry(AUTO_PROMPT, "Install updates of <plugin_name> automatically in the future?"),
            entry(AUTO_YES, "<green>[Yes]</green>"),
            entry(AUTO_LATER, "<yellow>[Not now]</yellow>"),
            entry(AUTO_NEVER, "<red>[Don't ask again]</red>"),
            entry(AUTO_ENABLED, "<green>Automatic updates of <plugin_name> are enabled."),
            entry(AUTO_ASK_LATER, "You will be asked again after the next manual update."),
            entry(
                    AUTO_NOT_ASKED,
                    "You will not be asked again. Automatic updates can still be enabled in the config of <plugin_name>."),
            entry(AUTO_UNAVAILABLE, "Automatic updates of <plugin_name> can only be enabled in its config."),
            entry(UNKNOWN_ACTION, "Unknown action. Use download, enable-auto, later or never."),
            entry(
                    MAJOR_NOTICE,
                    "<red>This is a major update. It may not be compatible with your server or other plugins, so it is not installed automatically. Read the patch notes before updating."),
            entry(
                    MAJOR_CONFIRM,
                    "<red>This is a major update of <plugin_name> and may not be compatible with your server or other plugins. Read the patch notes first."),
            entry(MAJOR_CONFIRM_BUTTON, "<red>[Download anyway]</red>"));

    private UpdaterMessages() {}

    /**
     * The updater's locale codes with their English defaults.
     *
     * @return codes and defaults
     */
    public static Map<String, String> defaults() {
        return DEFAULTS;
    }

    /**
     * Adds the updater's locale codes to the plugin's localizer, if it has one.
     *
     * @param plugin plugin owning the updater
     */
    static void register(Plugin plugin) {
        var localizer = ILocalizer.getPluginLocalizer(plugin);
        if (localizer != ILocalizer.DEFAULT) localizer.addLocaleCodes(DEFAULTS);
    }

    /**
     * The message to send for a text: the updater's codes become a locale tag when the plugin has a localizer and
     * their English default otherwise. Any other text, including a plugin's own locale code, is returned as it is.
     *
     * @param plugin plugin owning the updater
     * @param text   locale code or text
     * @return message to send
     */
    public static String resolve(Plugin plugin, String text) {
        if (!DEFAULTS.containsKey(text)) return text;
        if (ILocalizer.getPluginLocalizer(plugin) == ILocalizer.DEFAULT) return DEFAULTS.get(text);
        return ILocalizer.escape(text);
    }
}
