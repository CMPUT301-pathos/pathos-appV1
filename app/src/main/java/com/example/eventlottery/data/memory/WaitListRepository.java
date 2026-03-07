package com.example.eventlottery.data.memory;

import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import java.util.List;

public interface WaitListRepository {
    void addToWaitList(WaitListRecord record);
    void removeFromWaitList(String eventId, String deviceId);
    void updateStatus(String eventId, String deviceId, WaitStatus newStatus);
    WaitListRecord getRecord(String eventId, String deviceId);
    List<WaitListRecord> getRecordsByEvent(String eventId);
    List<WaitListRecord> getRecordsByStatus(String eventId, WaitStatus status);
}