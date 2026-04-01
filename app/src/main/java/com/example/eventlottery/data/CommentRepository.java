package com.example.eventlottery.data;

import com.example.eventlottery.domain.EventComment;

import java.util.List;

public interface CommentRepository {
    
    interface CommentCallback {
        void onSuccess(List<EventComment> comments);
        void onSuccess(EventComment comment);
        void onFailure(Exception e);
    }
    
    interface OperationCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
    
    void addComment(String eventId, EventComment comment, CommentCallback callback);
    void getComments(String eventId, CommentCallback callback);
    void deleteComment(String commentId, String eventId, CommentCallback callback);
}
