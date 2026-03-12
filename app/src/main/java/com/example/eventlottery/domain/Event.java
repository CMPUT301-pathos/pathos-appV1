package com.example.eventlottery.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Event domain model for organizer-created events.
 *
 * Extended to support category, location, eventDate,
 * registrationStart, and registrationEnd fields.
 *
 * User stories supported:
 * - US 02.01.01: Create a new event
 * - US 02.01.04: Set a registration period
 * - US 01.01.04: Filter events based on interests and availability
 *
 * @author Kenneth Joseph, Fawaz Mansoor, Hasratsinghchauhan
 * @version 1.2
 */
/*public class Event {

    private String id;
    private String name;
    private String description;
    private String organizerDeviceId;
    private String posterUrl;
    private String category;
    private String location;
    private long eventDate;           // when the event takes place (ms)
    private long registrationStart;   // when registration opens (ms)
    private int capacity;     // max entrants on waiting list (0 = unlimited)
    private int drawSize;     // how many will be selected in the lottery
    private Object registrationEnd;     // when registration closes (ms) - can be String or Long

    public Event() {}

    public Event(String name, String description, String organizerDeviceId) {
        this.name = name;
        this.description = description;
        this.organizerDeviceId = organizerDeviceId;
        this.posterUrl = null;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOrganizerDeviceId() { return organizerDeviceId; }
    public void setOrganizerDeviceId(String organizerDeviceId) { this.organizerDeviceId = organizerDeviceId; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public long getEventDate() { return eventDate; }
    public void setEventDate(long eventDate) { this.eventDate = eventDate; }

    public long getRegistrationStart() { return registrationStart; }
    public void setRegistrationStart(long registrationStart) { this.registrationStart = registrationStart; }

    // Special getter that converts Object to long safely, as there were poroblems with firebase compatibility and adminmainactivity
    public long getRegistrationEnd() {
        if (registrationEnd instanceof Long) {
            return (Long) registrationEnd;
        } else if (registrationEnd instanceof String) {
            try {
                return Long.parseLong((String) registrationEnd);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    public void setRegistrationEnd(long registrationEnd) { this.registrationEnd = registrationEnd; }
    //   Setter for Object (used by Firestore)
    public void setRegistrationEnd(Object registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getDrawSize() { return drawSize; }
    public void setDrawSize(int drawSize) { this.drawSize = drawSize; }

    public boolean isRegistrationOpen() {
        long now = System.currentTimeMillis();
        return now >= registrationStart && now <= getRegistrationEnd();
    }
    /**
     * Converts the Event object into a key-value map representation.
     *
     * The map contains all event properties and is typically used for
     * storing or updating event data in a database such as Firestore.
     *
     * @return a Map containing the event's fields and their corresponding values
     */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("description", description);
        m.put("organizerDeviceId", organizerDeviceId);
        m.put("posterUrl", posterUrl);
        m.put("category", category);
        m.put("location", location);
        m.put("eventDate", eventDate);
        m.put("registrationStart", registrationStart);
        m.put("registrationEnd", registrationEnd);
        m.put("capacity", capacity);
        m.put("drawSize", drawSize);
        return m;
    }
}*/
import java.util.HashMap;
import java.util.Map;

/**
 * Event domain model for organizer-created events.
 *
 * @author Kenneth Joseph, Fawaz Mansoor, Hasratsinghchauhan
 * @version 1.4
 */
public class Event {

    private String id;
    private String name;
    private String description;
    private String organizerDeviceId;
    private String posterUrl;
    private String category;
    private String location;

    // ADD THIS MISSING FIELD
    private Object createdAt;  // Firestore has this field!

    // Use Object for ALL date fields
    private Object eventDate;
    private Object registrationStart;
    private Object registrationEnd;

    private int capacity;
    private int drawSize;

    public Event() {}

    public Event(String name, String description, String organizerDeviceId) {
        this.name = name;
        this.description = description;
        this.organizerDeviceId = organizerDeviceId;
        this.posterUrl = null;
    }

    // Getters and setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOrganizerDeviceId() { return organizerDeviceId; }
    public void setOrganizerDeviceId(String organizerDeviceId) { this.organizerDeviceId = organizerDeviceId; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    // ADD GETTER/SETTER FOR createdAt
    public Object getCreatedAt() { return createdAt; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }

    // Event Date methods
    public void setEventDate(Object eventDate) {
        this.eventDate = eventDate;
    }

    public long getEventDate() {
        return convertToLong(eventDate);
    }

    // Registration Start methods
    public void setRegistrationStart(Object registrationStart) {
        this.registrationStart = registrationStart;
    }

    public long getRegistrationStart() {
        return convertToLong(registrationStart);
    }

    // Registration End methods
    public void setRegistrationEnd(Object registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    public long getRegistrationEnd() {
        return convertToLong(registrationEnd);
    }

    // Helper method to convert Object to long safely
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

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getDrawSize() { return drawSize; }
    public void setDrawSize(int drawSize) { this.drawSize = drawSize; }

    public boolean isRegistrationOpen() {
        long now = System.currentTimeMillis();
        return now >= getRegistrationStart() && now <= getRegistrationEnd();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("description", description);
        m.put("organizerDeviceId", organizerDeviceId);
        m.put("posterUrl", posterUrl);
        m.put("category", category);
        m.put("location", location);
        m.put("createdAt", createdAt);  // ADD THIS
        m.put("eventDate", eventDate);
        m.put("registrationStart", registrationStart);
        m.put("registrationEnd", registrationEnd);
        m.put("capacity", capacity);
        m.put("drawSize", drawSize);
        return m;
    }
}