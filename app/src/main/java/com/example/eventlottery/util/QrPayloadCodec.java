package com.example.eventlottery.util;

/**
 * Encodes/decodes QR payloads.
 *
 * For US 02.01.01, we only need to encode an eventId into a QR-safe string.
 *
 * Payload format:
 *   eventlottery:event:<eventId>
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class QrPayloadCodec {

    private static final String PREFIX = "eventlottery:event:";

    /**
     * Encodes the given event identifier into a QR-safe payload string.
     *
     * @param eventId event identifier to encode
     * @return encoded QR payload string
     */
    public static String encodeEventId(String eventId) {
        return PREFIX + eventId;
    }

    /**
     * Decodes an event identifier from a QR payload string.
     *
     * @param payload QR payload string to decode
     * @return extracted event identifier, or null if the payload is invalid
     */
    public static String decodeEventId(String payload) {
        if (payload == null) return null;
        if (!payload.startsWith(PREFIX)) return null;
        return payload.substring(PREFIX.length());
    }
}