package com.example.eventlottery.controller;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.domain.EventSummary;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for event browsing, filtering, and lottery criteria.
 *
 * Responsibilities:
 * - Load all events from the repository
 * - Filter events by category, location, and availability
 * - Generate lottery criteria text for a given event
 *
 * User stories supported:
 * - US 01.01.03: See a list of events to join the waiting list for
 * - US 01.01.04: Filter events based on interests and availability
 * - US 01.05.05: Be informed about lottery selection criteria
 *
 * @author Fawaz Mansoor
 * @version 1.1
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

    public String getLotteryCriteria(EventSummary event) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "MMM dd, yyyy", java.util.Locale.getDefault());

        StringBuilder criteria = new StringBuilder();

        criteria.append("📋 Lottery Criteria\n\n");

        // Registration window
        if (event.getRegistrationStart() > 0) {
            criteria.append("📅 Registration Opens:\n")
                    .append(sdf.format(new java.util.Date(event.getRegistrationStart())))
                    .append("\n\n");
        }
        if (event.getRegistrationEnd() > 0) {
            criteria.append("📅 Registration Closes:\n")
                    .append(sdf.format(new java.util.Date(event.getRegistrationEnd())))
                    .append("\n\n");
        }

        // Waiting list capacity
        if (event.getCapacity() > 0) {
            criteria.append("👥 Waiting List Capacity:\n")
                    .append(event.getCapacity()).append(" entrants max")
                    .append("\n\n");
        } else {
            criteria.append("👥 Waiting List Capacity:\n")
                    .append("Unlimited")
                    .append("\n\n");
        }

        // Draw size
        if (event.getDrawSize() > 0) {
            criteria.append("Number of Winners:\n")
                    .append(event.getDrawSize()).append(" entrants will be selected")
                    .append("\n\n");
        }

        // Selection process explanation
        criteria.append("Selection Process:\n")
                .append("Winners are chosen randomly and fairly from all entrants " +
                        "on the waiting list once registration closes. " +
                        "If a selected entrant declines, a replacement is drawn.");

        return criteria.toString();
    }
}
