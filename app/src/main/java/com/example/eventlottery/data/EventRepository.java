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
        /**
         * Called when event creation succeeds.
         *
         * @param eventId the created event's document ID
         */
        void onSuccess(String eventId);

        /**
         * Called when event creation fails.
         *
         * @param e the exception describing the failure
         */
        void onFailure(Exception e);
    }

    interface ListCallback {
        /**
         * Called when a list of events is successfully retrieved.
         *
         * @param events the list of events returned
         */
        void onSuccess(List<EventSummary> events);

        /**
         * Called when an event list retrieval fails.
         *
         * @param e the exception describing the failure
         */
        void onFailure(Exception e);
    }

    interface OperationCallback {
        /**
         * Called when an operation completes successfully.
         */
        void onSuccess();

        /**
         * Called when an operation fails.
         *
         * @param e the exception describing the failure
         */
        void onFailure(Exception e);
    }

    /**
     * Creates a new event record.
     *
     * @param event the event payload to save
     * @param callback callback for success or failure
     */
    void createEvent(Object event, CreateCallback callback);

    /**
     * Returns all events for the "Events" tab list.
     *
     * @param callback callback for success or failure
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