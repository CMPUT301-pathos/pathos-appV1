package com.example.eventlottery.domain;

import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Lightweight model for showing events in lists.
 *
 * Extended to support filtering by category, location, date,
 * registration availability, and lottery criteria.
 *
 * User stories supported:
 * - US 01.01.03: See a list of events to join the waiting list for
 * - US 01.01.04: Filter events based on interests and availability
 * - US 01.05.05: Be informed about lottery selection criteria
 *
 * @author Kenneth Joseph, Fawaz Mansoor
 * @version 1.2
 */
public class EventSummary {
    private final String id;
    private final String name;
    private final String description;
    private final String location;
    private final long createdAt;
    private final String organizerDeviceId;
    private final String category;
    private final long eventDate;
    private final long registrationStart;
    private final long registrationEnd;
    private final int capacity;
    private final int drawSize;
    private final String posterUrl;

    private boolean geoRequired;

    public boolean isGeoRequired() {
        return geoRequired;
    }

    public void setLocationRequired(boolean required) {
        this.geoRequired = required;
    }
    public EventSummary(String id, String name, String description, String location,
                        long createdAt, String organizerDeviceId, String category,
                        long eventDate, long registrationStart, long registrationEnd,
                        int capacity, int drawSize, String posterUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.createdAt = createdAt;
        this.organizerDeviceId = organizerDeviceId;
        this.category = category;
        this.eventDate = eventDate;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.capacity = capacity;
        this.drawSize = drawSize;
        this.posterUrl = posterUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public long getCreatedAt() { return createdAt; }
    public String getOrganizerDeviceId() { return organizerDeviceId; }
    public String getCategory() { return category; }
    public long getEventDate() { return eventDate; }
    public long getRegistrationStart() { return registrationStart; }
    public long getRegistrationEnd() { return registrationEnd; }
    public int getCapacity() { return capacity; }
    public int getDrawSize() { return drawSize; }
    public String getPosterUrl() {
        return posterUrl;
    }

    public boolean isRegistrationOpen() {
        if (registrationStart <= 0 || registrationEnd <= 0) {
            return false;
        }
        long now = System.currentTimeMillis();
        return now >= registrationStart && now <= registrationEnd;
    }

    public static EventSummary fromDoc(DocumentSnapshot doc) {
        String id = doc.getId();
        String name = safe(doc.getString("name"));
        String desc = safe(doc.getString("description"));
        String loc = safe(doc.getString("location"));
        String organizer = safe(doc.getString("organizerDeviceId"));
        String category = safe(doc.getString("category"));
        String posterUrl = doc.getString("posterUrl");

        Long created = safeGetLong(doc, "createdAt");
        Long evDate = safeGetLong(doc, "eventDate");
        Long regStart = safeGetLong(doc, "registrationStart");
        Long regEnd = safeGetLong(doc, "registrationEnd");
        Long cap = safeGetLong(doc, "capacity");
        Long draw = safeGetLong(doc, "drawSize");

        EventSummary e = new EventSummary(
                id, name, desc, loc,
                created == null ? 0L : created,
                organizer, category,
                evDate == null ? 0L : evDate,
                regStart == null ? 0L : regStart,
                regEnd == null ? 0L : regEnd,
                cap == null ? 0 : cap.intValue(),
                draw == null ? 0 : draw.intValue(),
                posterUrl
        );
        // set geoRequired after object is created
        Object geo = doc.get("geoRequired");
        if (geo instanceof Boolean) {
            e.geoRequired = (Boolean) geo;
        } else {
            e.geoRequired = false;
        }

        return e;
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    private static Long safeGetLong(DocumentSnapshot doc, String field) {
        try {
            return doc.getLong(field);
        } catch (Exception e) {
            return null;
        }
    }
}