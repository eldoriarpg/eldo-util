package de.eldoria.eldoutilities.configuration;

/**
 * Exception to signalize that a config was tried to load from a configuration which is not a main configuration.
 */
public class ExternalConfigException extends RuntimeException {
    public ExternalConfigException() {
        super("You tried to load a configuration file from an external configuration. This is forbidden. " +
                "Please use only the main config instance to load other config files.");
    }
}
