package de.eldoria.eldoutilities.debug;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public enum DefaultProperties {
    /**
     * The artifact name
     */
    ARTIFACT("artifact"),
    /**
     * The artifact version
     */
    ARTIFACT_VERSION("artifactVersion"),
    /**
     * Commit the build was based on
     */
    COMMIT("commit"),
    /**
     * Branch the build was based on
     */
    BRANCH("branch"),
    /**
     * Runtime used for compilation
     */
    RUNTIME("runtime"),
    /**
     * The build time as {@link DateTimeFormatter#ISO_INSTANT}
     */
    TIME("time"),
    /**
     * The build time as {@link Instant#getEpochSecond()}
     */
    UNIX("unix");

    private final String key;

    DefaultProperties(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
