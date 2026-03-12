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
 * FirestoreEventRepository
 *
 * Implementation of {@link EventRepository} using Google Firebase Firestore.
 * Stores events in the "events" collection.
 *
 * Responsibilities:
 * - Create new events in Firestore
 * - Retrieve all events
 * - Retrieve events by organizer
 *
 * Sorting: Events are returned newest first based on {@link EventSummary#getCreatedAt()}.
 *
 * Example usage:
 * <pre>
 * EventRepository repo = new FirestoreEventRepository();
 * repo.getAllEvents(callback);
 * repo.createEvent(eventObj, createCallback);
 * </pre>
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


    /**
     * Creates a new event in the Firestore database.
     *
     * The event object is added to the events collection. If the operation
     * succeeds, the generated document ID is returned through the callback.
     * If the operation fails, the failure callback is triggered.
     *
     * @param event the event object to be stored in the database
     * @param callback callback used to return success or failure
     */
    @Override
    public void createEvent(Object event, CreateCallback callback) {
        db.collection(COLLECTION)
                .add(event)
                .addOnSuccessListener(ref -> callback.onSuccess(ref.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Retrieves all events stored in the Firestore collection.
     *
     * Each document is converted into an EventSummary object. The resulting
     * list is then sorted by creation time (newest first) before being
     * returned through the callback.
     *
     * @param callback callback that receives the list of events or an error
     */
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

    /**
     * Retrieves all events created by a specific organizer.
     *
     * The method queries Firestore for events matching the given organizer
     * device ID, converts each result to an EventSummary object, and sorts
     * the results by creation time (newest first).
     *
     * @param organizerDeviceId the device ID of the organizer
     * @param callback callback that receives the filtered list of events
     */
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