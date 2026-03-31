package com.example.eventlottery.domain;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EventTest {

    @Test
    public void setCoOrganizerIds_null_setsEmptyList() {
        Event event = new Event();
        event.setCoOrganizerIds(null);

        assertNotNull(event.getCoOrganizerIds());
        assertTrue(event.getCoOrganizerIds().isEmpty());
    }

    @Test
    public void getCoOrganizerIds_returnsDefensiveCopy() {
        Event event = new Event();
        event.setCoOrganizerIds(new ArrayList<>(Arrays.asList("co1", "co2")));

        List<String> retrieved = event.getCoOrganizerIds();
        retrieved.add("co3");

        assertEquals(2, event.getCoOrganizerIds().size());
        assertFalse(event.getCoOrganizerIds().contains("co3"));
    }

    @Test
    public void toMap_includesCoOrganizerIds() {
        Event event = new Event();
        event.setName("Sample Event");
        event.setCoOrganizerIds(Arrays.asList("co1", "co2"));

        Map<String, Object> map = event.toMap();

        assertTrue(map.containsKey("coOrganizerIds"));
        Object value = map.get("coOrganizerIds");
        assertTrue(value instanceof List);

        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) value;

        assertEquals(2, ids.size());
        assertTrue(ids.contains("co1"));
        assertTrue(ids.contains("co2"));
    }
}