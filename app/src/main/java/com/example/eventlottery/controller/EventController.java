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
 * - Filter events by category, location, availability, and capacity
 * - Search events by keyword across name, description, and category
 * - Generate lottery criteria text for a given event
 *
 * User stories supported:
 * - US 01.01.03: See a list of events to join the waiting list for
 * - US 01.01.04: Filter events based on interests and availability
 * - US 01.01.05: Search for events by keyword
 * - US 01.01.06: Use keyword search with filtering
 * - US 01.05.05: Be informed about lottery selection criteria
 *
 * @author Fawaz Mansoor
 * @version 1.2
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

    /**
     * Searches events by keyword across name, description, and category.
     *
     * @param keyword search term (case-insensitive)
     * @return list of matching events
     */
    public List<EventSummary> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return new ArrayList<>(allEvents);
        String lower = keyword.toLowerCase().trim();
        List<EventSummary> results = new ArrayList<>();
        for (EventSummary e : allEvents) {
            if (e.getName().toLowerCase().contains(lower)
                    || e.getDescription().toLowerCase().contains(lower)
                    || e.getCategory().toLowerCase().contains(lower)) {
                results.add(e);
            }
        }
        return results;
    }

    /**
     * Filters events by maximum capacity.
     * Events with capacity 0 (unlimited) are always included.
     *
     * @param maxCapacity maximum capacity threshold (0 = no filter)
     * @return list of matching events
     */
    public List<EventSummary> filterByMaxCapacity(int maxCapacity) {
        if (maxCapacity <= 0) return new ArrayList<>(allEvents);
        List<EventSummary> filtered = new ArrayList<>();
        for (EventSummary e : allEvents) {
            if (e.getCapacity() == 0 || e.getCapacity() <= maxCapacity) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    /**
     * Applies all filters and keyword search together.
     *
     * @param keyword     search term (empty = no keyword filter)
     * @param category    category filter ("All" = no filter)
     * @param location    location filter (empty = no filter)
     * @param openOnly    if true, only show events with open registration
     * @param maxCapacity max capacity filter (0 = no filter)
     * @param afterDateMs only show events after this date (0 = no filter)
     * @return filtered and searched list of events
     */
    public List<EventSummary> applyAllFilters(String keyword, String category,
                                              String location, boolean openOnly,
                                              int maxCapacity, long afterDateMs) {
        List<EventSummary> results = new ArrayList<>(allEvents);

        // Keyword search
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lower = keyword.toLowerCase().trim();
            List<EventSummary> keywordFiltered = new ArrayList<>();
            for (EventSummary e : results) {
                if (e.getName().toLowerCase().contains(lower)
                        || e.getDescription().toLowerCase().contains(lower)
                        || e.getCategory().toLowerCase().contains(lower)) {
                    keywordFiltered.add(e);
                }
            }
            results = keywordFiltered;
        }

        // Category
        if (category != null && !category.equals("All")) {
            List<EventSummary> catFiltered = new ArrayList<>();
            for (EventSummary e : results) {
                if (e.getCategory().equalsIgnoreCase(category)) catFiltered.add(e);
            }
            results = catFiltered;
        }

        // Location
        if (location != null && !location.isEmpty()) {
            List<EventSummary> locFiltered = new ArrayList<>();
            for (EventSummary e : results) {
                if (e.getLocation().toLowerCase().contains(location.toLowerCase())) {
                    locFiltered.add(e);
                }
            }
            results = locFiltered;
        }

        // Open registration only
        if (openOnly) {
            List<EventSummary> openFiltered = new ArrayList<>();
            for (EventSummary e : results) {
                if (e.isRegistrationOpen()) openFiltered.add(e);
            }
            results = openFiltered;
        }

        // Max capacity
        if (maxCapacity > 0) {
            List<EventSummary> capFiltered = new ArrayList<>();
            for (EventSummary e : results) {
                if (e.getCapacity() == 0 || e.getCapacity() <= maxCapacity) {
                    capFiltered.add(e);
                }
            }
            results = capFiltered;
        }

        // After date
        if (afterDateMs > 0) {
            List<EventSummary> dateFiltered = new ArrayList<>();
            for (EventSummary e : results) {
                if (e.getEventDate() >= afterDateMs) dateFiltered.add(e);
            }
            results = dateFiltered;
        }

        return results;
    }

    public String getLotteryCriteria(EventSummary event) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "MMM dd, yyyy", java.util.Locale.getDefault());

        StringBuilder criteria = new StringBuilder();
        criteria.append("📋 Lottery Criteria\n\n");

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

        if (event.getCapacity() > 0) {
            criteria.append("👥 Waiting List Capacity:\n")
                    .append(event.getCapacity()).append(" entrants max")
                    .append("\n\n");
        } else {
            criteria.append("👥 Waiting List Capacity:\n")
                    .append("Unlimited")
                    .append("\n\n");
        }

        if (event.getDrawSize() > 0) {
            criteria.append("Number of Winners:\n")
                    .append(event.getDrawSize()).append(" entrants will be selected")
                    .append("\n\n");
        }

        criteria.append("Selection Process:\n")
                .append("Winners are chosen randomly and fairly from all entrants " +
                        "on the waiting list once registration closes. " +
                        "If a selected entrant declines, a replacement is drawn.");

        return criteria.toString();
    }
}
