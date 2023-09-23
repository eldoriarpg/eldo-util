/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.localization;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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
    Pattern LOCALIZATION_CODE = Pattern.compile("([a-zA-Z0-9_\\-]+?)\\.([a-zA-Z0-9_.\\-]+?)");

    static ILocalizer getPluginLocalizer(Plugin plugin) {
        if (plugin == null) return DEFAULT;
        return getPluginLocalizer(plugin.getClass());
    }

    static ILocalizer getPluginLocalizer(Class<? extends Plugin> plugin) {
        if (plugin == null) return DEFAULT;
        return LOCALIZER.getOrDefault(plugin, DEFAULT);
    }

    static String escape(String propertyKey) {
        return String.format("<i18n:%s>", propertyKey);
    }

    static boolean isLocaleCode(String message) {
        return LOCALIZATION_CODE.matcher(message).matches();
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
     * @param key message key
     * @return message with replaced replacements if present.
     */
    String getMessage(String key);

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

    @Nullable
    String getValue(String key);

    /**
     * Translates a String with Placeholders. Can handle multiple messages with replacements. Add replacements in the
     * right order.
     *
     * @param message Message to translate
     * @return Replaced Messages
     * @since 1.2.3
     */
    String localize(String message);

    void registerChild(ILocalizer localizer);

    public class DummyLocalizer implements ILocalizer {
        @Override
        public void setLocale(String language) {
        }

        @Override
        public String getMessage(String key) {
            return key;
        }

        @Override
        public String[] getIncludedLocales() {
            return new String[0];
        }

        @Override
        public void addLocaleCodes(Map<String, String> runtimeLocaleCodes) {
        }

        @Override
        public @Nullable String getValue(String key) {
            return null;
        }

        @Override
        public String localize(String message) {
            return message;
        }

        @Override
        public void registerChild(ILocalizer localizer) {

        }
    }
}
