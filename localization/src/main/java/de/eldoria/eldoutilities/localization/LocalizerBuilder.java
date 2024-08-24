/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.localization;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class LocalizerBuilder {
    private final Plugin plugin;
    private final String fallbackLocale;
    private final Set<String> includedLocales = new HashSet<>();
    private final Map<String, String> runtimeLocaleCodes = new HashMap<>();
    private String localesPath = "messages";
    private String localesPrefix = "messages";
    private Function<Player, String> userLocale;

    LocalizerBuilder(Plugin plugin, String fallbackLocale) {
        this.plugin = plugin;
        this.fallbackLocale = fallbackLocale;
        includedLocales.add(fallbackLocale);
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
        this.includedLocales.addAll(Arrays.stream(includedLocales).toList());
        return this;
    }

    public LocalizerBuilder addLocaleCodes(String key, String value) {
        this.runtimeLocaleCodes.put(key, value);
        return this;
    }

    public LocalizerBuilder addLocaleCodes(Map<String, String> runtimeLocaleCodes) {
        this.runtimeLocaleCodes.putAll(runtimeLocaleCodes);
        return this;
    }

    public Localizer build() {
        return new Localizer(plugin, localesPath, localesPrefix, fallbackLocale, userLocale, includedLocales, runtimeLocaleCodes);
    }
}
