package com.example.eventlottery.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Event domain model for organizer-created events.
 *
 * Minimal fields for US 02.01.01:
 * - name, description
 * - organizerDeviceId
 * - optional posterUrl (can be null for now)
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class Event {

    private String id;                 // Firestore document id
    private String name;
    private String description;
    private String organizerDeviceId;
    private String posterUrl;          // optional for now

    // Firestore requires empty constructor
    public Event() {}

    public Event(String name, String description, String organizerDeviceId) {
        this.name = name;
        this.description = description;
        this.organizerDeviceId = organizerDeviceId;
        this.posterUrl = null;
    }

    // --- Getters/Setters ---
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

    /**
     *  for debug/logging.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("description", description);
        m.put("organizerDeviceId", organizerDeviceId);
        m.put("posterUrl", posterUrl);
        return m;
    }
}