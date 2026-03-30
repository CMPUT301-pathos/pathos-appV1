package com.example.eventlottery.domain;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for WaitListRecord geolocation support.
 *
 * User stories covered:
 * - US 01.01.01: Join the waiting list for a specific event
 * - US 02.02.02: Organizer sees on a map where entrants joined from
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class WaitListRecordGeoTest {

    @Test
    public void constructor_setsDefaultWaitingStatus_andNoLocation() {
        WaitListRecord record = new WaitListRecord("event1", "device1");

        assertEquals("event1", record.getEventId());
        assertEquals("device1", record.getDeviceId());
        assertEquals(WaitStatus.WAITING, record.getStatus());
        assertTrue(record.getJoinTimeMs() > 0);
        assertFalse(record.hasJoinLocation());
        assertNull(record.getJoinLatitude());
        assertNull(record.getJoinLongitude());
        assertNull(record.getJoinAccuracyMeters());
        assertNull(record.getJoinLocationTimestampMs());
    }

    @Test
    public void geoConstructor_setsLocationFields() {
        WaitListRecord record = new WaitListRecord(
                "event1",
                "device1",
                53.5461,
                -113.4938,
                8.5f,
                123456789L
        );

        assertEquals(Double.valueOf(53.5461), record.getJoinLatitude());
        assertEquals(Double.valueOf(-113.4938), record.getJoinLongitude());
        assertEquals(Float.valueOf(8.5f), record.getJoinAccuracyMeters());
        assertEquals(Long.valueOf(123456789L), record.getJoinLocationTimestampMs());
        assertTrue(record.hasJoinLocation());
    }

    @Test
    public void setJoinLocation_setsLocationFields() {
        WaitListRecord record = new WaitListRecord("event1", "device1");

        record.setJoinLocation(51.0447, -114.0719, 12.0f, 999L);

        assertEquals(Double.valueOf(51.0447), record.getJoinLatitude());
        assertEquals(Double.valueOf(-114.0719), record.getJoinLongitude());
        assertEquals(Float.valueOf(12.0f), record.getJoinAccuracyMeters());
        assertEquals(Long.valueOf(999L), record.getJoinLocationTimestampMs());
        assertTrue(record.hasJoinLocation());
    }

    @Test
    public void clearJoinLocation_removesLocationFields() {
        WaitListRecord record = new WaitListRecord("event1", "device1");
        record.setJoinLocation(51.0447, -114.0719, 12.0f, 999L);

        record.clearJoinLocation();

        assertFalse(record.hasJoinLocation());
        assertNull(record.getJoinLatitude());
        assertNull(record.getJoinLongitude());
        assertNull(record.getJoinAccuracyMeters());
        assertNull(record.getJoinLocationTimestampMs());
    }

    @Test
    public void acceptInvitation_whenInvited_setsAccepted() {
        WaitListRecord record = new WaitListRecord("event1", "device1");
        record.setStatus(WaitStatus.INVITED);

        record.acceptInvitation();

        assertEquals(WaitStatus.ACCEPTED, record.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void acceptInvitation_whenNotInvited_throwsException() {
        WaitListRecord record = new WaitListRecord("event1", "device1");

        record.acceptInvitation();
    }

    @Test
    public void declineInvitation_whenInvited_setsDeclined() {
        WaitListRecord record = new WaitListRecord("event1", "device1");
        record.setStatus(WaitStatus.INVITED);

        record.declineInvitation();

        assertEquals(WaitStatus.DECLINED, record.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void declineInvitation_whenNotInvited_throwsException() {
        WaitListRecord record = new WaitListRecord("event1", "device1");

        record.declineInvitation();
    }
}