/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.localization;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.PropertyKey;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Basic Interface for localizer implementations.
 * <p>
 * Also allows access to localizer instances per plugin.
 *
 * @since 1.0.0
 */
public interface ILocalizer {
    Map<Class<? extends Plugin>, ILocalizer> LOCALIZER = new HashMap<>();
    ILocalizer DEFAULT = new DummyLocalizer();

    static ILocalizer getPluginLocalizer(Plugin plugin) {
        if (plugin == null) return DEFAULT;
        return getPluginLocalizer(plugin.getClass());
    }

    static ILocalizer getPluginLocalizer(Class<? extends Plugin> plugin) {
        if (plugin == null) return DEFAULT;
        return LOCALIZER.getOrDefault(plugin, DEFAULT);
    }

    static String escape(String string) {
        return String.format("$%s$", string);
    }

    /**
     * Create a new localizer instance with default values.
     * <p>
     * The message path and prefix will be "messages" and the fallback language the "en_US" locale.
     * <p>
     * This instance will create locale files, which are provided in the resources directory.
     * <p>
     * After this it will updates all locale files inside the locales directory. For this the ref keys from the internal
     * default locale file will be used.
     * <p>
     * After a update check and a update if needed it will load the provided language or the fallback language if the
     * provided language does not exists.
     *
     * @param plugin          instance of plugin
     * @param includedLocales internal provided locales
     * @return the created localizer instance
     */
    static ILocalizer create(Plugin plugin,
                             String... includedLocales) {
        return create(plugin, "messages", "messages", Locale.US, includedLocales);
    }

    /**
     * Create a new localizer instance.
     * <p>
     * This instance will create locale files, which are provided in the resources directory.
     * <p>
     * After this it will updates all locale files inside the locales directory. For this the ref keys from the internal
     * default locale file will be used.
     * <p>
     * After a update check and a update if needed it will load the provided language or the fallback language if the
     * provided language does not exists.
     *
     * @param plugin          instance of plugin
     * @param localesPath     path of the locales directory
     * @param localesPrefix   prefix of the locale files
     * @param fallbackLocale  fallbackLocale
     * @param includedLocales internal provided locales
     * @return the created localizer instance
     */
    static ILocalizer create(Plugin plugin, String localesPath,
                             String localesPrefix, Locale fallbackLocale, String... includedLocales) {
        ILocalizer localizer = new Localizer(plugin, localesPath, localesPrefix, fallbackLocale, includedLocales);
        LOCALIZER.put(plugin.getClass(), localizer);
        return localizer;
    }

    /**
     * Sets the locale of the localizer instance.
     *
     * @param language language to set.
     */
    void setLocale(String language);

    /**
     * Get a message.
     *
     * @param key          message key
     * @param replacements replacements for replacement keys
     * @return message with replaced replacements if present.
     */
    String getMessage(String key, Replacement... replacements);

    /**
     * Returns all available locales.
     *
     * @return array of registered locales.
     */
    String[] getIncludedLocales();

    /**
     * Add requested locale codes in runtime.
     * <p>
     * This has to be done before calling {@link #setLocale(String)}
     * <p>
     * Every key has one default value which will be added to the file if the key is not present.
     *
     * @param runtimeLocaleCodes map with locales codes to add.
     */
    void addLocaleCodes(Map<String, String> runtimeLocaleCodes);

    /**
     * Translates a String with Placeholders. Can handle multiple messages with replacements. Add replacements in the
     * right order.
     *
     * @param message      Message to translate
     * @param replacements Replacements in the right order.
     * @return Replaced Messages
     * @since 1.2.3
     */
    String localize(String message, Replacement... replacements);
}
