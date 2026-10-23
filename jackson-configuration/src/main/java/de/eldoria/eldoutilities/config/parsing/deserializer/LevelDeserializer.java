/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.config.parsing.deserializer;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.util.logging.Level;

public class LevelDeserializer extends ValueDeserializer<Level> {
    private static Level parseLevel(String level) {
        return switch (level.toUpperCase()) {
            case "OFF" -> Level.OFF;
            case "SEVERE" -> Level.SEVERE;
            case "WARNING" -> Level.WARNING;
            case "CONFIG", "DEBUG", "TRUE" -> Level.CONFIG;
            case "FINE" -> Level.FINE;
            case "FINER" -> Level.FINER;
            case "FINEST" -> Level.FINEST;
            case "ALL" -> Level.ALL;
            default -> Level.INFO;
        };
    }

    @Override
    public Level deserialize(JsonParser p, DeserializationContext ctxt) {
        return parseLevel(p.getString());
    }
}
