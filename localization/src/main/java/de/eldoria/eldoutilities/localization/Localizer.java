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
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static de.eldoria.eldoutilities.localization.ILocalizer.isLocaleCode;

/**
 * Compact localizer class.
 * <p>
 * Easy to use and fully automatic setup and updating of locales.
 * <p>
 * Requires to have at least one default locale and one fallback locale in the resources. Use the {@link #create(Plugin, String...)} constructor for initial setup. This will create missing files
 * and updates existing files.
 * <p>
 * You can change the currently used locale every time via {@link #setLocale(String)}.
 * <p>
 * The localizer also allows to use locales which are not included in the ressources folder.
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class Localizer implements ILocalizer {

    private String defaultLanguage;
    private final Plugin plugin;
    private final String localesPath;
    private final String localesPrefix;
    private final String[] includedLocales;
    private final Pattern localePattern = Pattern.compile("_(([a-zA-Z]{2})(_[a-zA-Z]{2})?)\\.properties");
    private final Map<String, String> runtimeLocaleCodes = new HashMap<>();
    private final Map<String, ResourceBundle> languages = new HashMap<>();
    private final Function<Player, String> userLocale;
    private final List<ILocalizer> childs = new ArrayList<>();
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
    Localizer(Plugin plugin, String localesPath,
              String localesPrefix, String fallbackLocale, Function<Player, String> userLocale, String... includedLocales) {
        this.plugin = plugin;
        this.localesPath = localesPath;
        this.localesPrefix = localesPrefix;
        this.userLocale = userLocale;
        this.includedLocales = includedLocales;
        defaultLanguage = fallbackLocale;
        bootstrap();
        loadLanguage(fallbackLocale);
        if (languages.containsKey(fallbackLocale)) {
            plugin.getLogger().log(Level.SEVERE, "Could not load default locale");
        }
        LOCALIZER.put(plugin.getClass(), this);
        for (String locale : includedLocales) {
            loadLanguage(locale);
        }
        createDefaults();
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
    public static ILocalizer create(Plugin plugin,
                                    String... includedLocales) {
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
    public static ILocalizer create(Plugin plugin, String localesPath,
                                    String localesPrefix, Locale fallbackLocale, String... includedLocales) {
        ILocalizer localizer = new LocalizerBuilder(plugin, fallbackLocale.toLanguageTag()).setLocalesPath(localesPath).setLocalesPrefix(localesPrefix).setUserLocale(e -> fallbackLocale.toLanguageTag()).setIncludedLocales(includedLocales).build();
        LOCALIZER.put(plugin.getClass(), localizer);
        return localizer;
    }

    public static LocalizerBuilder builder(Plugin plugin, String defaultLocale) {
        return new LocalizerBuilder(plugin, defaultLocale);
    }

    private void createDefaults() {
        runtimeLocaleCodes.put("error.invalidArguments", "Invalid arguments.\nSyntax: <gold><syntax><default>");
        runtimeLocaleCodes.put("error.invalidCommand", "Invalid Command");
        runtimeLocaleCodes.put("error.endOfRoute", "Please choose a subcommand. Available commands are:\n<gold><commands><default>");
        runtimeLocaleCodes.put("error.permission", "You do not have the permission to do this. (<gold><permission><default>)");
        runtimeLocaleCodes.put("error.invalidRange", "This value is out of range. Min: <gold><min><default> Max: <gold><max><default>");
        runtimeLocaleCodes.put("error.invalidEnumValue", "Invalid input value. Valid inputs are <gold><values><default>.");
        runtimeLocaleCodes.put("error.invalidMaterial", "Invalid material.");
        runtimeLocaleCodes.put("error.invalidNumber", "Invalid number");
        runtimeLocaleCodes.put("error.invalidBoolean", "Invalid value, <gold><true><default> or <gold><false><default>");
        runtimeLocaleCodes.put("error.invalidLength", "This input is too long. Max: <gold><max><default> chars.");
        runtimeLocaleCodes.put("error.notOnline", "Invalid player. This player is not online.");
        runtimeLocaleCodes.put("error.unknownPlayer", "Invalid player. This player has never played on this server.");
        runtimeLocaleCodes.put("error.unknownWorld", "Invalid world.");
        runtimeLocaleCodes.put("error.notAsConsole", "This command can not be executed from console.");
        runtimeLocaleCodes.put("error.onlyPlayer", "This command can only be used by players.");
        runtimeLocaleCodes.put("error.onlyConsole", "This command can only be used by console.");
        runtimeLocaleCodes.put("error.invalidSender", "This command can not be executed from here.");
        runtimeLocaleCodes.put("error.missingArgument", "Argument <INDEX> is accessed but not present.");
        runtimeLocaleCodes.put("error.notAsPlayer", "This command can not be executed as player");
        runtimeLocaleCodes.put("error.tooSmall", "The number is too small. Min: <gold><min>");
        runtimeLocaleCodes.put("error.tooLarge", "The number is too Large. Max: <gold><max>");
        runtimeLocaleCodes.put("commands.about", "<bold><gold><plugin_name></bold><default> by <bold><authors></bold>\nVersion: <bold><version></bold>\nSpigot: <bold><website></bold>\nSupport: <bold><discord></bold>");
        runtimeLocaleCodes.put("dialog.accept", "accept");
        runtimeLocaleCodes.put("dialog.deny", "deny");
        runtimeLocaleCodes.put("dialog.add", "add");
        runtimeLocaleCodes.put("dialog.remove", "remove");
        runtimeLocaleCodes.put("dialog.leftClickChange", "Left click to change");
        runtimeLocaleCodes.put("dialog.rightClickRemove", "Right click to remove");
    }

    /**
     * Change the locale to the language. If the locale is not present the fallback locale will be used.
     *
     * @param language language to be used
     * @deprecated Use
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
        var result = getValue(key);

        if (result == null && LOCALIZATION_CODE.matcher(key).matches()) {
            plugin.getLogger().warning("Key " + key + " is missing in fallback file.");
            plugin.getLogger().log(Level.WARNING, "Message from", new RuntimeException());
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

    @Override
    @Nullable
    public String getValue(String key, String language) {
        String result = null;
        if (localeBundle(language).containsKey(key)) {
            try {
                result = localeBundle(key).getString(key);
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
            for (var child : childs) {
                result = child.getValue(key, language);
                if (result != null) break;
            }
        }
        return result;
    }

    @Override
    public ResourceBundle localeBundle(String language) {
        ResourceBundle resourceBundle = languages.get(language);
        if (resourceBundle == null) {
            plugin.getLogger().severe("Language %s not found".formatted(language));
            if (language.equals(defaultLanguage)) {
                throw new RuntimeException("Fallback language is not registered.");
            }
            return localeBundle(defaultLanguage);
        }
        return resourceBundle;
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

    private ResourceBundle getDefaultLanguage() throws IOException {
        try (var input = plugin.getResource(localesPrefix + ".properties")) {
            if (input == null) {
                plugin.getLogger().severe("Could not load locale file " + localesPrefix + ".properties. Does it exist?");
                return new DummyResourceBundle();
            }
            //TODO: Lazy getter
            return new PropertyResourceBundle(input);
        }
    }

    private ResourceBundle getBundle(String locale) throws IOException {
        try (var input = plugin.getResource(getLocaleFileName(locale))) {
            if (input == null) {
                plugin.getLogger().severe("Could not load locale file " + getLocaleFileName(locale) + ".properties. Does it exist?");
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

    private Map<String, String> bundleMap(Path path) throws IOException {
        var map = new TreeMap<String, String>(String::compareToIgnoreCase);
        Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                .map(line -> line.split("=", 2)).filter(line -> line.length == 2)
                .forEach(line -> map.put(line[0], line[1]));
        return map;
    }

    private Set<String> getDefaultKeys() throws IOException {
        Set<String> defaultKeys = new HashSet<>(Collections.list(getDefaultLanguage().getKeys()));
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
                plugin.getLogger().info("Locale " + path + " is up to date.");
                continue;
            }
            plugin.getLogger().info("Updating " + path + ".");

            // check if ref key is in locale
            for (var currKey : updateKeys) {
                var value = refBundle.containsKey(currKey) ? refBundle.getString(currKey) : runtimeLocaleCodes.getOrDefault(currKey, "");
                // Add the property with the value if it exists in an internal file.
                bundleMap.put(currKey, value);
                plugin.getLogger().info("Added: " + currKey + "=" + value.replace("\n", "\\n"));
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
