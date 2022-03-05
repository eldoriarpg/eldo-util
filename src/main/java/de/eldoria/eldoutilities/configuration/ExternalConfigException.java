/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

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
