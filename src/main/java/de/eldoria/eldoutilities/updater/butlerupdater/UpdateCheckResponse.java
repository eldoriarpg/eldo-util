package de.eldoria.eldoutilities.updater.butlerupdater;

/**
 * Web Response for butler application.
 *
 * @since 1.1.0
 */
public class UpdateCheckResponse {
    private final boolean newVersionAvailable;
    private final String latestVersion;
    private final String hash;

    public UpdateCheckResponse(boolean newVersionAvailable, String latestVersion, String hash) {
        this.newVersionAvailable = newVersionAvailable;
        this.latestVersion = latestVersion;
        this.hash = hash;
    }

    public boolean isNewVersionAvailable() {
        return newVersionAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getHash() {
        return hash;
    }
}
