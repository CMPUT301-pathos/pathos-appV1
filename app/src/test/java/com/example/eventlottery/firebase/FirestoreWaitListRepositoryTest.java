package com.example.eventlottery.firebase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.google.firebase.firestore.DocumentSnapshot;


import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Unit tests for FirestoreWaitListRepository numeric parsing and waitlist document mapping.
 *
 * User stories covered:
 * - US 01.01.01: Join the waiting list for a specific event
 * - US 02.02.02: Organizer sees on a map where entrants joined from
 *
 * Note:
 * These tests use reflection to exercise the repository's private helper methods
 * and document-mapping logic without needing live Firestore access.
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class FirestoreWaitListRepositoryTest {

    private FirestoreWaitListRepository repository;

    @Before
    public void setUp() {
        repository = new FirestoreWaitListRepository(null);
    }

    /**
     * Verifies that getLongValue() returns a Long when the field contains a long value.
     */
    @Test
    public void getLongValue_whenFieldIsLong_returnsLongValue() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.get("joinTimeMs")).thenReturn(123L);

        Long value = invokeGetLongValue(doc, "joinTimeMs");

        assertEquals(Long.valueOf(123L), value);
    }

    /**
     * Verifies that getLongValue() converts an Integer field value to Long.
     */
    @Test
    public void getLongValue_whenFieldIsInteger_returnsConvertedLong() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.get("joinTimeMs")).thenReturn(42);

        Long value = invokeGetLongValue(doc, "joinTimeMs");

        assertEquals(Long.valueOf(42L), value);
    }

    /**
     * Verifies that getLongValue() converts a Double field value to Long by truncating.
     */
    @Test
    public void getLongValue_whenFieldIsDouble_returnsConvertedLong() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.get("joinTimeMs")).thenReturn(123.9);

        Long value = invokeGetLongValue(doc, "joinTimeMs");

        assertEquals(Long.valueOf(123L), value);
    }

    /**
     * Verifies that getLongValue() returns null when the field is missing or null.
     */
    @Test
    public void getLongValue_whenFieldMissing_returnsNull() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.get("joinTimeMs")).thenReturn(null);

        Long value = invokeGetLongValue(doc, "joinTimeMs");

        assertNull(value);
    }

    /**
     * Verifies that getDoubleValue() returns a Double when the field contains a double value.
     */
    @Test
    public void getDoubleValue_whenFieldIsDouble_returnsDoubleValue() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.get("joinLatitude")).thenReturn(53.5461);

        Double value = invokeGetDoubleValue(doc, "joinLatitude");

        assertEquals(Double.valueOf(53.5461), value);
    }

    /**
     * Verifies that getDoubleValue() converts a Long field value to Double.
     */
    @Test
    public void getDoubleValue_whenFieldIsLong_returnsConvertedDouble() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.get("joinLatitude")).thenReturn(53L);

        Double value = invokeGetDoubleValue(doc, "joinLatitude");

        assertEquals(Double.valueOf(53.0), value);
    }

    /**
     * Verifies that getDoubleValue() converts an Integer field value to Double.
     */
    @Test
    public void getDoubleValue_whenFieldIsInteger_returnsConvertedDouble() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.get("joinLatitude")).thenReturn(51);

        Double value = invokeGetDoubleValue(doc, "joinLatitude");

        assertEquals(Double.valueOf(51.0), value);
    }

    /**
     * Verifies that getDoubleValue() returns null when the field is missing or null.
     */
    @Test
    public void getDoubleValue_whenFieldMissing_returnsNull() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.get("joinLatitude")).thenReturn(null);

        Double value = invokeGetDoubleValue(doc, "joinLatitude");

        assertNull(value);
    }

    /**
     * Verifies that getFloatValue() returns a Float when the field contains a float value.
     */
    @Test
    public void getFloatValue_whenFieldIsFloat_returnsFloatValue() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.get("joinAccuracyMeters")).thenReturn(8.5f);

        Float value = invokeGetFloatValue(doc, "joinAccuracyMeters");

        assertEquals(Float.valueOf(8.5f), value);
    }

    /**
     * Verifies that getFloatValue() converts a Double field value to Float.
     */
    @Test
    public void getFloatValue_whenFieldIsDouble_returnsConvertedFloat() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.get("joinAccuracyMeters")).thenReturn(8.5);

        Float value = invokeGetFloatValue(doc, "joinAccuracyMeters");

        assertEquals(Float.valueOf(8.5f), value);
    }

    /**
     * Verifies that getFloatValue() converts a Long field value to Float.
     */
    @Test
    public void getFloatValue_whenFieldIsLong_returnsConvertedFloat() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.get("joinAccuracyMeters")).thenReturn(8L);

        Float value = invokeGetFloatValue(doc, "joinAccuracyMeters");

        assertEquals(Float.valueOf(8.0f), value);
    }

    /**
     * Verifies that getFloatValue() converts an Integer field value to Float.
     */
    @Test
    public void getFloatValue_whenFieldIsInteger_returnsConvertedFloat() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.get("joinAccuracyMeters")).thenReturn(12);

        Float value = invokeGetFloatValue(doc, "joinAccuracyMeters");

        assertEquals(Float.valueOf(12.0f), value);
    }

    /**
     * Verifies that getFloatValue() returns null when the field is missing or null.
     */
    @Test
    public void getFloatValue_whenFieldMissing_returnsNull() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.get("joinAccuracyMeters")).thenReturn(null);

        Float value = invokeGetFloatValue(doc, "joinAccuracyMeters");

        assertNull(value);
    }

    /**
     * Verifies that fromDocument() correctly maps all document fields including
     * geolocation data into a WaitListRecord domain object.
     */
    @Test
    public void fromDocument_withGeoFields_mapsRecordCorrectly() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);

        when(doc.getString("eventId")).thenReturn("event1");
        when(doc.getString("deviceId")).thenReturn("device1");
        when(doc.getString("status")).thenReturn("INVITED");

        when(doc.get("joinTimeMs")).thenReturn(1000L);
        when(doc.get("joinLatitude")).thenReturn(53.5461);
        when(doc.get("joinLongitude")).thenReturn(-113.4938);
        when(doc.get("joinAccuracyMeters")).thenReturn(7.25);
        when(doc.get("joinLocationTimestampMs")).thenReturn(2000L);

        WaitListRecord record = invokeFromDocument(doc);

        assertEquals("event1", record.getEventId());
        assertEquals("device1", record.getDeviceId());
        assertEquals(WaitStatus.INVITED, record.getStatus());
        assertEquals(1000L, record.getJoinTimeMs());
        assertEquals(Double.valueOf(53.5461), record.getJoinLatitude());
        assertEquals(Double.valueOf(-113.4938), record.getJoinLongitude());
        assertEquals(Float.valueOf(7.25f), record.getJoinAccuracyMeters());
        assertEquals(Long.valueOf(2000L), record.getJoinLocationTimestampMs());
        assertTrue(record.hasJoinLocation());
    }

    /**
     * Verifies that fromDocument() defaults to WAITING status when the document
     * contains an invalid status string and omits location fields.
     */
    @Test
    public void fromDocument_withInvalidStatus_defaultsToWaiting() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);

        when(doc.getString("eventId")).thenReturn("event1");
        when(doc.getString("deviceId")).thenReturn("device1");
        when(doc.getString("status")).thenReturn("NOT_A_REAL_STATUS");

        when(doc.get("joinTimeMs")).thenReturn(null);
        when(doc.get("joinLatitude")).thenReturn(null);
        when(doc.get("joinLongitude")).thenReturn(null);
        when(doc.get("joinAccuracyMeters")).thenReturn(null);
        when(doc.get("joinLocationTimestampMs")).thenReturn(null);

        WaitListRecord record = invokeFromDocument(doc);

        assertEquals(WaitStatus.WAITING, record.getStatus());
        assertNull(record.getJoinLatitude());
        assertNull(record.getJoinLongitude());
        assertNull(record.getJoinAccuracyMeters());
        assertNull(record.getJoinLocationTimestampMs());
    }

    private Long invokeGetLongValue(DocumentSnapshot doc, String field) throws Exception {
        Method method = FirestoreWaitListRepository.class
                .getDeclaredMethod("getLongValue", DocumentSnapshot.class, String.class);
        method.setAccessible(true);
        return (Long) method.invoke(repository, doc, field);
    }

    private Double invokeGetDoubleValue(DocumentSnapshot doc, String field) throws Exception {
        Method method = FirestoreWaitListRepository.class
                .getDeclaredMethod("getDoubleValue", DocumentSnapshot.class, String.class);
        method.setAccessible(true);
        return (Double) method.invoke(repository, doc, field);
    }

    private Float invokeGetFloatValue(DocumentSnapshot doc, String field) throws Exception {
        Method method = FirestoreWaitListRepository.class
                .getDeclaredMethod("getFloatValue", DocumentSnapshot.class, String.class);
        method.setAccessible(true);
        return (Float) method.invoke(repository, doc, field);
    }

    private WaitListRecord invokeFromDocument(DocumentSnapshot doc) throws Exception {
        Method method = FirestoreWaitListRepository.class
                .getDeclaredMethod("fromDocument", DocumentSnapshot.class);
        method.setAccessible(true);
        return (WaitListRecord) method.invoke(repository, doc);
    }
}