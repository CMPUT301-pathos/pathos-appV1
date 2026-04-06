package com.example.eventlottery.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for QR payload encoding/decoding.
 *
 * Supports US 02.01.01 by ensuring:
 * - the QR payload generated for an event can be decoded back into the same eventId.
 *
 * These tests are pure Java (no Android runtime required).
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class QrPayloadCodecTest {

    /**
     * Verifies that encoding an event ID and then decoding it returns
     * the original event ID.
     */
    @Test
    public void encodeThenDecode_returnsSameEventId() {
        String eventId = "abc123XYZ";
        String payload = QrPayloadCodec.encodeEventId(eventId);

        assertNotNull(payload);

        String decoded = QrPayloadCodec.decodeEventId(payload);
        assertEquals(eventId, decoded);
    }

    /**
     * Verifies that decodeEventId() returns null when passed a null payload.
     */
    @Test
    public void decode_withNullPayload_returnsNull() {
        assertNull(QrPayloadCodec.decodeEventId(null));
    }

    /**
     * Verifies that decodeEventId() returns null when passed a payload with
     * an invalid prefix or malformed structure.
     */
    @Test
    public void decode_withWrongPrefix_returnsNull() {
        assertNull(QrPayloadCodec.decodeEventId("not-a-valid-payload"));
        assertNull(QrPayloadCodec.decodeEventId("eventlottery:wrong:abc123"));
    }
}