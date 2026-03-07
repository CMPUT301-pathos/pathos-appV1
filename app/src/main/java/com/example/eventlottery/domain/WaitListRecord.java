package com.example.eventlottery.domain;

public class WaitListRecord {
    private String eventId;
    private String deviceId;
    private WaitStatus status;
    private long joinTimeMs;

    public WaitListRecord(String eventId, String deviceId) {
        this.eventId = eventId;
        this.deviceId = deviceId;
        this.status = WaitStatus.WAITING;
        this.joinTimeMs = System.currentTimeMillis();
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
}
