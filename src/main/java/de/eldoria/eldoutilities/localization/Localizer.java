/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.localization;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Compact localizer class.
 * <p>
 * Easy to use and fully automatic setup and updating of locales.
 * <p>
 * Requires to have at least one default locale and one fallback locale in the resources. Use the {@link
 * #Localizer(Plugin, String, String, Locale, String...)} constructor for initial setup. This will create missing files
 * and updates existing files.
 * <p>
 * You can change the currently used locale every time via {@link #setLocale(String)}.
 * <p>
 * The localizer also allows to use locales which are not included in the ressources folder.
 *
 * @since 1.0.0
 */
public class Localizer implements ILocalizer {
    private static final Pattern EMBED_LOCALIZATION_CODE = Pattern.compile("\\$([a-zA-Z0-9_.]+?)\\$");
    private static final Pattern LOCALIZATION_CODE = Pattern.compile("([a-zA-Z0-9_.]+?)");

    private final ResourceBundle fallbackBundle;
    private final Plugin plugin;
    private final String localesPath;
    private final String localesPrefix;
    private final String[] includedLocales;
    private final Pattern localePattern = Pattern.compile("_(([a-zA-Z]{2})(_[a-zA-Z]{2})?)\\.properties");
    private final Map<String, String> runtimeLocaleCodes = new HashMap<>();
    private ResourceBundle bundle;
    List<ILocalizer> childs = new ArrayList<>();
    private boolean checked;

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
     */
    Localizer(Plugin plugin, String localesPath,
              String localesPrefix, Locale fallbackLocale, String... includedLocales) {
        this.plugin = plugin;
        this.localesPath = localesPath;
        this.localesPrefix = localesPrefix;
        this.includedLocales = includedLocales;
        ResourceBundle fallbackBundle = new DummyResourceBundle();
        try {
            fallbackBundle = getBundle(fallbackLocale);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not read fallback file", e);
        }
        this.fallbackBundle = fallbackBundle;
        LOCALIZER.put(plugin.getClass(), this);
        createDefaults();
    }

    private void createDefaults() {
        Map<String, String> locales = new HashMap<>();
        locales.put("error.invalidArguments", "Invalid arguments.\nSyntax: %SYNTAX%");
        locales.put("error.invalidCommand", "Invalid Command");
        locales.put("error.endOfRoute", "Please choose a subcommand. Available commands are:\n%COMMANDS%");
        locales.put("error.permission", "You do not have the permissionNode to do this. (%PERMISSION%)");
        locales.put("error.invalidRange", "This value is out of range. Min: %MIN% Max: %MAX%");
        locales.put("error.invalidEnumValue", "Invalid input value. Valid inputs are %VALUES%.");
        locales.put("error.invalidMaterial", "Invalid material.");
        locales.put("error.invalidNumber", "Invalid number");
        locales.put("error.invalidBoolean", "Invalid value, %TRUE% or %FALSE%");
        locales.put("error.invalidLength", "This input is too long. Max: %MAX% chars.");
        locales.put("error.notOnline", "Invalid player. This player is not online.");
        locales.put("error.unkownPlayer", "Invalid player. This player has never played on this server.");
        locales.put("error.unkownWorld", "Invalid player. This player has never played on this server.");
        locales.put("error.notAsConsole", "This command can not be executed from console.");
        locales.put("error.onlyPlayer", "This command can only be used by players.");
        locales.put("error.onlyConsole", "This command can only be used by console.");
        locales.put("error.invalidSender", "This command can not be executed from here.");
        locales.put("error.missingArgument", "Argument %INDEX% is accessed but not present.");
        locales.put("error.notAsPlayer", "This command can not be executed as player");
        locales.put("error.tooSmall", "The number is too small. Min: %MIN%");
        locales.put("error.tooLarge", "The number is too Large. Max: %MAX%");
        locales.put("about", "%PLUGIN_NAME% by %AUTHORS%\nVersion: %VERSION%\nSpigot: %WEBSITE%\nSupport: %DISCORD%");
        locales.put("dialog.accept", "accept");
        locales.put("dialog.deny", "deny");
        locales.put("dialog.add", "add");
        locales.put("dialog.remove", "remove");
        locales.put("dialog.leftClickChange", "Left click to change");
        locales.put("dialog.rightClickRemove", "Right click to remove");

        addLocaleCodes(locales);
    }

    /**
     * Change the locale to the language. If the locale is not present the fallback locale will be used.
     *
     * @param language language to be used
     */
    @Override
    public void setLocale(String language) {
        if (!checked) {
            createOrUpdateLocaleFiles();
            checked = true;
        }

        try {
            this.bundle = getBundle(getLocaleFile(language));
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load locale file " + getLocaleFile(language), e);
            this.bundle = fallbackBundle;
        }
    }

    /**
     * Translates a String with Placeholders. Can handle multiple messages with replacements.
     *
     * @param key          Key of message
     * @param replacements Replacements
     * @return Replaced Messages
     */
    @Override
    public String getMessage(String key, Replacement... replacements) {
        var result = getValue(key);

        if (result == null) {
            plugin.getLogger().warning("Key " + key + " is missing in fallback file.");
            result = key;
        }

        return invokeReplacements(result, replacements);
    }

    @Override
    @Nullable
    public String getValue(String key) {
        String result = null;
        if (bundle.containsKey(key)) {
            try {
                result = bundle.getString(key);
            } catch (MissingResourceException e) {
                // ignore
            }
        }
        if (result == null && fallbackBundle.containsKey(key)) {
            try {
                result = fallbackBundle.getString(key);
            } catch (MissingResourceException e) {
                // ignore
            }
        }

        if (result == null) {
            for (var child : childs) {
                result = child.getValue(key);
                if (result != null) break;
            }
        }
        return result;
    }

    private Path getLocalePath() {
        return plugin.getDataFolder().toPath().resolve(localesPath);
    }

    private Path getLocaleFile(String locale) {
        return getLocalePath().resolve(getLocaleFileName(locale));
    }

    private String getLocaleFileName(String locale) {
        return String.format("%s_%s.properties", localesPrefix, locale);
    }

    private void createDefaultFiles() {
        // Create the property files if they do not exists.
        for (var locale : includedLocales) {
            var localeFile = getLocaleFile(locale).toFile();
            if (localeFile.exists()) {
                continue;
            }

            try (var resource = plugin.getResource(getLocaleFileName(locale))) {
                if (resource == null)
                    throw new FileNotFoundException("No locale file for " + locale + "found in resources.");
                Files.copy(resource, getLocaleFile(locale));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create default message file for locale " + locale);
                continue;
            }

            plugin.getLogger().info("Created default locale " + getLocaleFileName(locale));
        }
    }

    private boolean isLocaleFile(Path path) {
        if (path.toFile().isDirectory()) return false;
        if (path.toFile().getName().matches(localesPrefix + "_[a-zA-Z]{2}(_[a-zA-Z]{2})?\\.properties")) return true;
        plugin.getLogger().info(path + " is not a valid message file. Skipped.");
        return false;
    }

    private ResourceBundle getDefaultBundle() throws IOException {
        try (var input = plugin.getResource(localesPrefix + ".properties")) {
            //TODO: Lazy getter
            return new PropertyResourceBundle(input);
        }
    }

    private ResourceBundle getBundle(String locale) throws IOException {
        try (var input = plugin.getResource(getLocaleFileName(locale))) {
            if (input == null) {
                return getDefaultBundle();
            }
            return new PropertyResourceBundle(input);
        }
    }

    private ResourceBundle getBundle(Locale locale) throws IOException {
        if (locale == null) {
            return getDefaultBundle();
        }
        return getBundle(locale.toString());
    }

    private ResourceBundle getBundle(Path locale) throws IOException {
        try (var input = Files.newInputStream(locale)) {
            return new PropertyResourceBundle(input);
        }
    }

    private Map<String, String> bundleMap(Path path) throws IOException {
        var map = new TreeMap<String, String>(String::compareToIgnoreCase);
        Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                .map(line -> line.split("=", 2)).filter(line -> line.length == 2)
                .forEach(line -> map.put(line[0], line[1]));
        return map;
    }

    private Set<String> getDefaultKeys() throws IOException {
        Set<String> defaultKeys = new HashSet<>(Collections.list(getDefaultBundle().getKeys()));
        defaultKeys.addAll(runtimeLocaleCodes.keySet());
        return defaultKeys;
    }

    private String timestamp() {
        return DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now());
    }

    private void createOrUpdateLocaleFiles() {
        try {
            Files.createDirectories(getLocalePath());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create message directory.", e);
        }

        createDefaultFiles();

        List<Path> localeFiles = new ArrayList<>();

        // Load all property files
        try (var files = Files.list(getLocalePath())) {
            files.filter(this::isLocaleFile).forEach(localeFiles::add);
        } catch (IOException e) {
            // we will try to continue with the successfull loaded files. If there are none thats not bad.
            plugin.getLogger().log(Level.WARNING, "Failed to load message files.");
        }


        // Update keys of existing files.
        for (var path : localeFiles) {

            // get the default keys.
            Set<String> updateKeys;
            try {
                updateKeys = getDefaultKeys();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not load reference file... Aborting update!", e);
                return;
            }

            // try to search for a included updated version.
            ResourceBundle refBundle;
            try {
                refBundle = getBundle(extractLocale(path));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not load any reference locale", e);
                continue;
            }

            ResourceBundle bundle;
            try {
                bundle = getBundle(path);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not load bundle " + path, e);
                continue;
            }

            // load the external property file.
            Map<String, String> bundleMap;
            try {
                bundleMap = bundleMap(path);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not update locale " + path + ".", e);
                continue;
            }

            updateKeys.removeAll(bundleMap.keySet());

            if (updateKeys.isEmpty()) {
                plugin.getLogger().info("§2Locale " + path + " is up to date.");
                continue;
            }
            plugin.getLogger().info("§2Updating " + path + ".");

            // check if ref key is in locale
            for (var currKey : updateKeys) {
                var value = refBundle.containsKey(currKey) ? refBundle.getString(currKey) : runtimeLocaleCodes.getOrDefault(currKey, "");
                // Add the property with the value if it exists in a internal file.
                bundleMap.put(currKey, value);
                plugin.getLogger().info("§2Added: §3" + currKey + "§6=§b" + value.replace("\n", "\\n"));
            }

            List<String> lines = new ArrayList<>();
            lines.add("# File automatically updated at " + timestamp());

            bundleMap.entrySet().stream()
                    .map(e -> String.format("%s=%s", e.getKey(), e.getValue().replace("\n", "\\n")))
                    .forEach(lines::add);

            try {
                Files.write(path, lines, StandardCharsets.UTF_8);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not update locale " + path + ".", e);
                continue;
            }
            plugin.getLogger().info("§2Updated locale " + path + ". Please check your translation.");
        }
    }

    private Locale extractLocale(Path filename) {
        var matcher = localePattern.matcher(filename.toFile().getName());
        if (matcher.find()) {
            var group = matcher.group(1);
            var s = group.split("_");
            if (s.length == 1) {
                return new Locale(s[0]);
            }
            return new Locale(s[0], s[1]);
        }
        return null;
    }

    /**
     * Translates a String with Placeholders. Can handle multiple messages with replacements.
     *
     * @param message      Message to translate
     * @param replacements Replacements.
     * @return Replaced Messages
     */
    @Override
    public String localize(String message, Replacement... replacements) {
        if (message == null) {
            return null;
        }

        // If the matcher doesn't find any key we assume its a simple message.
        if (!EMBED_LOCALIZATION_CODE.matcher(message).find()) {
            if (LOCALIZATION_CODE.matcher(message).matches()) {
                message = getMessage(message, replacements);
            }
        }

        // find locale codes in message
        var matcher = EMBED_LOCALIZATION_CODE.matcher(message);
        List<String> keys = new ArrayList<>();
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }

        var result = message;
        for (var match : keys) {
            //Replace current locale code with result
            result = result.replace("$" + match + "$", getMessage(match, replacements));
        }

        result = invokeReplacements(result, replacements);

        if (EMBED_LOCALIZATION_CODE.matcher(result).find()) {
            return localize(result, replacements);
        }

        return result;
    }

    private String invokeReplacements(String message, Replacement... replacements) {
        for (var replacement : replacements) {
            message = replacement.invoke(message);
        }
        return message;
    }

    /**
     * Get currently registered locales.
     *
     * @return array of available locales.
     */
    @Override
    public String[] getIncludedLocales() {
        return includedLocales;
    }

    @Override
    public void addLocaleCodes(Map<String, String> runtimeLocaleCodes) {
        this.runtimeLocaleCodes.putAll(runtimeLocaleCodes);
    }

    @Override
    public void registerChild(ILocalizer localizer) {
        childs.add(localizer);
    }

    private class DummyResourceBundle extends ResourceBundle {

        @Override
        protected Object handleGetObject(@NotNull String key) {
            return null;
        }

        @NotNull
        @Override
        public Enumeration<String> getKeys() {
            return Collections.emptyEnumeration();
        }
    }
}
