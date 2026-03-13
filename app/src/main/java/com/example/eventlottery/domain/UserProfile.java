package com.example.eventlottery.domain;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.firebase.FirestoreProfileRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user profile in the application.
 * Stores personal information, role, notification preferences,
 * and whether the required profile info has been completed.
 *
 * User stories supported:
 * - US 01.02.01: Provide personal info
 * - US 01.02.02: Update personal info
 * - US 01.02.03: View event history
 * - US 01.02.04: Delete profile
 * - US 01.04.03: Opt out of notifications
 * - US 01.07.01: Be identified by device
 *
 * @author Dmitriy Limanets, Fawaz Mansoor, Kenneth Joseph
 * @version 1.2
 * @see ProfileRepository
 * @see FirestoreProfileRepository
 */
public class UserProfile {
    private String deviceId;
    private String name;
    private String email;
    private String phoneNumber;
    private String role; // user, admin
    private boolean profileCompleted;
    private Boolean notificationsEnabled = true; // opt-in by default
    private String profilePhotoUri;

    /**
     * List of event history records for this user.
     * Tracks all events the user has interacted with and their outcomes.
     */
    private List<EventHistoryRecord> eventHistory;
    /**
     * Empty constructor required by Firestore to deserialize documents.
     */
    public UserProfile() {
    }

    /**
     * Creates a user profile with default role "entrant".
     * A user can exist by device ID even if their profile info is incomplete.
     *
     * @param deviceId    unique device identifier
     * @param name        user's name
     * @param email       user's email
     * @param phoneNumber user's phone number (optional)
     */
    public UserProfile(String deviceId, String name, String email, String phoneNumber) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = "user";
        this.profileCompleted = hasRequiredInfo();
    }

    /**
     * Creates a user profile with a specified role.
     *
     * @param deviceId    unique device identifier
     * @param name        user's name
     * @param email       user's email
     * @param phoneNumber user's phone number (optional)
     * @param role        user's role
     */
    public UserProfile(String deviceId, String name, String email, String phoneNumber, String role) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.profileCompleted = hasRequiredInfo();
    }

    //Getters and Setters

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        this.profileCompleted = hasRequiredInfo();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.profileCompleted = hasRequiredInfo();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.profileCompleted = hasRequiredInfo();
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

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled == null || notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getProfilePhotoUri() {
        return profilePhotoUri;
    }

    public void setProfilePhotoUri(String profilePhotoUri) {
        this.profilePhotoUri = profilePhotoUri;
    }

    /**
     * Returns true if required profile fields are filled.
     * Name and email are required. Phone number is optional.
     */
    public boolean hasRequiredInfo() {
        return deviceId != null && !deviceId.trim().isEmpty()
                && name != null && !name.trim().isEmpty()
                && email != null && !email.trim().isEmpty();
    }

    /**
     * Recalculates and updates whether the profile is complete.
     */
    public void refreshProfileCompleted() {
        this.profileCompleted = hasRequiredInfo();
    }

    /**
     * Returns the user's event history list.
     * Initializes an empty list if null.
     *
     * @return list of EventHistoryRecord objects
     */
    public List<EventHistoryRecord> getEventHistory() {
        if (eventHistory == null) {
            eventHistory = new ArrayList<>();
        }
        return eventHistory;
    }

    /**
     * Sets the user's event history list.
     *
     * @param eventHistory list of EventHistoryRecord objects
     */
    public void setEventHistory(List<EventHistoryRecord> eventHistory) {
        this.eventHistory = eventHistory;
    }

    /**
     * Adds a single record to event history.
     * Newest records are inserted first.
     *
     * @param record event history record to add
     */
    public void addToHistory(EventHistoryRecord record) {
        if (eventHistory == null) {
            eventHistory = new ArrayList<>();
        }
        eventHistory.add(0, record);
    }

    /**
     * Clears all event history for this user.
     */
    public void clearHistory() {
        if (eventHistory != null) {
            eventHistory.clear();
        }
    }
}