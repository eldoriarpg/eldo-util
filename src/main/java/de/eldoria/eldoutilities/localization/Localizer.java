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
    private static final Pattern EMBED_LOCALIZATION_CODE = Pattern.compile("\\$([a-zA-Z.]+?)\\$");
    private static final Pattern LOCALIZATION_CODE = Pattern.compile("([a-zA-Z.]+?)");

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
        Map<String, String> locales = new HashMap<String, String>() {
            {
                put("error.invalidArguments", "Invalid arguments.\nSyntax: %SYNTAX%");
                put("error.invalidCommand", "Invalid Command");
                put("error.permission", "You do not have the permissionNode to do this. (%PERMISSION%)");
                put("error.invalidRange", "This value is out of range. Min: %MIN% Max: %MAX%");
                put("error.invalidEnumValue", "Invalid input value. Valid inputs are %VALUES%.");
                put("error.invalidNumber", "Invalid number");
                put("error.invalidLength", "This input is too long. Max: %MAX% chars.");
                put("error.notAsConsole", "This command can not be executed from console.");
                put("error.notAsPlayer", "This command can not be executed as player");
                put("about", "%PLUGIN_NAME% by %AUTHORS%\nVersion: %VERSION%\nSpigot: %WEBSITE%\nSupport: %DISCORD%");
                put("dialog.accept", "accept");
                put("dialog.deny", "deny");
                put("dialog.add", "add");
                put("dialog.remove", "remove");
                put("dialog.leftClickChange", "Left click to change");
                put("dialog.rightClickRemove", "Right click to remove");
            }
        };
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

        String localeFile = localesPrefix + "_" + language + ".properties";

        try (InputStream stream = Files.newInputStream(Paths.get(plugin.getDataFolder().toString(), localesPath, localeFile))) {
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
            return "";
        }

        for (Replacement replacement : replacements) {
            result = replacement.invoke(result);
        }

        return result;
    }

    private void createOrUpdateLocaleFiles() {
        Path messages = Paths.get(plugin.getDataFolder().toString(), localesPath);

        // Make sure that the messages directory exists.
        if (!messages.toFile().exists()) {
            boolean mkdir = messages.toFile().mkdir();
            if (!mkdir) {
                plugin.getLogger().log(Level.WARNING, "Failed to create locale directory.");
                return;
            }
        }


        // Create the property files if they do not exists.
        for (String includedLocale : includedLocales) {
            String filename = localesPrefix + "_" + includedLocale + ".properties";

            File localeFile = Paths.get(messages.toString(), filename).toFile();
            if (localeFile.exists()) {
                continue;
            }

            StringBuilder builder = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    plugin.getResource(filename), StandardCharsets.UTF_8))) {
                String line = bufferedReader.readLine();
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

            try (OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(localeFile), StandardCharsets.UTF_8)) {
                outputStream.write(builder.toString());

            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to create default message file " + localeFile.getName() + ".", e);
                continue;
            }
            plugin.getLogger().info("Created default locale " + filename);
        }

        List<File> localeFiles = new ArrayList<>();

        // Load all property files
        try (Stream<Path> message = Files.list(messages)) {
            for (Path path : message.collect(Collectors.toList())) {
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
        for (File file : localeFiles) {

            // try to search for a included updated version.
            Locale currLocale = extractLocale(file.getName());
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
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line = bufferedReader.readLine();
                while (line != null) {
                    String[] split = line.split("=", 2);
                    if (split.length == 2) {
                        treemap.put(split[0], split[1]);
                    }
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not update locale " + file.getName() + ".", e);
                continue;
            }

            Set<String> keys = treemap.keySet();

            boolean updated = false;
            // check if ref key is in locale
            for (String currKey : defaultKeys) {
                if (keys.contains(currKey)) continue;
                String value = "";
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
                try (OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                    outputStream.write("# File automatically updated at "
                            + DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()) + "\n");
                    for (Map.Entry<String, String> entry : treemap.entrySet()) {
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
        Matcher matcher = localePattern.matcher(filename);
        if (matcher.find()) {
            String group = matcher.group(1);
            String[] s = group.split("_");
            if (s.length == 1) {
                return new Locale(s[0]);
            }
            return new Locale(s[0], s[1]);
        }
        return null;
    }

    /**
     * Translates a String with Placeholders. Can handle multiple messages with replacements. Add replacements in the
     * right order.
     *
     * @param message      Message to translate
     * @param replacements Replacements in the right order.
     * @return Replaced Messages
     */
    @Override
    public String localize(String message, Replacement... replacements) {
        if (message == null) {
            return null;
        }

        // If the matcher doesn't find any key we assume its a simple message.
        if (LOCALIZATION_CODE.matcher(message).matches()) {
            return getMessage(message, replacements);
        }

        // find locale codes in message
        Matcher matcher = EMBED_LOCALIZATION_CODE.matcher(message);
        List<String> keys = new ArrayList<>();
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }


        String result = message;
        for (String match : keys) {
            //Replace current locale code with result
            result = result.replace("$" + match + "$", getMessage(match, replacements));
        }
        return result;
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
