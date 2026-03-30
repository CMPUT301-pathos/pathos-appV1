package com.example.eventlottery.data;

import com.example.eventlottery.domain.EventSummary;

import java.util.List;

/**
 * Repository interface for organizer-created events.
 *
 * Supports:
 * - US 02.01.01: create event
 * - Event list screens: load all events / load events for a specific organizer
 * - US 02.09.01: load events manageable by organizer or co-organizer
 * - Organizer/account cleanup: delete owned events
 *
 * @author Kenneth Joseph and hasratsinghchauhan
 * @version 1.3
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

    interface OperationCallback {
        void onSuccess();
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

    /**
     * Returns all events the specified user can manage, including:
     * - events they created
     * - events where they are listed as a co-organizer
     */
    void getManageableEvents(String deviceId, ListCallback callback);

    /**
     * Deletes a single event by its Firestore document ID.
     */
    void deleteEvent(String eventId, OperationCallback callback);
}