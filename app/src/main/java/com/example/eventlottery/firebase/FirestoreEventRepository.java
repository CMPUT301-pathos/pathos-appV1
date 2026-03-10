package com.example.eventlottery.firebase;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.domain.Event;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Firestore implementation of EventRepository.
 * Stores events in collection: "events"
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class FirestoreEventRepository implements EventRepository {

    private static final String COLLECTION = "events";
    private final FirebaseFirestore db;

    public FirestoreEventRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void createEvent(Event event, CreateCallback callback) {
        // Auto-generate doc id
        db.collection(COLLECTION)
                .add(event)
                .addOnSuccessListener(ref -> callback.onSuccess(ref.getId()))
                .addOnFailureListener(callback::onFailure);
    }
}