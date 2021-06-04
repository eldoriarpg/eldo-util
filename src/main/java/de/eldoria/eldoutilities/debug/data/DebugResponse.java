package de.eldoria.eldoutilities.debug.data;

/**
 * Response of UpdateButler.
 */
public class DebugResponse {
    private final String hash;
    private final String deletionHash;

    public DebugResponse(String hash, String deletionHash) {
        this.hash = hash;
        this.deletionHash = deletionHash;
    }

    public String getHash() {
        return hash;
    }

    public String getDeletionHash() {
        return deletionHash;
    }
}
