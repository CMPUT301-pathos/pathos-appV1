package com.example.eventlottery.firebase;

import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore implementation of {@link WaitListRepository}.
 * Stores participation records in collection: "waitlist".
 * Document IDs follow the format: {eventId}_{deviceId}.
 *
 * Responsibilities:
 * - Persist entrant participation records to Firestore
 * - Persist optional join geolocation metadata for entrant records
 * - Update participation status in Firestore
 * - Provide async queries for lottery draw operations
 *
 * User stories supported:
 * - US 01.01.01: Join the waiting list for a specific event
 * - US 01.01.02: Leave the waiting list for a specific event
 * - US 01.05.01: Another chance to be chosen when someone declines
 * - US 01.05.02: Accept the invitation to register for an event
 * - US 01.05.03: Decline an invitation when chosen
 * - US 02.02.02: View on a map where entrants joined from
 * - US 02.05.02: Sample a specified number of attendees
 * - US 02.05.03: Draw a replacement applicant
 *
 * @author Fawaz Mansoor, Dmitriy Limanets, Kenneth Joseph
 * @version 1.2
 */
public class FirestoreWaitListRepository implements WaitListRepository {

    private static final String COLLECTION = "waitlist";

    private static final String FIELD_EVENT_ID = "eventId";
    private static final String FIELD_DEVICE_ID = "deviceId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_JOIN_TIME_MS = "joinTimeMs";
    private static final String FIELD_NOTIFIED = "notified";
    private static final String FIELD_JOIN_LATITUDE = "joinLatitude";
    private static final String FIELD_JOIN_LONGITUDE = "joinLongitude";
    private static final String FIELD_JOIN_ACCURACY_METERS = "joinAccuracyMeters";
    private static final String FIELD_JOIN_LOCATION_TIMESTAMP_MS = "joinLocationTimestampMs";

    private final FirebaseFirestore db;

    public FirestoreWaitListRepository() {
        this.db = FirebaseFirestore.getInstance();
    }
    // For unit tests
    public FirestoreWaitListRepository(FirebaseFirestore db) {
        this.db = db;
    }



    @Override
    public void addToWaitList(WaitListRecord record) {
        db.collection(COLLECTION)
                .document(buildDocumentId(record.getEventId(), record.getDeviceId()))
                .set(toMap(record));
    }

    @Override
    public void removeFromWaitList(String eventId, String deviceId) {
        db.collection(COLLECTION)
                .document(buildDocumentId(eventId, deviceId))
                .delete();
    }

    @Override
    public void updateStatus(String eventId, String deviceId, WaitStatus newStatus) {
        db.collection(COLLECTION)
                .document(buildDocumentId(eventId, deviceId))
                .update(FIELD_STATUS, newStatus.name());
    }

    @Override
    public WaitListRecord getRecord(String eventId, String deviceId) {
        return null;
    }

    @Override
    public List<WaitListRecord> getRecordsByEvent(String eventId) {
        return new ArrayList<>();
    }

    @Override
    public List<WaitListRecord> getRecordsByStatus(String eventId, WaitStatus status) {
        return new ArrayList<>();
    }

    @Override
    public void getRecordsByStatusAsync(String eventId,
                                        WaitStatus status,
                                        WaitListCallBack callback) {
        db.collection(COLLECTION)
                .whereEqualTo(FIELD_EVENT_ID, eventId)
                .whereEqualTo(FIELD_STATUS, status.name())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<WaitListRecord> records = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        records.add(fromDocument(doc));
                    }
                    callback.onSuccess(records);
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getRecordAsync(String eventId,
                               String deviceId,
                               SingleRecordCallback callback) {
        db.collection(COLLECTION)
                .document(buildDocumentId(eventId, deviceId))
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onSuccess(fromDocument(doc));
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void addToWaitList(WaitListRecord record, OperationCallback callback) {
        db.collection(COLLECTION)
                .document(buildDocumentId(record.getEventId(), record.getDeviceId()))
                .set(toMap(record))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void removeFromWaitList(String eventId,
                                   String deviceId,
                                   OperationCallback callback) {
        db.collection(COLLECTION)
                .document(buildDocumentId(eventId, deviceId))
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getRecordsByEventAsync(String eventId, WaitListCallBack callback) {
        db.collection(COLLECTION)
                .whereEqualTo(FIELD_EVENT_ID, eventId)
                .get()
                .addOnSuccessListener(snap -> {
                    List<WaitListRecord> records = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        records.add(fromDocument(doc));
                    }
                    callback.onSuccess(records);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getRecordsForDevice(String deviceId, WaitListCallBack callback) {
        db.collection(COLLECTION)
                .whereEqualTo(FIELD_DEVICE_ID, deviceId)
                .get()
                .addOnSuccessListener(snap -> {
                    List<WaitListRecord> records = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        records.add(fromDocument(doc));
                    }
                    callback.onSuccess(records);
                })
                .addOnFailureListener(callback::onFailure);
    }

    private String buildDocumentId(String eventId, String deviceId) {
        return eventId + "_" + deviceId;
    }

    private Map<String, Object> toMap(WaitListRecord record) {
        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_EVENT_ID, record.getEventId());
        data.put(FIELD_DEVICE_ID, record.getDeviceId());
        data.put(FIELD_STATUS, record.getStatus().name());
        data.put(FIELD_JOIN_TIME_MS, record.getJoinTimeMs());
        data.put(FIELD_NOTIFIED, false);
        data.put(FIELD_JOIN_LATITUDE, record.getJoinLatitude());
        data.put(FIELD_JOIN_LONGITUDE, record.getJoinLongitude());
        data.put(FIELD_JOIN_ACCURACY_METERS, record.getJoinAccuracyMeters());
        data.put(FIELD_JOIN_LOCATION_TIMESTAMP_MS, record.getJoinLocationTimestampMs());
        return data;
    }

    private WaitListRecord fromDocument(DocumentSnapshot doc) {
        WaitListRecord record = new WaitListRecord(
                doc.getString(FIELD_EVENT_ID),
                doc.getString(FIELD_DEVICE_ID)
        );

        String statusStr = doc.getString(FIELD_STATUS);
        if (statusStr != null) {
            try {
                record.setStatus(WaitStatus.valueOf(statusStr));
            } catch (IllegalArgumentException ignored) {
                record.setStatus(WaitStatus.WAITING);
            }
        }

        Long joinTimeMs = getLongValue(doc, FIELD_JOIN_TIME_MS);
        if (joinTimeMs != null) {
            record.setJoinTimeMs(joinTimeMs);
        }

        record.setJoinLatitude(getDoubleValue(doc, FIELD_JOIN_LATITUDE));
        record.setJoinLongitude(getDoubleValue(doc, FIELD_JOIN_LONGITUDE));
        record.setJoinAccuracyMeters(getFloatValue(doc, FIELD_JOIN_ACCURACY_METERS));
        record.setJoinLocationTimestampMs(getLongValue(doc, FIELD_JOIN_LOCATION_TIMESTAMP_MS));

        return record;
    }

    private Long getLongValue(DocumentSnapshot doc, String field) {
        Object value = doc.get(field);

        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Double) {
            return ((Double) value).longValue();
        }
        return null;
    }

    private Double getDoubleValue(DocumentSnapshot doc, String field) {
        Object value = doc.get(field);

        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        return null;
    }

    private Float getFloatValue(DocumentSnapshot doc, String field) {
        Object value = doc.get(field);

        if (value instanceof Float) {
            return (Float) value;
        }
        if (value instanceof Double) {
            return ((Double) value).floatValue();
        }
        if (value instanceof Long) {
            return ((Long) value).floatValue();
        }
        if (value instanceof Integer) {
            return ((Integer) value).floatValue();
        }
        return null;
    }
}