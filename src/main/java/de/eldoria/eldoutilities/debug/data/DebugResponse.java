package de.eldoria.eldoutilities.debug.data;

public class DebugResponse {
    private String hash;
    private String deletionHash;

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
