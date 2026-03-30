package com.example.eventlottery.domain;

/**
 * Domain model representing an entrant's participation record for an event.
 *
 * Responsibilities:
 * - store participation status (WAITING, INVITED, ACCEPTED, DECLINED, CANCELLED)
 * - store when the entrant joined the waiting list
 * - optionally store the location the entrant joined from
 * - enforce valid state transitions for accept and decline actions
 *
 * User stories supported:
 * - US 01.01.01: Join the waiting list for a specific event
 * - US 01.05.02: Accept the invitation to register for an event
 * - US 01.05.03: Decline an invitation when chosen
 * - US 02.02.02: Organizer sees on a map where entrants joined from
 *
 * @author Fawaz Mansoor
 * @modified: Kenneth joseph
 * @version 1.1
 */
public class WaitListRecord {

    private String eventId;
    private String deviceId;
    private WaitStatus status;
    private long joinTimeMs;
    private Double joinLatitude;
    private Double joinLongitude;
    private Float joinAccuracyMeters;
    private Long joinLocationTimestampMs;

    public WaitListRecord() {
        this.status = WaitStatus.WAITING;
        this.joinTimeMs = System.currentTimeMillis();
    }

    public WaitListRecord(String eventId, String deviceId) {
        this();
        this.eventId = eventId;
        this.deviceId = deviceId;
    }

    public WaitListRecord(String eventId,
                          String deviceId,
                          Double joinLatitude,
                          Double joinLongitude,
                          Float joinAccuracyMeters,
                          Long joinLocationTimestampMs) {
        this(eventId, deviceId);
        this.joinLatitude = joinLatitude;
        this.joinLongitude = joinLongitude;
        this.joinAccuracyMeters = joinAccuracyMeters;
        this.joinLocationTimestampMs = joinLocationTimestampMs;
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

    public void setJoinLocation(Double joinLatitude,
                                Double joinLongitude,
                                Float joinAccuracyMeters,
                                Long joinLocationTimestampMs) {
        this.joinLatitude = joinLatitude;
        this.joinLongitude = joinLongitude;
        this.joinAccuracyMeters = joinAccuracyMeters;
        this.joinLocationTimestampMs = joinLocationTimestampMs;
    }

    public void clearJoinLocation() {
        this.joinLatitude = null;
        this.joinLongitude = null;
        this.joinAccuracyMeters = null;
        this.joinLocationTimestampMs = null;
    }

    public boolean hasJoinLocation() {
        return joinLatitude != null && joinLongitude != null;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public WaitStatus getStatus() {
        return status;
    }

    public void setStatus(WaitStatus status) {
        this.status = status;
    }

    public long getJoinTimeMs() {
        return joinTimeMs;
    }

    public void setJoinTimeMs(long joinTimeMs) {
        this.joinTimeMs = joinTimeMs;
    }

    public Double getJoinLatitude() {
        return joinLatitude;
    }

    public void setJoinLatitude(Double joinLatitude) {
        this.joinLatitude = joinLatitude;
    }

    public Double getJoinLongitude() {
        return joinLongitude;
    }

    public void setJoinLongitude(Double joinLongitude) {
        this.joinLongitude = joinLongitude;
    }

    public Float getJoinAccuracyMeters() {
        return joinAccuracyMeters;
    }

    public void setJoinAccuracyMeters(Float joinAccuracyMeters) {
        this.joinAccuracyMeters = joinAccuracyMeters;
    }

    public Long getJoinLocationTimestampMs() {
        return joinLocationTimestampMs;
    }

    public void setJoinLocationTimestampMs(Long joinLocationTimestampMs) {
        this.joinLocationTimestampMs = joinLocationTimestampMs;
    }
}