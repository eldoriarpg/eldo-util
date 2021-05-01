package de.eldoria.eldoutilities.debug.data;

import de.eldoria.eldoutilities.debug.DebugSettings;

public class EntryData {
    protected String name;
    protected String content;

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

    public void applyFilter(DebugSettings settings) {
        content = settings.applyFilter(content);
    }
}
