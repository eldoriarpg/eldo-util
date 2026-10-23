/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.localization;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static de.eldoria.eldoutilities.localization.ILocalizer.isLocaleCode;

/**
 * Compact localizer class.
 * <p>
 * Easy to use and fully automatic setup and updating of locales.
 * <p>
 * Requires to have at least one default locale and one fallback locale in the resources. Use
 * {@link #builder(Plugin, String)} for the setup. This will create missing files and updates existing files.
 * <p>
 * You can change the currently used locale every time via {@link #setDefaultLocale(String)}.
 * <p>
 * The localizer also allows to use locales which are not included in the resources folder: every locale file in the
 * locales directory is loaded, and receives the keys it lacks from the default locale.
 * <p>
 * Missing keys are appended to a locale file, so comments, multi-line values and the order of the existing entries
 * stay as they are. A key that is asked for but missing is reported once.
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class Localizer implements ILocalizer {
    private static final Map<String, String> KEY_ALIASES = Map.of(
            "error.unknownPlayer", "error.unkownPlayer",
            "error.unkownPlayer", "error.unknownPlayer",
            "error.unknownWorld", "error.unkownWorld",
            "error.unkownWorld", "error.unknownWorld",
            "error.invalidBoolean", "error.invalidBoolen",
            "error.invalidBoolen", "error.invalidBoolean",
            "error.tooSmall", "error.tooLow",
            "error.tooLow", "error.tooSmall");

    private String defaultLanguage;
    private final Plugin plugin;
    private final String localesPath;
    private final String localesPrefix;
    private final Set<String> includedLocales;
    private final Pattern localePattern = Pattern.compile("_(([a-zA-Z]{2})(_[a-zA-Z]{2})?)\\.properties");
    private final Map<String, String> runtimeLocaleCodes;
    private final Map<String, ResourceBundle> languages = new HashMap<>();
    private final Function<Player, String> userLocale;
    private final List<ILocalizer> children = new ArrayList<>();
    private final Set<String> reportedMissing = ConcurrentHashMap.newKeySet();
    private boolean checked;

    /**
     * Create a new localizer instance.
     * <p>
     * This instance will create locale files, which are provided in the "resources" directory.
     * <p>
     * After this it will update all locale files inside the locales directory. For this the ref keys from the internal
     * default locale file will be used.
     * <p>
     * After an update check and a update if needed it will load the provided language or the fallback language if the
     * provided language does not exist.
     *
     * @param plugin          instance of plugin
     * @param localesPath     path of the locales directory
     * @param localesPrefix   prefix of the locale files
     * @param fallbackLocale  fallbackLocale
     * @param includedLocales internal provided locales
     */
    Localizer(
            Plugin plugin,
            String localesPath,
            String localesPrefix,
            String fallbackLocale,
            Function<Player, String> userLocale,
            Set<String> includedLocales,
            Map<String, String> runtimeLocaleCodes) {
        this.plugin = plugin;
        this.localesPath = localesPath;
        this.localesPrefix = localesPrefix;
        this.userLocale = userLocale;
        this.includedLocales = includedLocales;
        defaultLanguage = fallbackLocale;
        this.runtimeLocaleCodes = runtimeLocaleCodes;
        createDefaults();
        bootstrap();
        loadLanguage(fallbackLocale);
        if (!languages.containsKey(fallbackLocale)) {
            plugin.getLogger().log(Level.SEVERE, "Could not load default locale");
        }
        LOCALIZER.put(plugin.getClass(), this);
        for (String locale : includedLocales) {
            loadLanguage(locale);
        }
        for (String locale : localesInDirectory()) {
            if (!languages.containsKey(locale)) loadLanguage(locale);
        }
    }

    /**
     * Create a new localizer instance with default values.
     * <p>
     * The message path and prefix will be "messages" and the fallback language the "en_US" locale.
     * <p>
     * This instance will create locale files, which are provided in the "resources" directory.
     * <p>
     * After this it will update all locale files inside the locales directory. For this the ref keys from the internal
     * default locale file will be used.
     * <p>
     * After a update check and an update if needed it will load the provided language or the fallback language if the
     * provided language does not exist.
     *
     * @param plugin          instance of plugin
     * @param includedLocales internal provided locales
     * @return the created localizer instance
     * @deprecated Use the builder provided by {@link #builder(Plugin, String)}
     */
    @Deprecated(forRemoval = true)
    public static ILocalizer create(Plugin plugin, String... includedLocales) {
        return create(plugin, "messages", "messages", Locale.US, includedLocales);
    }

    /**
     * Create a new localizer instance.
     * <p>
     * This instance will create locale files, which are provided in the "resources" directory.
     * <p>
     * After this it will update all locale files inside the locales directory. For this the ref keys from the internal
     * default locale file will be used.
     * <p>
     * After a update check and an update if needed it will load the provided language or the fallback language if the
     * provided language does not exist.
     *
     * @param plugin          instance of plugin
     * @param localesPath     path of the locales directory
     * @param localesPrefix   prefix of the locale files
     * @param fallbackLocale  fallbackLocale
     * @param includedLocales internal provided locales
     * @return the created localizer instance
     * @deprecated Use the builder provided by {@link #builder(Plugin, String)}
     */
    @Deprecated(forRemoval = true)
    public static ILocalizer create(
            Plugin plugin, String localesPath, String localesPrefix, Locale fallbackLocale, String... includedLocales) {
        ILocalizer localizer = new LocalizerBuilder(plugin, fallbackLocale.toLanguageTag())
                .setLocalesPath(localesPath)
                .setLocalesPrefix(localesPrefix)
                .setUserLocale(e -> fallbackLocale.toLanguageTag())
                .setIncludedLocales(includedLocales)
                .build();
        LOCALIZER.put(plugin.getClass(), localizer);
        return localizer;
    }

    public static LocalizerBuilder builder(Plugin plugin, String defaultLocale) {
        return new LocalizerBuilder(plugin, defaultLocale);
    }

    private void createDefaults() {
        runtimeLocaleCodes.putIfAbsent("error.invalidArguments", "Invalid arguments.\nSyntax: <gold><syntax><default>");
        runtimeLocaleCodes.putIfAbsent("error.invalidCommand", "Invalid Command");
        runtimeLocaleCodes.putIfAbsent(
                "error.endOfRoute", "Please choose a subcommand. Available commands are:\n<gold><commands><default>");
        runtimeLocaleCodes.putIfAbsent(
                "error.permission", "You do not have the permission to do this. (<gold><permission><default>)");
        runtimeLocaleCodes.putIfAbsent(
                "error.invalidRange",
                "This value is out of range. Min: <gold><min><default> Max: <gold><max><default>");
        runtimeLocaleCodes.putIfAbsent(
                "error.invalidEnumValue", "Invalid input value. Valid inputs are <gold><values><default>.");
        runtimeLocaleCodes.putIfAbsent("error.invalidMaterial", "Invalid material.");
        runtimeLocaleCodes.putIfAbsent("error.invalidNumber", "Invalid number");
        runtimeLocaleCodes.putIfAbsent(
                "error.invalidBoolean", "Invalid value, <gold><true><default> or <gold><false><default>");
        runtimeLocaleCodes.putIfAbsent(
                "error.invalidLength", "This input is too long. Max: <gold><max><default> chars.");
        runtimeLocaleCodes.putIfAbsent("error.notOnline", "Invalid player. This player is not online.");
        runtimeLocaleCodes.putIfAbsent(
                "error.unknownPlayer", "Invalid player. This player has never played on this server.");
        runtimeLocaleCodes.putIfAbsent("error.unknownWorld", "Invalid world.");
        runtimeLocaleCodes.putIfAbsent("error.notAsConsole", "This command can not be executed from console.");
        runtimeLocaleCodes.putIfAbsent("error.onlyPlayer", "This command can only be used by players.");
        runtimeLocaleCodes.putIfAbsent("error.onlyConsole", "This command can only be used by console.");
        runtimeLocaleCodes.putIfAbsent("error.invalidSender", "This command can not be executed from here.");
        runtimeLocaleCodes.putIfAbsent("error.missingArgument", "Argument <INDEX> is accessed but not present.");
        runtimeLocaleCodes.putIfAbsent("error.notAsPlayer", "This command can not be executed as player");
        runtimeLocaleCodes.putIfAbsent("error.tooSmall", "The number is too small. Min: <gold><min>");
        runtimeLocaleCodes.putIfAbsent("error.tooLarge", "The number is too Large. Max: <gold><max>");
        runtimeLocaleCodes.putIfAbsent(
                "commands.about",
                "<bold><gold><plugin_name></bold><default> by <bold><authors></bold>\nVersion: <bold><version></bold>\nSpigot: <bold><website></bold>\nSupport: <bold><discord></bold>");
        runtimeLocaleCodes.putIfAbsent("dialog.accept", "accept");
        runtimeLocaleCodes.putIfAbsent("dialog.deny", "deny");
        runtimeLocaleCodes.putIfAbsent("dialog.add", "add");
        runtimeLocaleCodes.putIfAbsent("dialog.remove", "remove");
        runtimeLocaleCodes.putIfAbsent("dialog.leftClickChange", "Left click to change");
        runtimeLocaleCodes.putIfAbsent("dialog.rightClickRemove", "Right click to remove");
    }

    @Override
    public Plugin plugin() {
        return plugin;
    }

    /**
     * Change the locale to the language. If the locale is not present the fallback locale will be used.
     *
     * @param language language to be used
     * @deprecated Use {@link #setDefaultLocale(String)}
     */
    @Override
    @Deprecated(forRemoval = true)
    public void setLocale(String language) {
        bootstrap();
        setDefaultLocale(language);
    }

    @Override
    public String getMessage(String key) {
        return getMessage(key, (CommandSender) null);
    }

    private void bootstrap() {
        if (!checked) {
            createOrUpdateLocaleFiles();
            checked = true;
        }
    }

    public void setDefaultLocale(String language) {
        bootstrap();
        if (!languages.containsKey(language)) {
            plugin.getLogger().log(Level.WARNING, "Language %s does not exist".formatted(language));
            return;
        }
        this.defaultLanguage = language;
    }

    @Override
    public String getMessage(String key, @Nullable CommandSender sender) {
        if (sender instanceof Player player) {
            return getMessage(key, userLocale.apply(player));
        }
        return getMessage(key, defaultLanguage);
    }

    @Override
    public @Nullable String getValue(String key) {
        return getValue(key, (CommandSender) null);
    }

    @Override
    public String getMessage(String key, String language) {
        var result = getValue(key, language);

        if (result == null && LOCALIZATION_CODE.matcher(key).matches()) {
            if (reportedMissing.add(key)) {
                plugin.getLogger().warning("Key " + key + " is missing in fallback file.");
                plugin.getLogger().log(Level.FINE, "Missing key " + key + " requested from", new RuntimeException());
            }
            result = key;
        }

        return result;
    }

    @Override
    @Nullable
    public String getValue(String key, CommandSender sender) {
        if (sender instanceof Player player) {
            return getValue(key, userLocale.apply(player));
        }
        return getValue(key, defaultLanguage);
    }

    /**
     * Looks a key up in the locale files, the children and the built-in codes.
     * <p>
     * Keys that were renamed are still found under their old name, in both directions: a locale file that holds the
     * old key answers for the new one, and the old key gets the text of the new one.
     *
     * @param key      locale code
     * @param language language to look the key up in
     * @return the text, or null when no file, child or built-in code has the key
     */
    @Override
    @Nullable
    public String getValue(String key, String language) {
        var alias = KEY_ALIASES.get(key);
        var result = fileValue(key, language);
        if (result == null && alias != null) result = fileValue(alias, language);
        if (result == null) result = runtimeLocaleCodes.get(key);
        if (result == null && alias != null) result = runtimeLocaleCodes.get(alias);
        return result;
    }

    @Nullable
    private String fileValue(String key, String language) {
        String result = null;
        if (localeBundle(language).containsKey(key)) {
            try {
                result = localeBundle(language).getString(key);
            } catch (MissingResourceException e) {
                // ignore
            }
        }
        if (result == null && localeBundle(defaultLanguage).containsKey(key)) {
            try {
                result = localeBundle(defaultLanguage).getString(key);
            } catch (MissingResourceException e) {
                // ignore
            }
        }

        if (result == null) {
            for (var child : children) {
                result = child.getValue(key, language);
                if (result != null) break;
            }
        }
        return result;
    }

    @Override
    public ResourceBundle localeBundle(String language) {
        ResourceBundle resourceBundle = languages.get(closestLanguage(languages.keySet(), language, defaultLanguage));
        if (resourceBundle == null) {
            throw new RuntimeException("Fallback language is not registered.");
        }
        return resourceBundle;
    }

    /**
     * Finds the loaded language closest to a requested one: the same locale, ignoring case and {@code -} or {@code _}
     * ({@code en-us} finds {@code en_US}), else a locale of the same language ({@code de_AT} finds {@code de_DE}),
     * else the fallback. A player's client language rarely has a file of its own, so a missing one is no error.
     *
     * @param loaded    the loaded languages
     * @param requested the language asked for, e.g. a player's locale
     * @param fallback  the fallback language
     * @return the language to use
     */
    static String closestLanguage(Set<String> loaded, String requested, String fallback) {
        if (requested == null) return fallback;
        if (loaded.contains(requested)) return requested;
        String normalized = requested.replace('-', '_');
        String base = normalized.split("_", 2)[0];
        String sameLanguage = null;
        for (String candidate : new TreeSet<>(loaded)) {
            if (candidate.equalsIgnoreCase(normalized)) return candidate;
            if (sameLanguage == null && candidate.split("_", 2)[0].equalsIgnoreCase(base)) sameLanguage = candidate;
        }
        return sameLanguage != null ? sameLanguage : fallback;
    }

    @Override
    public ResourceBundle defaultBundle() {
        return localeBundle(defaultLanguage);
    }

    private void loadLanguage(String language) {
        try {
            languages.put(language, getBundle(getLocaleFile(language)));
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load language %s.".formatted(language), e);
        }
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
        // Create the property files if they do not exist.
        for (var locale : includedLocales) {
            var localeFile = getLocaleFile(locale).toFile();
            if (localeFile.exists()) {
                continue;
            }

            try (var resource = plugin.getResource(getLocaleFileName(locale))) {
                if (resource == null)
                    throw new FileNotFoundException("No locale file for " + locale + " found in resources.");
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
        if (hasLocaleFileName(path)) return true;
        plugin.getLogger().info(path + " is not a valid message file. Skipped.");
        return false;
    }

    private boolean hasLocaleFileName(Path path) {
        return path.toFile()
                .getName()
                .matches(Pattern.quote(localesPrefix) + "_[a-zA-Z]{2}(_[a-zA-Z]{2})?\\.properties");
    }

    private List<String> localesInDirectory() {
        try (var files = Files.list(getLocalePath())) {
            return files.filter(path -> !path.toFile().isDirectory() && hasLocaleFileName(path))
                    .map(path -> {
                        var name = path.getFileName().toString();
                        return name.substring(localesPrefix.length() + 1, name.length() - ".properties".length());
                    })
                    .toList();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not list the locale files.", e);
            return List.of();
        }
    }

    private ResourceBundle getDefaultLanguage() throws IOException {
        try (var input = plugin.getResource(localesPrefix + ".properties")) {
            if (input == null) {
                plugin.getLogger()
                        .severe("Could not load locale file " + localesPrefix + ".properties. Does it exist?");
                return new DummyResourceBundle();
            }
            // TODO: Lazy getter
            return new PropertyResourceBundle(input);
        }
    }

    private ResourceBundle getBundle(String locale) throws IOException {
        try (var input = plugin.getResource(getLocaleFileName(locale))) {
            if (input == null) {
                plugin.getLogger()
                        .fine("No bundled locale file " + getLocaleFileName(locale)
                                + ". Using the default locale as reference.");
                return getDefaultLanguage();
            }
            return new PropertyResourceBundle(input);
        }
    }

    private ResourceBundle getBundle(Locale locale) throws IOException {
        if (locale == null) {
            return getDefaultLanguage();
        }
        return getBundle(locale.toString());
    }

    private ResourceBundle getBundle(Path locale) throws IOException {
        try (var input = Files.newInputStream(locale)) {
            return new PropertyResourceBundle(input);
        }
    }

    private Properties existingEntries(Path path) throws IOException {
        var properties = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private static String escapeValue(String value) {
        var escaped = value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
        return escaped.startsWith(" ") ? "\\" + escaped : escaped;
    }

    private Set<String> getDefaultKeys() throws IOException {
        Set<String> defaultKeys =
                new HashSet<>(Collections.list(getDefaultLanguage().getKeys()));
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

            Properties existing;
            // load the external property file.
            try {
                existing = existingEntries(path);
                updateKeys.removeAll(existing.stringPropertyNames());
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not update locale " + path + ".", e);
                continue;
            }

            if (updateKeys.isEmpty()) {
                plugin.getLogger().info("Locale " + path + " is up to date.");
                continue;
            }
            plugin.getLogger().info("Updating " + path + ".");

            List<String> lines = new ArrayList<>();
            lines.add("# Keys added automatically at " + timestamp());
            for (var currKey : new TreeSet<>(updateKeys)) {
                var alias = KEY_ALIASES.get(currKey);
                String value;
                if (alias != null && existing.containsKey(alias)) {
                    value = existing.getProperty(alias);
                } else if (refBundle.containsKey(currKey)) {
                    value = refBundle.getString(currKey);
                } else {
                    value = runtimeLocaleCodes.getOrDefault(currKey, "");
                }
                lines.add(currKey + "=" + escapeValue(value));
                plugin.getLogger().info("Added: " + currKey + "=" + escapeValue(value));
            }

            try {
                var content = Files.readString(path, StandardCharsets.UTF_8);
                var separator = content.isEmpty() || content.endsWith("\n") ? "" : "\n";
                Files.writeString(
                        path,
                        separator + String.join("\n", lines) + "\n",
                        StandardCharsets.UTF_8,
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not update locale " + path + ".", e);
                continue;
            }
            plugin.getLogger().info("Updated locale " + path + ". Please check your translation.");
        }
    }

    private Locale extractLocale(Path filename) {
        var matcher = localePattern.matcher(filename.toFile().getName());
        if (matcher.find()) {
            var group = matcher.group(1);
            var part = group.split("_");
            if (part.length == 1) {
                return new Locale(part[0]);
            }
            return new Locale(part[0], part[1]);
        }
        return null;
    }

    /**
     * Translates a String with Placeholders. Can handle multiple messages with replacements.
     *
     * @param message Message to translate
     * @return Replaced Messages
     */
    @Override
    public String localize(String message) {
        if (message == null) {
            return null;
        }

        // Check if input is a locale key.
        if (isLocaleCode(message)) {
            message = getMessage(message);
        }

        return message;
    }

    @Override
    public String localize(CommandSender sender, String message) {
        if (message == null) {
            return null;
        }

        // Check if input is a locale key.
        if (isLocaleCode(message)) {
            message = getMessage(message, sender);
        }

        return message;
    }

    /**
     * Get currently registered locales.
     *
     * @return array of available locales.
     */
    @Override
    @Deprecated(forRemoval = true)
    public String[] getIncludedLocales() {
        return includedLocales.toArray(new String[0]);
    }

    public Set<String> includedLocales() {
        return Collections.unmodifiableSet(includedLocales);
    }

    /**
     * Adds locale codes with their default text.
     * <p>
     * Codes added after the localizer was created are appended to the locale files right away and the languages are
     * read again, so they are available in every language without a restart.
     *
     * @param runtimeLocaleCodes map with locales codes to add.
     */
    @Override
    public void addLocaleCodes(Map<String, String> runtimeLocaleCodes) {
        this.runtimeLocaleCodes.putAll(runtimeLocaleCodes);
        if (!checked) return;
        createOrUpdateLocaleFiles();
        for (var language : List.copyOf(languages.keySet())) {
            loadLanguage(language);
        }
    }

    @Override
    public void registerChild(ILocalizer localizer) {
        plugin.getLogger().info("Localizer from " + localizer.plugin().getName() + " registered as child.");
        children.add(localizer);
    }

    private static class DummyResourceBundle extends ResourceBundle {

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
