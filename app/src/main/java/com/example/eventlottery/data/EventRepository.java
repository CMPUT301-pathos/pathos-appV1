package com.example.eventlottery.data;

import com.example.eventlottery.domain.Event;

/**
 * Repository interface for organizer-created events.
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public interface EventRepository {

    interface CreateCallback {
        void onSuccess(String eventId);
        void onFailure(Exception e);
    }

    void createEvent(Event event, CreateCallback callback);
}