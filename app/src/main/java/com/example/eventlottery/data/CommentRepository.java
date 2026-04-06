package com.example.eventlottery.data;

import com.example.eventlottery.domain.EventComment;

import java.util.List;

public interface CommentRepository {
    
    interface CommentCallback {
        /**
         * Called when a list of comments is successfully retrieved.
         *
         * @param comments the retrieved list of event comments
         */
        void onSuccess(List<EventComment> comments);

        /**
         * Called when a single comment operation succeeds.
         *
         * @param comment the comment object returned by the operation
         */
        void onSuccess(EventComment comment);

        /**
         * Called when a comment operation fails.
         *
         * @param e the exception describing the failure
         */
        void onFailure(Exception e);
    }
    
    interface OperationCallback {
        /**
         * Called when an operation completes successfully.
         */
        void onSuccess();

        /**
         * Called when an operation fails.
         *
         * @param e the exception describing the failure
         */
        void onFailure(Exception e);
    }
    
    /**
     * Adds a new comment to the specified event.
     *
     * @param eventId the event identifier
     * @param comment the comment to add
     * @param callback callback for success or failure
     */
    void addComment(String eventId, EventComment comment, CommentCallback callback);

    /**
     * Retrieves comments for the specified event.
     *
     * @param eventId the event identifier
     * @param callback callback for success or failure
     */
    void getComments(String eventId, CommentCallback callback);

    /**
     * Deletes a comment from an event.
     *
     * @param commentId the comment document identifier
     * @param eventId the event identifier
     * @param callback callback for success or failure
     */
    void deleteComment(String commentId, String eventId, CommentCallback callback);
}
