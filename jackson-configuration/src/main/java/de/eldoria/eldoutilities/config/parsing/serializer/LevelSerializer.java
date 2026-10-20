/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.config.parsing.serializer;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.logging.Level;

public class LevelSerializer extends ValueSerializer<Level> {
    @Override
    public void serialize(Level value, JsonGenerator gen, SerializationContext serializers) {
        gen.writeString(value.getName());
    }
}
