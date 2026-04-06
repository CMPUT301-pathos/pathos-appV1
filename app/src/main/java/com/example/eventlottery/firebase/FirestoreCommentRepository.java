package com.example.eventlottery.firebase;

import android.util.Log;

import com.example.eventlottery.data.CommentRepository;
import com.example.eventlottery.domain.EventComment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore-backed implementation of {@link com.example.eventlottery.data.CommentRepository}.
 * Handles adding, listing, and deleting comments for event documents.
 */
public class FirestoreCommentRepository implements CommentRepository {
    
    private static final String TAG = "FirestoreCommentRepo";
    private final FirebaseFirestore db;
    private static final String EVENTS_COLLECTION = "events";
    private static final String COMMENTS_SUBCOLLECTION = "comments";
    
    public FirestoreCommentRepository() {
        this.db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Adds a new comment to the event's comments subcollection.
     * The comment is written as a plain Firestore document map.
     *
     * @param eventId  the event document ID
     * @param comment  the comment payload to save
     * @param callback callback that receives the saved comment or failure
     */
    @Override
    public void addComment(String eventId, EventComment comment, CommentCallback callback) {
        CollectionReference commentsRef = db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .collection(COMMENTS_SUBCOLLECTION);
        
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", comment.getEventId());
        data.put("userId", comment.getUserId());
        data.put("userName", comment.getUserName());
        data.put("content", comment.getContent());
        data.put("timestamp", comment.getTimestamp());
        data.put("isFlagged", comment.isFlagged());
        data.put("flagReason", comment.getFlagReason());
        
        commentsRef.add(data)
                .addOnSuccessListener(ref -> {
                    comment.setId(ref.getId());
                    callback.onSuccess(comment);
                })
                .addOnFailureListener(callback::onFailure);
    }
    
    /**
     * Retrieves comments for the specified event sorted by newest first.
     *
     * @param eventId  the event document ID
     * @param callback callback that receives the resulting comment list or failure
     */
    @Override
    public void getComments(String eventId, CommentCallback callback) {
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .collection(COMMENTS_SUBCOLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<EventComment> comments = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        EventComment comment = doc.toObject(EventComment.class);
                        comment.setId(doc.getId());
                        comments.add(comment);
                    }
                    callback.onSuccess(comments);
                })
                .addOnFailureListener(callback::onFailure);
    }
    
    /**
     * Deletes a comment from the event's comments subcollection.
     *
     * @param commentId the Firestore document ID of the comment
     * @param eventId   the parent event document ID
     * @param callback  callback invoked on success or failure
     */
    @Override
    public void deleteComment(String commentId, String eventId, CommentCallback callback) {
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .collection(COMMENTS_SUBCOLLECTION)
                .document(commentId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess((EventComment) null))
                .addOnFailureListener(callback::onFailure);
    }
}

