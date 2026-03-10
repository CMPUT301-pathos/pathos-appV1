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

    @Test
    public void encodeThenDecode_returnsSameEventId() {
        String eventId = "abc123XYZ";
        String payload = QrPayloadCodec.encodeEventId(eventId);

        assertNotNull(payload);

        String decoded = QrPayloadCodec.decodeEventId(payload);
        assertEquals(eventId, decoded);
    }

    @Test
    public void decode_withNullPayload_returnsNull() {
        assertNull(QrPayloadCodec.decodeEventId(null));
    }

    @Test
    public void decode_withWrongPrefix_returnsNull() {
        assertNull(QrPayloadCodec.decodeEventId("not-a-valid-payload"));
        assertNull(QrPayloadCodec.decodeEventId("eventlottery:wrong:abc123"));
    }
}