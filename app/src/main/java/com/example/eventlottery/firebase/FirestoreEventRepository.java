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
    public void deleteEvent(String eventId, OperationCallback callback) {
        db.collection(COLLECTION)
                .document(eventId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void updateGeoRequirement(String eventId, boolean required, OperationCallback callback) {
        db.collection(COLLECTION)
                .document(eventId)
                .update("geoRequired", required)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}