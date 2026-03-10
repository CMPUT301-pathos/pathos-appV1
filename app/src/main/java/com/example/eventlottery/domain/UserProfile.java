package com.example.eventlottery.domain;


import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.firebase.FirestoreProfileRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user profile in the application.
 * Stores personal information, role, and notification preferences.
 *
 * Extended to support notification opt-out preference.
 *
 * User stories supported:
 * - US 01.02.01/US 01.02.02: Provide/update personal info
 * - US 01.02.03: View event history
 * - US 01.02.04: Delete profile
 * - US 01.04.03: Opt out of receiving notifications
 *
 * @author Dmitriy Limanets, Fawaz Mansoor
 * @version 1.1
 * @see ProfileRepository
 * @see FirestoreProfileRepository
 */

public class UserProfile {
    private String deviceId;
    private String name;
    private String email;
    private String phoneNumber;
    private String role; //entrant, organizer, admin
    /**
     * List of event history records for this user.
     * Tracks all events the user has interacted with and their outcomes.
     * Used for US 01.02.03 - Event History display. -Hasrat
     */
    private List<EventHistoryRecord> eventHistory;
    private boolean notificationsEnabled = true; // opt-in by default
    /**
     * Empty constructor required by Firestore to deserialize documents.
     */
    public UserProfile() {
    }

    /**
     * Creates a user profile with a default role of "entrant".
     *
     * @param deviceId    the unique device identifier
     * @param name        the user's name
     * @param email       the user's email address
     * @param phoneNumber the user's phone number (optional)
     */
    public UserProfile(String deviceId, String name, String email, String phoneNumber) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = "entrant"; //for now have it as entrant on default
    }


    /**
     * Creates a user profile with a specified role.
     * Used for creating organizer or admin profiles.
     *
     * @param deviceId    the unique device identifier
     * @param name        the user's name
     * @param email       the user's email address
     * @param phoneNumber the user's phone number (optional)
     * @param role        the user's role ("entrant", "organizer", or "admin")
     */
    //This one is for creating an organizer and an admin
    public UserProfile(String deviceId, String name, String email, String phoneNumber, String role) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    //Getters and Setters

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    /**
     * author : Hasrat
     * Returns the user's event history list.
     * Initializes an empty list if null (prevents NullPointerException).
     *
     * @return List of EventHistoryRecord objects
     */
    public List<EventHistoryRecord> getEventHistory() {  // ← ADD THIS METHOD
        if (eventHistory == null) {
            eventHistory = new ArrayList<>();
        }
        return eventHistory;
    }

    /**
     * Sets the user's event history list.
     *
     * @param eventHistory the list of EventHistoryRecord objects
     */
    public void setEventHistory(List<EventHistoryRecord> eventHistory) {  // ← ADD THIS METHOD
        this.eventHistory = eventHistory;
    }

    /**
     * Helper method to add a single record to event history.
     * Automatically initializes the list if needed.
     *
     * @param record the EventHistoryRecord to add
     */
    public void addToHistory(EventHistoryRecord record) {  // ← ADD THIS METHOD
        if (eventHistory == null) {
            eventHistory = new ArrayList<>();
        }
        // Add to beginning for newest first
        eventHistory.add(0, record);
    }

    /**
     * Clears all event history for this user.
     * Useful when resetting or deleting account.
     */
    public void clearHistory() {  // ← ADD THIS METHOD
        if (eventHistory != null) {
            eventHistory.clear();
        }
    }
}