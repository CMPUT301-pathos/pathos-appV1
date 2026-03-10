package com.example.eventlottery.data;

import com.example.eventlottery.domain.EventSummary;

import java.util.List;

/**
 * Repository interface for organizer-created events.
 *
 * Supports:
 * - US 02.01.01: create event
 * - Event list screens: load all events / load events for a specific organizer
 *
 * @author Kenneth Joseph
 * @version 1.1
 */
public interface EventRepository {

    interface CreateCallback {
        void onSuccess(String eventId);
        void onFailure(Exception e);
    }

    interface ListCallback {
        void onSuccess(List<EventSummary> events);
        void onFailure(Exception e);
    }

    void createEvent(Object event, CreateCallback callback);

    /**
     * Returns all events for the "Events" tab list.
     */
    void getAllEvents(ListCallback callback);

    /**
     * Returns only events created by the specified organizer (deviceId).
     */
    void getEventsByOrganizer(String organizerDeviceId, ListCallback callback);
}