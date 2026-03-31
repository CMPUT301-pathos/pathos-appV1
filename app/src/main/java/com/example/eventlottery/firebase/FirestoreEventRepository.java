package com.example.eventlottery.firebase;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.domain.EventSummary;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Firestore implementation of EventRepository.
 * Stores event documents in the "events" collection.
 *
 * Supports:
 * - creating events
 * - loading all events
 * - loading events for a specific organizer
 * - deleting a specific event by document ID
 *
 * @author Kenneth Joseph
 * @version 1.2
 */
public class FirestoreEventRepository implements EventRepository {

    private static final String COLLECTION = "events";
    private final FirebaseFirestore db;

    public FirestoreEventRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void createEvent(Object event, CreateCallback callback) {
        db.collection(COLLECTION)
                .add(event)
                .addOnSuccessListener(ref -> callback.onSuccess(ref.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getAllEvents(ListCallback callback) {
        db.collection(COLLECTION)
                .get()
                .addOnSuccessListener(snap -> {
                    List<EventSummary> out = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        out.add(EventSummary.fromDoc(doc));
                    }

                    Collections.sort(out,
                            Comparator.comparingLong(EventSummary::getCreatedAt).reversed());

                    callback.onSuccess(out);
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getEventsByOrganizer(String organizerDeviceId, ListCallback callback) {
        db.collection(COLLECTION)
                .whereEqualTo("organizerDeviceId", organizerDeviceId)
                .get()
                .addOnSuccessListener(snap -> {
                    List<EventSummary> out = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        out.add(EventSummary.fromDoc(doc));
                    }

                    Collections.sort(out,
                            Comparator.comparingLong(EventSummary::getCreatedAt).reversed());

                    callback.onSuccess(out);
                })
                .addOnFailureListener(callback::onFailure);
    }
    @Override
    public void getManageableEvents(String deviceId, ListCallback callback) {
        db.collection(COLLECTION)
                .get()
                .addOnSuccessListener(snap -> {
                    List<EventSummary> out = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snap) {
                        EventSummary event = EventSummary.fromDoc(doc);

                        // Core logic: organizer OR co-organizer
                        if (event != null && event.isUserOrganizer(deviceId)) {
                            out.add(event);
                        }
                    }

                    Collections.sort(out,
                            Comparator.comparingLong(EventSummary::getCreatedAt).reversed());

                    callback.onSuccess(out);
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void deleteEvent(String eventId, OperationCallback callback) {
        db.collection(COLLECTION)
                .document(eventId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public interface EventByIdCallback {
        void onResult(EventSummary event);
    }

    public void getEventById(String eventId, EventByIdCallback callback) {
        db.collection(COLLECTION)
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onResult(EventSummary.fromDoc(doc));
                    } else {
                        callback.onResult(null);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(null));
    }
}