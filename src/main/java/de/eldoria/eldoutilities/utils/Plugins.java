package de.eldoria.eldoutilities.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.logging.Level;

public final class Plugins {
    private Plugins() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static Optional<File> getPluginFile(Plugin plugin) {
        // Get File of plugin
        Field fileField;
        try {
            fileField = JavaPlugin.class.getDeclaredField("file");
        } catch (NoSuchFieldException e) {
            plugin.getLogger().log(Level.WARNING, "§cCould not find field file in plugin.", e);
            return Optional.empty();
        }
        fileField.setAccessible(true);
        try {
            return Optional.of((File) fileField.get(plugin));
        } catch (IllegalAccessException e) {
            plugin.getLogger().log(Level.WARNING, "Could not retrieve file of plugin.", e);
            return Optional.empty();
        }
    }
}
