/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.payload;

import de.eldoria.eldoutilities.debug.DebugSettings;
import de.eldoria.eldoutilities.debug.data.LogData;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogMeta extends LogData {
    public static final int MAX_LOG_PART_SIZE = 2500;
    public static final int MAX_LOG_MB = 50;
    private static final int SCAN_OVERLAP = 250;
    private static final Pattern EXCEPTION = Pattern.compile(
            "[0-9]{2}:[0-9]{2}:[0-9]{2}] (\\[[^\\]]*?(?:ERROR|WARN)]:.*?)^\\[",
            Pattern.DOTALL + Pattern.MULTILINE);
    private static final String PLUGIN_LOG = "([0-9]{2}:[0-9]{2}:[0-9]{2}] \\[[^\\]]*?]: \\[[^\\]]*?name].*?)^\\[";

    public LogMeta(String log, String pluginLog, String[] internalExceptions, String[] exceptions) {
        super(log, pluginLog, internalExceptions, exceptions);
    }


    private static Pattern getPluginLog(Plugin plugin) {
        var prefix = plugin.getDescription().getPrefix();
        var name = prefix != null ? prefix : plugin.getDescription().getName();
        return Pattern.compile(PLUGIN_LOG.replace("name", Pattern.quote(name)), Pattern.DOTALL + Pattern.MULTILINE);
    }

    /**
     * Reads the server's {@code logs/latest.log}.
     * <p>
     * A log of up to {@value #MAX_LOG_PART_SIZE} times two lines is taken whole; a longer one is cut to its first and
     * last {@value #MAX_LOG_PART_SIZE} lines. Exceptions and the plugin's own lines are collected from all of it. A log
     * over {@value #MAX_LOG_MB} MB is never held in memory at once: it is scanned in parts as it is read.
     *
     * @param plugin   the plugin the report is about
     * @param settings the filters applied to everything taken from the log
     * @return the log, filtered
     */
    public static LogData create(Plugin plugin, DebugSettings settings) {
        var root = plugin.getDataFolder().toPath().toAbsolutePath().getParent().getParent();
        return fromFile(root.resolve(Paths.get("logs", "latest.log")), plugin, settings);
    }

    static LogData fromFile(Path logPath, Plugin plugin, DebugSettings settings) {
        var latestLog = "Could not read latest log.";
        Set<String> pluginLog = new LinkedHashSet<>();
        var exceptionPair = new ExceptionPair();

        if (Files.isRegularFile(logPath)) {
            try {
                latestLog = Files.size(logPath) / (1024 * 1024) > MAX_LOG_MB
                        ? readLarge(logPath, plugin, pluginLog, exceptionPair)
                        : readWhole(logPath, plugin, pluginLog, exceptionPair);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not read the log.", e);
            }
        }
        latestLog = settings.applyFilter(latestLog);
        var pluginlogs = settings.applyFilter(String.join("", pluginLog));
        exceptionPair.applyFilter(settings);

        return new LogMeta(latestLog, pluginlogs, exceptionPair.getInternalArray(), exceptionPair.getExternalArray());
    }

    private static String readWhole(Path logPath, Plugin plugin, Set<String> pluginLog, ExceptionPair exceptions)
            throws IOException {
        List<String> lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
        exceptions.combine(extractExceptions(lines, plugin));
        pluginLog.addAll(extractPluginLog(lines, plugin));
        if (lines.size() <= MAX_LOG_PART_SIZE * 2) return String.join("\n", lines);
        return cut(lines.subList(0, MAX_LOG_PART_SIZE), lines.subList(lines.size() - MAX_LOG_PART_SIZE, lines.size()));
    }

    private static String readLarge(Path logPath, Plugin plugin, Set<String> pluginLog, ExceptionPair exceptions)
            throws IOException {
        List<String> start = new ArrayList<>();
        var end = new FixedStack<String>(MAX_LOG_PART_SIZE);
        var linesReadSinceScan = 0;
        try (var reader = Files.newBufferedReader(logPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (start.size() < MAX_LOG_PART_SIZE) {
                    start.add(line);
                    if (start.size() == MAX_LOG_PART_SIZE) {
                        exceptions.combine(extractExceptions(start, plugin));
                        pluginLog.addAll(extractPluginLog(start, plugin));
                    }
                    continue;
                }
                end.add(line);
                linesReadSinceScan++;
                if (linesReadSinceScan == MAX_LOG_PART_SIZE) {
                    exceptions.combine(extractExceptions(end.getLinkedList(), plugin));
                    pluginLog.addAll(extractPluginLog(end.getLinkedList(), plugin));
                    linesReadSinceScan = SCAN_OVERLAP;
                }
            }
        }
        exceptions.combine(extractExceptions(end.getLinkedList(), plugin));
        pluginLog.addAll(extractPluginLog(end.getLinkedList(), plugin));
        return cut(start, end.getLinkedList());
    }

    private static String cut(List<String> start, List<String> end) {
        return String.join("\n", start) + "\n\n[...]\n\n" + String.join("\n", end);
    }

    private static Set<String> extractPluginLog(Collection<String> log, Plugin plugin) {
        return extractPluginLog(String.join("\n", log), plugin);
    }

    private static Set<String> extractPluginLog(String log, Plugin plugin) {
        Set<String> pluginLog = new LinkedHashSet<>();
        var pluginLogPattern = getPluginLog(plugin);
        var matcher = pluginLogPattern.matcher(log);
        while (matcher.find()) {
            var match = matcher.group(1);
            pluginLog.add("[" + match);
        }
        return pluginLog;
    }

    private static ExceptionPair extractExceptions(String log, Plugin plugin) {
        var packages = plugin.getDescription().getMain().split("\\.");
        var project = String.join(".", Arrays.copyOfRange(packages, 0, Math.min(packages.length, 3)));

        Set<String> external = new LinkedHashSet<>();
        Set<String> internal = new LinkedHashSet<>();
        var matcher = EXCEPTION.matcher(log);
        while (matcher.find()) {
            var match = matcher.group(1);
            if (match.contains(project)) {
                internal.add(match);
            } else {
                external.add(match);
            }
        }
        return new ExceptionPair(external, internal);
    }

    private static ExceptionPair extractExceptions(List<String> lines, Plugin plugin) {
        return extractExceptions(String.join("\n", lines), plugin);
    }

    private static class ExceptionPair {
        private Set<String> external;
        private Set<String> internal;

        public ExceptionPair() {
            external = new LinkedHashSet<>();
            internal = new LinkedHashSet<>();
        }

        public ExceptionPair(Set<String> external, Set<String> internal) {
            this.external = new LinkedHashSet<>(external);
            this.internal = new LinkedHashSet<>(internal);
        }

        public Set<String> getExternal() {
            return external;
        }

        public Set<String> getInternal() {
            return internal;
        }

        public void combine(ExceptionPair pair) {
            external.addAll(pair.external);
            internal.addAll(pair.internal);
        }

        public String[] getExternalArray() {
            return external.toArray(new String[0]);
        }

        public String[] getInternalArray() {
            return internal.toArray(new String[0]);
        }

        public void applyFilter(DebugSettings settings) {
            external = external.stream().map(settings::applyFilter).collect(Collectors.toCollection(LinkedHashSet::new));
            internal = internal.stream().map(settings::applyFilter).collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    static class FixedStack<E> {
        private final int size;
        private final LinkedList<E> linkedList = new LinkedList<>();

        public FixedStack(int size) {
            this.size = size;
        }

        public boolean add(E e) {
            if (linkedList.size() >= size) {
                linkedList.removeFirst();
            }
            return linkedList.add(e);
        }

        public Iterator<E> iterator() {
            return linkedList.iterator();
        }

        public void clear() {
            linkedList.clear();
        }

        public ListIterator<E> listIterator() {
            return linkedList.listIterator();
        }

        public ListIterator<E> listIterator(int index) {
            return linkedList.listIterator(index);
        }

        public LinkedList<E> getLinkedList() {
            return linkedList;
        }
    }
}
