/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.localization;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Function;

public class LocalizerBuilder {
    private final Plugin plugin;
    private final String fallbackLocale;
    private String localesPath = "messages";
    private String localesPrefix = "messages";
    private Function<Player, String> userLocale;
    private String[] includedLocales = new String[0];

    LocalizerBuilder(Plugin plugin, String fallbackLocale) {
        this.plugin = plugin;
        this.fallbackLocale = fallbackLocale;
        userLocale = p -> fallbackLocale;
    }

    public LocalizerBuilder setLocalesPath(String localesPath) {
        this.localesPath = localesPath;
        return this;
    }

    public LocalizerBuilder setLocalesPrefix(String localesPrefix) {
        this.localesPrefix = localesPrefix;
        return this;
    }

    public LocalizerBuilder setUserLocale(Function<Player, String> userLocale) {
        this.userLocale = userLocale;
        return this;
    }

    public LocalizerBuilder setIncludedLocales(String... includedLocales) {
        this.includedLocales = includedLocales;
        return this;
    }

    public Localizer build() {
        return new Localizer(plugin, localesPath, localesPrefix, fallbackLocale, userLocale, includedLocales);
    }
}
