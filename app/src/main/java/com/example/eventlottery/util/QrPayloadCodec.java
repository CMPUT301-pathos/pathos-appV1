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

    public static String encodeEventId(String eventId) {
        return PREFIX + eventId;
    }

    public static String decodeEventId(String payload) {
        if (payload == null) return null;
        if (!payload.startsWith(PREFIX)) return null;
        return payload.substring(PREFIX.length());
    }
}