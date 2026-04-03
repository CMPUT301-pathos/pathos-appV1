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

public class FirestoreCommentRepository implements CommentRepository {
    
    private static final String TAG = "FirestoreCommentRepo";
    private final FirebaseFirestore db;
    private static final String EVENTS_COLLECTION = "events";
    private static final String COMMENTS_SUBCOLLECTION = "comments";
    
    public FirestoreCommentRepository() {
        this.db = FirebaseFirestore.getInstance();
    }
    
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
