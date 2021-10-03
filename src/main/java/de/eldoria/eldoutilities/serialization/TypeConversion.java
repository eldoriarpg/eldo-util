package de.eldoria.eldoutilities.serialization;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Class for simple type conversion.
 *
 * @since 1.0.0
 */
public final class TypeConversion {
    private TypeConversion() {
        throw new UnsupportedOperationException("This is a utility class!");
    }

    /**
     * Converts a UUID to a byte array.
     *
     * @param uuid UUID to convert
     * @return UUID as byte array
     */
    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }

    /**
     * Convers a byte array to UUID
     *
     * @param bytes array to convert
     * @return byte array as UUID
     */
    public static UUID getUUIDFromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        Long high = byteBuffer.getLong();
        Long low = byteBuffer.getLong();

        return new UUID(high, low);
    }
}
