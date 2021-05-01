package de.eldoria.eldoutilities.configuration;

public class ExternalConfigException extends RuntimeException {
    public ExternalConfigException() {
        super("You tried to load a configuration file from an external configuration. This is forbidden. " +
                "Please use only the main config instance to load other config files.");
    }
}
