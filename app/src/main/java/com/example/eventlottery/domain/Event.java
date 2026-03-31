package com.example.eventlottery.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Event domain model for organizer-created events.
 *
 * Responsibilities:
 * - store event details such as name, description, organizer, dates, and capacity
 * - store event configuration options such as geolocation requirement
 * - store co-organizer device IDs for shared event management
 * - convert event data into a Firestore-friendly map
 *
 * User stories supported:
 * - US 02.01.01: Create a new public event with a promotional QR code
 * - US 02.01.02: Create a private event
 * - US 02.01.04: Set a registration period
 * - US 02.02.03: Enable or disable the geolocation requirement for an event
 * - US 02.09.01: Add co-organizers to an event
 *
 * @author Kenneth Joseph, Fawaz Mansoor, Hasratsinghchauhan
 * @version 1.6
 */
public class Event {

    private String id;
    private String name;
    private String description;
    private String organizerDeviceId;
    private String posterUrl;
    private String category;
    private String location;
    private Object createdAt;
    private Object eventDate;
    private Object registrationStart;
    private Object registrationEnd;

    private int capacity;
    private int drawSize;

    /**
     * Whether entrants must provide device location when joining this event's waiting list.
     */
    private boolean requiresGeolocation;

    /**
     * Device IDs for co-organizers assigned to this event.
     */
    private List<String> coOrganizerIds;

    public Event() {
        this.coOrganizerIds = new ArrayList<>();
    }

    public Event(String name, String description, String organizerDeviceId) {
        this.name = name;
        this.description = description;
        this.organizerDeviceId = organizerDeviceId;
        this.posterUrl = null;
        this.requiresGeolocation = false;
        this.coOrganizerIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrganizerDeviceId() {
        return organizerDeviceId;
    }

    public void setOrganizerDeviceId(String organizerDeviceId) {
        this.organizerDeviceId = organizerDeviceId;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }

    public void setEventDate(Object eventDate) {
        this.eventDate = eventDate;
    }

    public long getEventDate() {
        return convertToLong(eventDate);
    }

    public void setRegistrationStart(Object registrationStart) {
        this.registrationStart = registrationStart;
    }

    public long getRegistrationStart() {
        return convertToLong(registrationStart);
    }

    public void setRegistrationEnd(Object registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    public long getRegistrationEnd() {
        return convertToLong(registrationEnd);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getDrawSize() {
        return drawSize;
    }

    public void setDrawSize(int drawSize) {
        this.drawSize = drawSize;
    }

    public boolean isGeolocationRequired() {
        return requiresGeolocation;
    }

    public void setRequiresGeolocation(boolean requiresGeolocation) {
        this.requiresGeolocation = requiresGeolocation;
    }

    public List<String> getCoOrganizerIds() {
        return new ArrayList<>(coOrganizerIds);
    }

    public void setCoOrganizerIds(List<String> coOrganizerIds) {
        if (coOrganizerIds == null) {
            this.coOrganizerIds = new ArrayList<>();
        } else {
            this.coOrganizerIds = new ArrayList<>(coOrganizerIds);
        }
    }

    public boolean isRegistrationOpen() {
        long now = System.currentTimeMillis();
        return now >= getRegistrationStart() && now <= getRegistrationEnd();
    }

    private long convertToLong(Object value) {
        if (value == null) return 0;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("description", description);
        m.put("organizerDeviceId", organizerDeviceId);
        m.put("posterUrl", posterUrl);
        m.put("category", category);
        m.put("location", location);
        m.put("createdAt", createdAt);
        m.put("eventDate", eventDate);
        m.put("registrationStart", registrationStart);
        m.put("registrationEnd", registrationEnd);
        m.put("capacity", capacity);
        m.put("drawSize", drawSize);
        m.put("requiresGeolocation", requiresGeolocation);
        m.put("coOrganizerIds", new ArrayList<>(coOrganizerIds));
        return m;
    }
}