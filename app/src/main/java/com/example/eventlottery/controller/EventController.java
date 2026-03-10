package com.example.eventlottery.controller;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.domain.EventSummary;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for event browsing and filtering.
 *
 * Responsibilities:
 * - Load all events from the repository
 * - Filter events by category, location, and availability
 *
 * User stories supported:
 * - US 01.01.03: See a list of events to join the waiting list for
 * - US 01.01.04: Filter events based on interests and availability
 *
 * @author Fawaz Mansoor
 * @version 1.0
 */
public class EventController {

    private final EventRepository eventRepository;
    private List<EventSummary> allEvents = new ArrayList<>();

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void loadAllEvents(EventRepository.ListCallback callback) {
        eventRepository.getAllEvents(new EventRepository.ListCallback() {
            @Override
            public void onSuccess(List<EventSummary> events) {
                allEvents = events;
                callback.onSuccess(events);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public List<EventSummary> filterByCategory(String category) {
        if (category == null || category.equals("All")) return allEvents;
        List<EventSummary> filtered = new ArrayList<>();
        for (EventSummary e : allEvents) {
            if (e.getCategory().equalsIgnoreCase(category)) filtered.add(e);
        }
        return filtered;
    }

    public List<EventSummary> filterByLocation(String location) {
        if (location == null || location.isEmpty()) return allEvents;
        List<EventSummary> filtered = new ArrayList<>();
        for (EventSummary e : allEvents) {
            if (e.getLocation().toLowerCase().contains(location.toLowerCase())) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    public List<EventSummary> filterByRegistrationOpen() {
        List<EventSummary> filtered = new ArrayList<>();
        for (EventSummary e : allEvents) {
            if (e.isRegistrationOpen()) filtered.add(e);
        }
        return filtered;
    }

    public List<EventSummary> filterByCategoryAndAvailability(String category, boolean openOnly) {
        List<EventSummary> filtered = new ArrayList<>();
        for (EventSummary e : allEvents) {
            boolean categoryMatch = category == null || category.equals("All")
                    || e.getCategory().equalsIgnoreCase(category);
            boolean availabilityMatch = !openOnly || e.isRegistrationOpen();
            if (categoryMatch && availabilityMatch) filtered.add(e);
        }
        return filtered;
    }
}
