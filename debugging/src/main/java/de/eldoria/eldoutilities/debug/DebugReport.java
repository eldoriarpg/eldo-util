/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import de.eldoria.eldoutilities.debug.data.EntryData;
import de.eldoria.eldoutilities.debug.data.LogData;
import de.eldoria.eldoutilities.debug.data.PluginMetaData;
import de.eldoria.eldoutilities.debug.data.ServerMetaData;
import de.eldoria.eldoutilities.debug.payload.LogMeta;
import de.eldoria.eldoutilities.debug.payload.PluginMeta;
import de.eldoria.eldoutilities.debug.payload.ServerMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * A debug report as Lyna takes it, in its second format: the plugin and the server, then the sections in the order
 * they are shown.
 * <p>
 * It is built in two steps. {@link #collect(Plugin)} asks the server about itself and the plugin's
 * {@link DebugDataProvider}s, which belongs on the thread that owns that state; {@link #create(Snapshot, DebugSettings)}
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
                           EntryData[] additional) {
    }

    /**
     * Asks the server and the plugin about themselves.
     *
     * @param plugin the plugin the report is about
     */
    public static Snapshot collect(Plugin plugin) {
        return new Snapshot(plugin, PluginMeta.create(plugin), ServerMeta.create(),
                DebugUtil.getAdditionalPluginMeta(plugin));
    }

    /**
     * Completes a report with the log and the configuration files, every section filtered.
     */
    public static DebugReport create(Snapshot snapshot, DebugSettings settings) {
        LogData log = LogMeta.create(snapshot.plugin(), settings);
        List<ConfigFiles.Read> configs = ConfigFiles.read(snapshot.plugin(), settings.configFiles(snapshot.plugin()));
        return new DebugReport(snapshot.pluginMeta(), snapshot.serverMeta(),
                sections(snapshot.additional(), log, configs, settings));
    }

    static List<Section> sections(EntryData[] additional, LogData log, List<ConfigFiles.Read> configs,
                                  DebugSettings settings) {
        List<Section> sections = new ArrayList<>();
        for (EntryData entry : additional) {
            sections.add(new Section(Kind.META, entry.getName(), settings.applyFilter(entry.getContent())));
        }
        sections.add(new Section(Kind.LOG, "latest.log", log.getLog()));
        if (!log.getPluginLog().isBlank()) {
            sections.add(new Section(Kind.PLUGIN_LOG, "Plugin log", log.getPluginLog()));
        }
        addExceptions(sections, Kind.INTERNAL_EXCEPTION, "Plugin exception", log.getInternalExceptions());
        addExceptions(sections, Kind.EXTERNAL_EXCEPTION, "Other exception", log.getExternalExceptions());
        for (ConfigFiles.Read config : configs) {
            sections.add(new Section(Kind.CONFIG, config.name(), settings.applyFilter(config.content())));
        }
        return sections;
    }

    private static void addExceptions(List<Section> sections, Kind kind, String name, String[] exceptions) {
        int number = 1;
        for (String exception : exceptions) {
            if (exception.isBlank()) continue;
            sections.add(new Section(kind, name + " " + number++, exception));
        }
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
     * One part of a report.
     *
     * @param name what the viewer calls it, e.g. a file name
     */
    public record Section(Kind kind, String name, String content) {
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
        CONFIG
    }
}
