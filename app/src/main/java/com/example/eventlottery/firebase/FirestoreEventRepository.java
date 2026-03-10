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
 * Stores events in collection: "events"
 *
 * @author Kenneth Joseph
 * @version 1.1
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
                    // Sort newest first if createdAt exists
                    Collections.sort(out, Comparator.comparingLong(EventSummary::getCreatedAt).reversed());
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
                    // Sort newest first in-memory (avoids Firestore composite index requirement)
                    Collections.sort(out, Comparator.comparingLong(EventSummary::getCreatedAt).reversed());
                    callback.onSuccess(out);
                })
                .addOnFailureListener(callback::onFailure);
    }
}