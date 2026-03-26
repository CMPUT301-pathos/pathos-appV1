package com.example.eventlottery.domain;

/**
 * Domain model representing an entrant's participation record for an event.
 *
 * Responsibilities:
 * - Store participation status (WAITING, INVITED, ACCEPTED, DECLINED, CANCELLED)
 * - Enforce valid state transitions for accept and decline actions
 *
 * User stories supported:
 * - US 01.05.02: Accept the invitation to register for an event
 * - US 01.05.03: Decline an invitation when chosen
 *
 * @author Fawaz Mansoor
 * @version 1.0
 */
public class WaitListRecord {
    private String eventId;
    private String deviceId;
    private WaitStatus status;
    private long joinTimeMs;

    private double latitude;
    private double longitude;

    public WaitListRecord(String eventId, String deviceId) {
        this(eventId, deviceId, 0, 0);
    }

    public WaitListRecord(String eventId, String deviceId, double latitude, double longitude) {
        this.eventId = eventId;
        this.deviceId = deviceId;
        this.status = WaitStatus.WAITING;
        this.joinTimeMs = System.currentTimeMillis();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void acceptInvitation() {
        if (this.status != WaitStatus.INVITED) {
            throw new IllegalStateException("Can only accept when INVITED");
        }
        this.status = WaitStatus.ACCEPTED;
    }

    public void declineInvitation() {
        if (this.status != WaitStatus.INVITED) {
            throw new IllegalStateException("Can only decline when INVITED");
        }
        this.status = WaitStatus.DECLINED;
    }

    // Getters
    public String getEventId() { return eventId; }
    public String getDeviceId() { return deviceId; }
    public WaitStatus getStatus() { return status; }
    public long getJoinTimeMs() { return joinTimeMs; }

    // Setter for status (used by repository when loading from Firestore)
    public void setStatus(WaitStatus status) { this.status = status; }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
