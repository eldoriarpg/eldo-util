package de.eldoria.eldoutilities.crossversion;

/**
 * A exception which is thrown when the current server version is not supported by the plugin.
 */
public class UnsupportedVersionException extends RuntimeException {
    public UnsupportedVersionException() {
        super("Version " + ServerVersion.CURRENT_VERSION.name() + " is not supported.");
    }
}
