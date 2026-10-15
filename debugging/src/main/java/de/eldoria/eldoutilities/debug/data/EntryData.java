/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.data;

import de.eldoria.eldoutilities.debug.DebugSettings;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Something a plugin adds about itself to a debug report: a block of text, or pairs of keys and values shown as a
 * table, or both.
 */
public class EntryData {
    protected String name;
    protected String content;
    protected Map<String, String> entries = Map.of();

    /**
     * Create a new debug entry
     *
     * @param name    Name of debug entry. This is the name displayed on the web page
     * @param content content of the debug entry.
     */
    public EntryData(String name, String content) {
        this.name = name;
        this.content = content;
    }

    /**
     * Create a new debug entry shown as a table.
     *
     * @param name    Name of debug entry. This is the name displayed on the web page
     * @param entries the rows, in the order they are shown
     */
    public EntryData(String name, Map<String, String> entries) {
        this.name = name;
        this.content = "";
        this.entries = new LinkedHashMap<>(entries);
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    /**
     * @return the rows of the table, empty when the entry is only text
     */
    public Map<String, String> getEntries() {
        return entries;
    }

    public void applyFilter(DebugSettings settings) {
        content = settings.applyFilter(content);
        Map<String, String> filtered = new LinkedHashMap<>();
        entries.forEach((key, value) -> filtered.put(key, settings.applyFilter(String.valueOf(value))));
        entries = filtered;
    }
}
