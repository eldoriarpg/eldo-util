/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.config.exceptions;

public class ConfigurationException extends RuntimeException{
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
