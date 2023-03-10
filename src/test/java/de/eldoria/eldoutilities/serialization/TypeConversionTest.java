/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.serialization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class TypeConversionTest {
    @Test
    public void shouldGet16BytesFromAUUID() {
        UUID uuid = UUID.randomUUID();

        byte[] result = TypeConversion.getBytesFromUUID(uuid);

        Assertions.assertEquals(16, result.length);
    }

    @Test
    public void shouldReconstructSameUUIDFromByteArray() {
        UUID uuid = UUID.randomUUID();

        byte[] bytes = TypeConversion.getBytesFromUUID(uuid);
        UUID reconstructedUuid = TypeConversion.getUUIDFromBytes(bytes);

        Assertions.assertEquals(uuid, reconstructedUuid);
    }

    @Test
    public void shouldNotGenerateTheSameUUIDFromBytes() {
        UUID uuid = UUID.fromString("9f881758-0b4a-4eaa-b59f-b6dea0934223");

        byte[] result = TypeConversion.getBytesFromUUID(uuid);
        UUID newUuid = UUID.nameUUIDFromBytes(result);

        Assertions.assertNotEquals(uuid, newUuid);
    }
}
