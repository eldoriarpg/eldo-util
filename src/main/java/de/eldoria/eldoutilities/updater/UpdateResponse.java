package de.eldoria.eldoutilities.updater;

import org.bukkit.plugin.Plugin;

public class UpdateResponse {
    private final boolean update;
    private final String latest;

    /**
     * Create a new Update check response.
     *
     * @param update whether a new version is available or not
     * @param latest latest available version
     */
    public UpdateResponse(boolean update, String latest) {
        this.update = update;
        this.latest = latest;
    }

    public boolean isOutdated() {
        return update;
    }

    public String latestVersion() {
        return latest;
    }

    public static UpdateResponse create(String latest, Plugin plugin) {
        return new UpdateResponse(!plugin.getDescription().getVersion().equalsIgnoreCase(latest), latest);
    }
}
