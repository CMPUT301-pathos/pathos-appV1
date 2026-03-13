package com.example.eventlottery.domain;

/**
 * Model for policy violation records
 */
public class PolicyViolation {
    private String userId;
    private String userName;
    private String userEmail;
    private long deletedAt;
    private String deletedBy;
    private String reason;

    public PolicyViolation() {
        // Required for Firestore
    }

    public PolicyViolation(String userId, String userName, String userEmail,
                           String deletedBy, String reason) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.deletedAt = System.currentTimeMillis();
        this.deletedBy = deletedBy;
        this.reason = reason;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public long getDeletedAt() { return deletedAt; }
    public void setDeletedAt(long deletedAt) { this.deletedAt = deletedAt; }

    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
