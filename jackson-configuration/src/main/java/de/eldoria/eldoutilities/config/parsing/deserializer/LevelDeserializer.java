/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.config.parsing.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.logging.Level;

public class LevelDeserializer extends JsonDeserializer<Level> {
    private static Level parseLevel(String level) {
        return switch (level.toUpperCase()) {
            case "OFF" -> Level.OFF;
            case "SEVERE" -> Level.SEVERE;
            case "WARNING" -> Level.WARNING;
            case "DEBUG", "TRUE" -> Level.CONFIG;
            case "FINE" -> Level.FINE;
            case "FINER" -> Level.FINER;
            case "FINEST" -> Level.FINEST;
            case "ALL" -> Level.ALL;
            default -> Level.INFO;
        };
    }

    @Override
    public Level deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return parseLevel(p.getText());
    }
}
