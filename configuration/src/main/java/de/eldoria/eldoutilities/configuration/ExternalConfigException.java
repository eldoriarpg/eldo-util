/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.configuration;

/**
 * Exception to signalize that a config was tried to load from a configuration which is not a main configuration.
 * @deprecated Use the jackson-configuration module. This module is removed in 4.0.
 */
@Deprecated(since = "3.6.0", forRemoval = true)
public class ExternalConfigException extends RuntimeException {
    public ExternalConfigException() {
        super("You tried to load a configuration file from an external configuration. This is forbidden. "
                + "Please use only the main config instance to load other config files.");
    }
}
