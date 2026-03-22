package com.example.eventlottery.data;

import com.example.eventlottery.domain.EventComment;

import java.util.List;

/**
 * Repository interface for event comments.
 *
 * User stories supported:
 * - US 01.08.01: Post comments on an event
 * - US 01.08.02: View comments on an event
 *
 * @author Edwin David
 * @version 1.0
 */
public interface CommentRepository {

    void addComment(EventComment comment, OperationCallback callback);

    void getCommentsByEvent(String eventId, CommentsCallback callback);

    void deleteComment(String commentId, OperationCallback callback);

    interface CommentsCallback {
        void onSuccess(List<EventComment> comments);
        void onFailure(Exception e);
    }

    interface OperationCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
