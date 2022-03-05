/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.localization;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final ResourceBundle fallbackLocaleFile;
    private final Plugin plugin;
    private final String localesPath;
    private final String localesPrefix;
    private final String[] includedLocales;
    private final Pattern localePattern = Pattern.compile("_(([a-zA-Z]{2})(_[a-zA-Z]{2})?)\\.properties");
    private final Map<String, String> runtimeLocaleCodes = new HashMap<>();
    private ResourceBundle localeFile;
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
        fallbackLocaleFile = ResourceBundle.getBundle(localesPrefix, fallbackLocale, plugin.getClass().getClassLoader());
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

        var localeFile = localesPrefix + "_" + language + ".properties";

        try (var stream = Files.newInputStream(Paths.get(plugin.getDataFolder().toString(), localesPath, localeFile))) {
            this.localeFile = new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load locale file " + Paths.get(localesPath, localeFile), e);
            this.localeFile = fallbackLocaleFile;
        }
    }

    /**
     * Translates a String with Placeholders. Can handle multiple messages with replacements. Add replacements in the
     * right order.
     *
     * @param key          Key of message
     * @param replacements Replacements in the right order.
     * @return Replaced Messages
     */
    @Override
    public String getMessage(String key, Replacement... replacements) {
        String result = null;
        if (localeFile.containsKey(key)) {
            result = localeFile.getString(key);
            if (result.isEmpty()) {
                if (fallbackLocaleFile.containsKey(key)) {
                    result = fallbackLocaleFile.getString(key);
                }
            }
        } else if (fallbackLocaleFile.containsKey(key)) {
            result = fallbackLocaleFile.getString(key);
        }

        if (result == null) {
            plugin.getLogger().warning("Key " + key + " is missing in fallback file.");
            result = key;
        }

        return invokeReplacements(result, replacements);
    }

    private void createOrUpdateLocaleFiles() {
        var messages = Paths.get(plugin.getDataFolder().toString(), localesPath);

        try {
            Files.createDirectories(messages);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create message directory.", e);
        }

        // Create the property files if they do not exists.
        for (var includedLocale : includedLocales) {
            var filename = localesPrefix + "_" + includedLocale + ".properties";

            var localeFile = Paths.get(messages.toString(), filename).toFile();
            if (localeFile.exists()) {
                continue;
            }

            var builder = new StringBuilder();
            try (var bufferedReader = new BufferedReader(new InputStreamReader(
                    plugin.getResource(filename), StandardCharsets.UTF_8))) {
                var line = bufferedReader.readLine();
                while (line != null) {
                    builder.append(line).append("\n");
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not load resource " + filename + ".", e);
            } catch (NullPointerException e) {
                plugin.getLogger().log(Level.WARNING, "Locale " + includedLocale + " could not be loaded but should exists.", e);
                continue;
            }

            try (var outputStream = new OutputStreamWriter(new FileOutputStream(localeFile), StandardCharsets.UTF_8)) {
                outputStream.write(builder.toString());

            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to create default message file " + localeFile.getName() + ".", e);
                continue;
            }
            plugin.getLogger().info("Created default locale " + filename);
        }

        List<File> localeFiles = new ArrayList<>();

        // Load all property files
        try (var message = Files.list(messages)) {
            for (var path : message.collect(Collectors.toList())) {
                // skip directories. why should they be there anyway?
                if (path.toFile().isDirectory()) {
                    continue;
                }

                // Lets be a bit nice with the formatting. not everyone knows the ISO.
                if (path.toFile().getName().matches(localesPrefix + "_[a-zA-Z]{2}(_[a-zA-Z]{2})?\\.properties")) {
                    localeFiles.add(path.toFile());
                } else {
                    // Notify the user that he did something weird in his messages directory.
                    plugin.getLogger().info(path + " is not a valid message file. Skipped.");
                }
            }
        } catch (IOException e) {
            // we will try to continue with the successfull loaded files. If there are none thats not bad.
            plugin.getLogger().log(Level.WARNING, "Failed to load message files.");
        }

        // get the default pack to have a set of all needed keys. Hopefully its correct.
        ResourceBundle defaultBundle = null;
        try {
            defaultBundle = new PropertyResourceBundle(new InputStreamReader(plugin.getResource(localesPrefix + ".properties"), StandardCharsets.UTF_8));
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load reference file... This is really bad!", e);
        }

        if (defaultBundle == null) {
            // How should we update without a reference?
            plugin.getLogger().warning("No reference locale found. Please report this to the application owner");
            return;
        }

        Set<String> defaultKeys = new HashSet<>(Collections.list(defaultBundle.getKeys()));
        defaultKeys.addAll(runtimeLocaleCodes.keySet());

        // Update keys of existing files.
        for (var file : localeFiles) {

            // try to search for a included updated version.
            var currLocale = extractLocale(file.getName());
            @Nullable ResourceBundle refBundle = null;

            if (currLocale != null) {
                try {
                    refBundle = new PropertyResourceBundle(new InputStreamReader(
                            plugin.getResource(localesPrefix + "_" + currLocale + ".properties"), StandardCharsets.UTF_8));
                } catch (IOException | NullPointerException e) {
                    plugin.getLogger().info("§eNo reference locale found for " + currLocale + ". Using default locale.");
                }
                if (refBundle == null) {
                    refBundle = defaultBundle;
                } else {
                    plugin.getLogger().info("§2Found matching locale for " + currLocale);
                }
            } else {
                plugin.getLogger().warning("Could not determine locale code of file " + file.getName());
                refBundle = defaultBundle;
            }

            // TODO: Preserve commands for properties.
            // load the external property file.
            Map<String, String> treemap = new TreeMap<>(String::compareToIgnoreCase);
            try (var bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                var line = bufferedReader.readLine();
                while (line != null) {
                    var split = line.split("=", 2);
                    if (split.length == 2) {
                        treemap.put(split[0], split[1]);
                    }
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not update locale " + file.getName() + ".", e);
                continue;
            }

            var keys = treemap.keySet();

            var updated = false;
            // check if ref key is in locale
            for (var currKey : defaultKeys) {
                if (keys.contains(currKey)) continue;
                var value = "";
                if (refBundle != null) {
                    value = refBundle.containsKey(currKey) ? refBundle.getString(currKey) : runtimeLocaleCodes.getOrDefault(currKey, "");
                }
                // Add the property with the value if it exists in a internal file.
                treemap.put(currKey, value);
                if (!updated) {
                    plugin.getLogger().info("§2Updating " + file.getName() + ".");

                }
                plugin.getLogger().info("§2Added: §3" + currKey + "§6=§b" + value.replace("\n", "\\n"));
                updated = true;

            }

            // Write to file if updated.
            if (updated) {
                try (var outputStream = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                    outputStream.write("# File automatically updated at "
                                       + DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()) + "\n");
                    for (var entry : treemap.entrySet()) {
                        outputStream.write(entry.getKey() + "=" + entry.getValue().replace("\n", "\\n") + "\n");
                    }
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Could not update locale " + file.getName() + ".", e);
                    continue;
                }
                plugin.getLogger().info("§2Updated locale " + file.getName() + ". Please check your translation.");
            } else {
                plugin.getLogger().info("§2Locale " + file.getName() + " is up to date.");
            }
        }
    }

    private Locale extractLocale(String filename) {
        var matcher = localePattern.matcher(filename);
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
}
