package com.example.eventlottery.firebase;

import com.example.eventlottery.data.CommentRepository;
import com.example.eventlottery.domain.EventComment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore implementation of CommentRepository.
 * Stores comments in Firestore collection: "comments".

 * User stories supported:
 * - US 01.08.01: Post a comment on an event
 * - US 01.08.02: View comments on an event
 *
 * @author Edwin David
 * @version 1.0
 */
public class FirestoreCommentRepository implements CommentRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION = "comments";

    public FirestoreCommentRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void addComment(EventComment comment, OperationCallback callback) {
        // Build a map of fields to store in Firestore
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", comment.getEventId());
        data.put("deviceId", comment.getDeviceId());
        data.put("authorName", comment.getAuthorName());
        data.put("text", comment.getText());
        data.put("createdAt", comment.getCreatedAt());

        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(ref -> {

                    // Store the Firestore document ID onto the comment object
                    comment.setCommentId(ref.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getCommentsByEvent(String eventId, CommentsCallback callback) {
        db.collection(COLLECTION)
                .whereEqualTo("eventId", eventId) // filter to this event only
                .orderBy("createdAt", Query.Direction.ASCENDING) // oldest comment first
                .get()
                .addOnSuccessListener(snap -> {
                    List<EventComment> comments = new ArrayList<>();

                    // Change each Firestore document into an EventComment object
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        EventComment c = new EventComment(
                                doc.getString("eventId"),
                                doc.getString("deviceId"),
                                doc.getString("authorName"),
                                doc.getString("text")
                        );
                        c.setCommentId(doc.getId());
                        Long createdAt = doc.getLong("createdAt"); //may be null if the document is malformed
                        if (createdAt != null) c.setCreatedAt(createdAt);
                        comments.add(c);
                    }
                    callback.onSuccess(comments);
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void deleteComment(String commentId, OperationCallback callback) {
        db.collection(COLLECTION)
                .document(commentId) // locate the specific comment document
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}
