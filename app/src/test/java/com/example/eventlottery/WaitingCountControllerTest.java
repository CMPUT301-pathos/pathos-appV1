package com.example.eventlottery;

import static org.junit.Assert.*;

import com.example.eventlottery.controller.WaitingListController;
import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for US 01.05.04 - Know how many entrants are on the waiting list.
 *
 * @author Edwin
 * @version 1.0
 */
class FakeWaitListRepository implements WaitListRepository {

    private final List<WaitListRecord> records;

    FakeWaitListRepository(List<WaitListRecord> records) {
        this.records = records;
    }

    @Override
    public void getRecordsByStatusAsync(String eventId, WaitStatus status, WaitListCallBack callback) {
        callback.onSuccess(records);
    }

    @Override public void addToWaitList(WaitListRecord r) {}
    @Override public void addToWaitList(WaitListRecord r, OperationCallback callback) { callback.onSuccess(); }
    @Override public void removeFromWaitList(String eId, String dId) {}
    @Override public void removeFromWaitList(String eId, String dId, OperationCallback callback) { callback.onSuccess(); }
    @Override public void updateStatus(String eId, String dId, WaitStatus s) {}
    @Override public WaitListRecord getRecord(String eId, String dId) { return null; }
    @Override public void getRecordAsync(String eId, String dId, SingleRecordCallback callback) { callback.onSuccess(null); }
    @Override public List<WaitListRecord> getRecordsByEvent(String eId) { return new ArrayList<>(); }
    @Override public List<WaitListRecord> getRecordsByStatus(String eId, WaitStatus s) { return new ArrayList<>(); }
    @Override
    public void getRecordsByEventAsync(String eventId, WaitListCallBack callback) {
        callback.onSuccess(records);
    }
}
