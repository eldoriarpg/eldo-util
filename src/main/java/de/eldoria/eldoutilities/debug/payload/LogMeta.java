package de.eldoria.eldoutilities.debug.payload;

import de.eldoria.eldoutilities.debug.DebugSettings;
import de.eldoria.eldoutilities.debug.data.LogData;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogMeta extends LogData {
    // [time] [channel] [plugin]
    //"^(\\[[0-9]{2}:[0-9]{2}:[0-9]{2}] \\[[^\\]]*?]: \\[[^\\]]*?{{name}}].*?)^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}]";
    public static final int MAX_LOG_PART_SIZE = 2500;
    public static final int MAX_LOG_MB = 50;
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
        return Pattern.compile(PLUGIN_LOG.replace("name", name), Pattern.DOTALL + Pattern.MULTILINE);
    }

    /**
     * Gets the latest log from the logs directory.
     *
     * @param plugin   plugin for pure lazyness and logging purposes
     * @param settings settings for debug dispatching
     * @return Log as string.
     */
    public static LogData create(Plugin plugin, DebugSettings settings) {
        var root = plugin.getDataFolder().toPath().toAbsolutePath().getParent().getParent();
        var logPath = Paths.get(root.toString(), "logs", "latest.log");
        var logFile = logPath.toFile();

        var fullLog = "";
        var latestLog = "Could not read latest log.";
        Set<String> pluginLog = new LinkedHashSet<>();

        var exceptionPair = new ExceptionPair();

        if (logFile.exists()) {
            if (logFile.length() / (1024 * 1024) > MAX_LOG_MB) {
                List<String> start = new LinkedList<>();
                var end = new FixedStack<String>(MAX_LOG_PART_SIZE);
                // The log seems to be large we will read it partially.
                var linesReadSinceScan = 0;
                try (InputStream stream = new FileInputStream(logFile); var reader = new Scanner(stream)) {
                    while (reader.hasNext()) {
                        // first we grab the start
                        if (start.size() < MAX_LOG_PART_SIZE) {
                            start.add(reader.nextLine());
                            if (start.size() == MAX_LOG_PART_SIZE) {
                                pluginLog.addAll(extractPluginLog(start, plugin));
                            }
                            continue;
                        }
                        // Now we build chunks and use a fixed stack where we push the oldest size exceeding entry out.
                        end.add(reader.nextLine());
                        linesReadSinceScan++;
                        if (linesReadSinceScan == MAX_LOG_PART_SIZE) {
                            // When we reached the max log size which will be send we want to scan this part first.
                            exceptionPair.combine(extractExceptions(end.getLinkedList(), plugin));
                            pluginLog.addAll(extractPluginLog(end.getLinkedList(), plugin));
                            // we want a slight overlap
                            linesReadSinceScan = 250;
                        }
                    }
                    exceptionPair.combine(extractExceptions(end.getLinkedList(), plugin));
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Could not read log.", e);
                }
                var startLog = String.join("\n", start);
                var endLog = String.join("\n", end.linkedList);
                latestLog = startLog + "\n\n[...]\n\n" + endLog;
            } else {
                try (var reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8))) {
                    var logLines = reader.lines().collect(Collectors.toList());
                    if (logLines.size() <= MAX_LOG_PART_SIZE * 2) {
                        // We have a small log. We will send it in one part.
                        latestLog = logLines.stream()
                                .collect(Collectors.joining(System.lineSeparator()));
                    } else {
                        // We have a small log, we will only send start and end
                        var start = String.join("\n", logLines.subList(0, MAX_LOG_PART_SIZE));
                        var end = String.join("\n", logLines.subList(logLines.size() - MAX_LOG_PART_SIZE, logLines.size()));
                        latestLog = start + "\n\n[...]\n\n" + end;
                    }
                    exceptionPair.combine(extractExceptions(logLines, plugin));
                    pluginLog.addAll(extractPluginLog(logLines, plugin));
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Could not read log file", e);
                }
            }
        }
        latestLog = settings.applyFilter(latestLog);
        var pluginlogs = settings.applyFilter(String.join("", pluginLog));
        exceptionPair.applyFilter(settings);

        return new LogMeta(latestLog, pluginlogs, exceptionPair.getInternalArray(), exceptionPair.getExternalArray());
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
            external = external.stream().map(settings::applyFilter).collect(Collectors.toSet());
            internal = internal.stream().map(settings::applyFilter).collect(Collectors.toSet());
        }
    }

    private static class FixedStack<E> {
        private final int size;
        private final LinkedList<E> linkedList = new LinkedList<>();

        public FixedStack(int size) {
            this.size = size;
        }

        public boolean add(E e) {
            if (linkedList.size() > size) {
                linkedList.removeLast();
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
