/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Where debug reports are sent, what is hidden in them, and which configuration files they carry.
 * <p>
 * Everything a report holds passes through the filters before it leaves the server; the Lyna instance stores what it
 * receives. The default filters hide IP addresses (an IPv4 address keeps its first two octets, loopback stays),
 * passwords, tokens, secrets and API keys in YAML, properties and JSON form, credentials in URLs, and database user
 * names.
 */
public final class DebugSettings {
    /**
     * The Lyna instance reports are sent to by default.
     */
    public static final String DEFAULT_HOST = "https://lyna.eldoria.de";

    private static final String SECRET_KEY = "[\\w.-]*(?i:password|passwd|token|secret|api[-_]?key)[\\w.-]*";
    private static final String OCTET = "(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)";

    public static final List<Filter> DEFAULT_FILTER = List.of(
            new Filter(Pattern.compile("(?i)\\b([a-z][a-z0-9+.-]*://)[^\\s/:@]+:[^\\s/@]+@"), "$1*****:*****@"),
            new Filter(
                    Pattern.compile(
                            "([\"']?" + SECRET_KEY + "[\"']?\\s*[:=]\\s*)([\"'])(?:(?!\\2)[^\\\\\\r\\n]|\\\\.)+\\2"),
                    "$1$2*****$2"),
            new Filter(
                    Pattern.compile("(?m)^(\\s*-?\\s*" + SECRET_KEY + "\\s*[:=][ \\t]*)(?![\"'{\\[])\\S[^\\r\\n]*$"),
                    "$1*****"),
            new Filter(Pattern.compile("(?im)^(\\s*-?\\s*user(?:name)?\\s*[:=][ \\t]*)\\S[^\\r\\n]*$"), "$1*****"),
            new Filter(
                    Pattern.compile("(?<![\\w.])(?!127\\.)(" + OCTET + "\\." + OCTET + ")\\." + OCTET + "\\." + OCTET
                            + "(?![\\w.])"),
                    "$1.x.x"),
            new Filter(
                    Pattern.compile("(?<![\\w:])(?:(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}"
                            + "|(?:[0-9a-fA-F]{1,4}:){1,7}:(?:[0-9a-fA-F]{1,4}(?::[0-9a-fA-F]{1,4}){0,6})?)(?![\\w:])"),
                    "x:x:x:x:x:x:x:x"));

    public static final DebugSettings DEFAULT = builder().withDefaultFilter().build();

    private final String host;
    private final List<Filter> filters = new ArrayList<>();
    private final Function<Plugin, List<Path>> configFiles;

    private DebugSettings(String host, List<Filter> filters, Function<Plugin, List<Path>> configFiles) {
        this.host = host;
        this.filters.addAll(filters);
        this.configFiles = configFiles;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return the address of the Lyna instance, without a trailing slash
     */
    public String getHost() {
        return host;
    }

    public void addFilter(Filter... filter) {
        filters.addAll(Arrays.asList(filter));
    }

    public String applyFilter(String text) {
        for (var filter : filters) {
            text = filter.apply(text);
        }
        return text;
    }

    /**
     * @return the configuration files a report about this plugin carries
     */
    public List<Path> configFiles(Plugin plugin) {
        return configFiles.apply(plugin);
    }

    public static class Builder {
        private final List<Filter> filters = new ArrayList<>();
        private String host = DEFAULT_HOST;
        private Function<Plugin, List<Path>> configFiles = ConfigFiles::inDataFolder;

        /**
         * Sends reports to another Lyna instance.
         *
         * @param host its address, e.g. {@code https://lyna.example.com}
         */
        public Builder forHost(String host) {
            this.host = host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
            return this;
        }

        public Builder withFilter(Filter... filter) {
            this.filters.addAll(Arrays.asList(filter));
            return this;
        }

        public Builder withDefaultFilter() {
            this.filters.addAll(DEFAULT_FILTER);
            return this;
        }

        /**
         * Replaces the choice of configuration files, which by default is {@link ConfigFiles#inDataFolder(Plugin)}.
         * Whatever is chosen is still held to the size limits of {@link ConfigFiles}.
         *
         * @param configFiles chooses the files for a plugin; call {@link ConfigFiles#inDataFolder(Plugin)} to extend
         *                    the default rather than replace it
         */
        public Builder withConfigFiles(Function<Plugin, List<Path>> configFiles) {
            this.configFiles = configFiles;
            return this;
        }

        public DebugSettings build() {
            return new DebugSettings(host, filters, configFiles);
        }
    }
}
