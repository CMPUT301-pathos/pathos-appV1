package com.example.eventlottery.domain;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link EventComment}.
 *
 * User stories covered:
 * - US 01.08.01: Post comments on an event
 * - US 01.08.02: View comments on an event
 *
 * @author Edwin David
 * @version 1.0
 */
public class EventCommentTest {

    @Test
    public void constructor_storesAllFields() {
        EventComment c = new EventComment("event1", "device1", "Alice", "Hello!");
        assertEquals("event1", c.getEventId());
        assertEquals("device1", c.getDeviceId());
        assertEquals("Alice", c.getAuthorName());
        assertEquals("Hello!", c.getText());
    }

    @Test
    public void constructor_setsTimestamp() {
        long before = System.currentTimeMillis();
        EventComment c = new EventComment("e1", "d1", "Bob", "Test");
        long after = System.currentTimeMillis();
        assertTrue(c.getCreatedAt() >= before && c.getCreatedAt() <= after);
    }
}
