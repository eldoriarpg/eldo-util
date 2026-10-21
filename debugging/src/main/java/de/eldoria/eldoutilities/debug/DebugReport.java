/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import de.eldoria.eldoutilities.debug.data.ChartData;
import de.eldoria.eldoutilities.debug.data.EntryData;
import de.eldoria.eldoutilities.debug.data.ExceptionData;
import de.eldoria.eldoutilities.debug.data.LogData;
import de.eldoria.eldoutilities.debug.data.PluginMetaData;
import de.eldoria.eldoutilities.debug.data.ServerMetaData;
import de.eldoria.eldoutilities.debug.payload.LogMeta;
import de.eldoria.eldoutilities.debug.payload.PluginMeta;
import de.eldoria.eldoutilities.debug.payload.ServerMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A debug report as Lyna takes it, in its second format: the plugin and the server, then the sections in the order
 * they are shown: health checks, what the plugin adds about itself, the log, the exceptions with their counts, and
 * the configuration files.
 * <p>
 * It is built in two steps. {@link #collect(Plugin)} asks the server about itself and the plugin's
 * {@link DebugDataProvider}s, including their health checks, which belongs on the thread that owns that state; {@link #create(Snapshot, DebugSettings)}
 * then reads the log and the configuration files and filters everything, which may take a while and runs anywhere.
 */
public final class DebugReport {
    private final int v = 2;
    private final PluginMetaData plugin;
    private final ServerMetaData server;
    private final List<Section> sections;

    private DebugReport(PluginMetaData plugin, ServerMetaData server, List<Section> sections) {
        this.plugin = plugin;
        this.server = server;
        this.sections = sections;
    }

    /**
     * What is asked of the server and the plugin itself.
     *
     * @param additional what the plugin's {@link DebugDataProvider}s add
     */
    public record Snapshot(Plugin plugin, PluginMetaData pluginMeta, ServerMetaData serverMeta,
                           EntryData[] additional, List<HealthCheck> checks, List<ChartData> charts) {
    }

    /**
     * Asks the server and the plugin about themselves.
     *
     * @param plugin the plugin the report is about
     */
    public static Snapshot collect(Plugin plugin) {
        return new Snapshot(plugin, PluginMeta.create(plugin), ServerMeta.create(),
                DebugUtil.getAdditionalPluginMeta(plugin), DebugUtil.getHealthChecks(plugin), DebugUtil.getCharts(plugin));
    }

    /**
     * Completes a report with the log and the configuration files, every section filtered.
     */
    public static DebugReport create(Snapshot snapshot, DebugSettings settings) {
        LogData log = LogMeta.create(snapshot.plugin(), settings);
        List<ConfigFiles.Read> configs = ConfigFiles.read(snapshot.plugin(), settings.configFiles(snapshot.plugin()));
        return new DebugReport(snapshot.pluginMeta(), snapshot.serverMeta(),
                sections(snapshot.checks(), snapshot.additional(), snapshot.charts(), log, configs, settings));
    }

    static List<Section> sections(List<HealthCheck> checks, EntryData[] additional, List<ChartData> charts, LogData log,
                                  List<ConfigFiles.Read> configs, DebugSettings settings) {
        List<Section> sections = new ArrayList<>();
        for (HealthCheck check : checks) {
            sections.add(Section.check(check.name(), check.level(), settings.applyFilter(check.message())));
        }
        for (EntryData entry : additional) {
            entry.applyFilter(settings);
            sections.add(Section.meta(entry.getName(), entry.getContent(), entry.getEntries()));
        }
        for (ChartData chart : charts) {
            sections.add(Section.chart(settings.applyFilter(chart.name()), settings.applyFilter(chart.description()),
                    chart.toChart(settings)));
        }
        sections.add(Section.text(Kind.LOG, "latest.log", log.getLog(), "log"));
        if (!log.getPluginLog().isBlank()) {
            sections.add(Section.text(Kind.PLUGIN_LOG, "Plugin log", log.getPluginLog(), "log"));
        }
        addExceptions(sections, Kind.INTERNAL_EXCEPTION, "Plugin exception", log.getInternalExceptions());
        addExceptions(sections, Kind.EXTERNAL_EXCEPTION, "Other exception", log.getExternalExceptions());
        for (ConfigFiles.Read config : configs) {
            sections.add(Section.text(Kind.CONFIG, config.name(), settings.applyFilter(config.content()),
                    formatOf(config.name())));
        }
        return sections;
    }

    private static void addExceptions(List<Section> sections, Kind kind, String name, ExceptionData[] exceptions) {
        int number = 1;
        for (ExceptionData exception : exceptions) {
            if (exception.text().isBlank()) continue;
            sections.add(Section.exception(kind, name + " " + number++, exception));
        }
    }

    /**
     * @return how Lyna highlights a file of this name
     */
    static String formatOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        String extension = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "yml", "yaml" -> "yaml";
            case "json", "properties", "toml" -> extension;
            default -> "text";
        };
    }

    public PluginMetaData plugin() {
        return plugin;
    }

    public ServerMetaData server() {
        return server;
    }

    public List<Section> sections() {
        return sections;
    }

    /**
     * One part of a report. Everything after {@code content} is a hint for the viewer, null when it does not apply,
     * and left out of the JSON then.
     *
     * @param name      what the viewer calls it, e.g. a file name; a {@code /} puts a config file into a folder
     * @param format    how the content is highlighted: yaml, json, properties, toml, log or text
     * @param level     the outcome of a {@link Kind#CHECK}
     * @param entries   the rows of a {@link Kind#META} section shown as a table
     * @param count     how often an exception occurred
     * @param firstSeen the log time it first occurred
     * @param lastSeen  the log time it last occurred
     * @param chart     type, data and hints of a {@link Kind#CHART}
     */
    public record Section(Kind kind, String name, String content, String format, HealthCheck.Level level,
                          List<Entry> entries, Integer count, String firstSeen, String lastSeen,
                          Map<String, Object> chart) {
        static Section text(Kind kind, String name, String content, String format) {
            return new Section(kind, name, content, format, null, null, null, null, null, null);
        }

        static Section check(String name, HealthCheck.Level level, String message) {
            return new Section(Kind.CHECK, name, message, null, level, null, null, null, null, null);
        }

        static Section meta(String name, String content, Map<String, String> entries) {
            List<Entry> rows = entries.entrySet().stream().map(e -> new Entry(e.getKey(), e.getValue())).toList();
            return new Section(Kind.META, name, content, null, null, rows.isEmpty() ? null : rows, null, null, null, null);
        }

        static Section exception(Kind kind, String name, ExceptionData exception) {
            return new Section(kind, name, exception.text(), null, null, null, exception.count(),
                    exception.firstSeen(), exception.lastSeen(), null);
        }

        static Section chart(String name, String description, Map<String, Object> chart) {
            return new Section(Kind.CHART, name, description, null, null, null, null, null, null, chart);
        }
    }

    /**
     * A row of a {@link Kind#META} section's table.
     */
    public record Entry(String key, String value) {
    }

    /**
     * What a section holds, which decides how the viewer shows it.
     */
    public enum Kind {
        /**
         * Something the plugin says about itself.
         */
        META,
        /**
         * The server's latest.log.
         */
        LOG,
        /**
         * The lines of the log the plugin wrote.
         */
        PLUGIN_LOG,
        /**
         * An error or warning from the plugin.
         */
        INTERNAL_EXCEPTION,
        /**
         * An error or warning from something else.
         */
        EXTERNAL_EXCEPTION,
        /**
         * A configuration file.
         */
        CONFIG,
        /**
         * The outcome of a {@link HealthCheck}.
         */
        CHECK,
        /**
         * A {@link ChartData chart} the plugin drew from its data.
         */
        CHART
    }
}
