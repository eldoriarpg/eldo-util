/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * The configuration files a debug report carries.
 * <p>
 * A file larger than {@value #MAX_FILE_BYTES} bytes is left out, and files are taken in order until
 * {@value #MAX_TOTAL_BYTES} bytes are reached, so a plugin keeping large data next to its configuration does not
 * fill the report with it.
 */
public final class ConfigFiles {
    public static final long MAX_FILE_BYTES = 1024 * 1024;
    public static final long MAX_TOTAL_BYTES = 5 * 1024 * 1024;
    private static final Set<String> EXTENSIONS = Set.of("yml", "yaml", "json", "properties", "toml");
    private static final int DEPTH = 4;

    private ConfigFiles() {}

    /**
     * Every {@code .yml}, {@code .yaml}, {@code .json}, {@code .properties} and {@code .toml} file in the plugin's
     * folder and up to {@value #DEPTH} folders below it, sorted by path.
     */
    public static List<Path> inDataFolder(Plugin plugin) {
        Path folder = plugin.getDataFolder().toPath();
        if (!Files.isDirectory(folder)) return List.of();
        try (Stream<Path> files = Files.walk(folder, DEPTH)) {
            return files.filter(Files::isRegularFile)
                    .filter(ConfigFiles::isConfig)
                    .sorted()
                    .toList();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not list the configuration files.", e);
            return List.of();
        }
    }

    /**
     * Reads the chosen files within the size limits.
     *
     * @return each file's path relative to the plugin's folder (or as given, when outside it) with its content
     */
    static List<Read> read(Plugin plugin, List<Path> chosen) {
        Path folder = plugin.getDataFolder().toPath().toAbsolutePath();
        List<Read> read = new ArrayList<>();
        long total = 0;
        for (Path path : chosen) {
            try {
                long size = Files.size(path);
                if (size > MAX_FILE_BYTES || total + size > MAX_TOTAL_BYTES) continue;
                read.add(new Read(name(folder, path), Files.readString(path, StandardCharsets.UTF_8)));
                total += size;
            } catch (IOException e) {
                plugin.getLogger().log(Level.FINE, "Could not read " + path + " for a debug report.", e);
            }
        }
        return read;
    }

    private static String name(Path folder, Path path) {
        Path absolute = path.toAbsolutePath();
        Path name = absolute.startsWith(folder) ? folder.relativize(absolute) : path;
        return name.toString().replace('\\', '/');
    }

    private static boolean isConfig(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 && EXTENSIONS.contains(name.substring(dot + 1).toLowerCase(Locale.ROOT));
    }

    /**
     * @param name where the file is, relative to the plugin's folder
     */
    record Read(String name, String content) {}
}
