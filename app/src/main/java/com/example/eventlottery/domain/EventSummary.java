package com.example.eventlottery.domain;

import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Lightweight model for showing events in lists.
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class EventSummary {
    private final String id;
    private final String name;
    private final String description;
    private final String location;
    private final long createdAt;
    private final String organizerDeviceId;

    public EventSummary(String id, String name, String description, String location,
                        long createdAt, String organizerDeviceId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.createdAt = createdAt;
        this.organizerDeviceId = organizerDeviceId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public long getCreatedAt() { return createdAt; }
    public String getOrganizerDeviceId() { return organizerDeviceId; }

    public static EventSummary fromDoc(DocumentSnapshot doc) {
        String id = doc.getId();
        String name = safe(doc.getString("name"));
        String desc = safe(doc.getString("description"));
        String loc = safe(doc.getString("location"));
        String organizer = safe(doc.getString("organizerDeviceId"));

        Long created = doc.getLong("createdAt");
        long createdAt = (created == null) ? 0L : created;

        return new EventSummary(id, name, desc, loc, createdAt, organizer);
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }
}