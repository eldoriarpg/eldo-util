/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.payload;

import de.eldoria.eldoutilities.debug.DebugSettings;
import de.eldoria.eldoutilities.debug.data.ExceptionData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Counts the errors and warnings in a log, split into the plugin's own and everything else.
 * <p>
 * A log may be scanned in overlapping parts, so each occurrence is known by the line it starts on and counted once.
 * The same message logged again is the same entry, counted up, with the time of its first and last occurrence.
 */
final class ExceptionCounter {
    private static final Pattern EXCEPTION = Pattern.compile(
            "([0-9]{2}:[0-9]{2}:[0-9]{2})] (\\[[^\\]]*?(?:ERROR|WARN)]:.*?)^\\[", Pattern.DOTALL + Pattern.MULTILINE);

    private final String project;
    private final Map<String, Occurrences> internal = new LinkedHashMap<>();
    private final Map<String, Occurrences> external = new LinkedHashMap<>();
    private final Set<Long> seen = new HashSet<>();

    /**
     * @param main the plugin's main class; a trace naming its first three packages is the plugin's own
     */
    ExceptionCounter(String main) {
        var packages = main.split("\\.");
        project = String.join(".", Arrays.copyOfRange(packages, 0, Math.min(packages.length, 3)));
    }

    /**
     * Counts what a part of the log holds.
     *
     * @param lines     the part
     * @param firstLine the number of its first line in the whole log, counted from 0
     */
    void scan(List<String> lines, long firstLine) {
        var text = String.join("\n", lines);
        var matcher = EXCEPTION.matcher(text);
        long line = firstLine;
        int counted = 0;
        while (matcher.find()) {
            line += newlines(text, counted, matcher.start());
            counted = matcher.start();
            if (!seen.add(line)) continue;
            var message = matcher.group(2);
            (message.contains(project) ? internal : external)
                    .computeIfAbsent(message, key -> new Occurrences())
                    .add(matcher.group(1));
        }
    }

    /**
     * @return the plugin's own, filtered, in the order they first occurred
     */
    ExceptionData[] internal(DebugSettings settings) {
        return filtered(internal, settings);
    }

    /**
     * @return everything else, filtered, in the order they first occurred
     */
    ExceptionData[] external(DebugSettings settings) {
        return filtered(external, settings);
    }

    private static ExceptionData[] filtered(Map<String, Occurrences> found, DebugSettings settings) {
        Map<String, Occurrences> merged = new LinkedHashMap<>();
        found.forEach((message, occurrences) -> merged.computeIfAbsent(
                        settings.applyFilter(message), key -> new Occurrences())
                .merge(occurrences));
        return merged.entrySet().stream()
                .map(entry -> new ExceptionData(
                        entry.getKey(), entry.getValue().count, entry.getValue().first, entry.getValue().last))
                .toArray(ExceptionData[]::new);
    }

    private static int newlines(String text, int from, int to) {
        int count = 0;
        for (int i = from; i < to; i++) {
            if (text.charAt(i) == '\n') count++;
        }
        return count;
    }

    private static final class Occurrences {
        private int count;
        private String first;
        private String last;

        void add(String time) {
            count++;
            if (first == null) first = time;
            last = time;
        }

        void merge(Occurrences other) {
            count += other.count;
            if (first == null) first = other.first;
            if (other.last != null) last = other.last;
        }
    }
}
