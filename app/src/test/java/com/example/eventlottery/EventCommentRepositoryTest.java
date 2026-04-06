package com.example.eventlottery;

import com.example.eventlottery.data.CommentRepository;
import com.example.eventlottery.domain.EventComment;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for comment repository using a fake repository.
 *
 * User stories covered:
 * - US 01.08.01: Post comments on an event
 * - US 01.08.02: View comments on an event
 * - US 02.08.01: Organizer can delete entrant comments
 *
 * @author Edwin David
 * @version 1.1
 */
public class EventCommentRepositoryTest {

    private static class FakeCommentRepository implements CommentRepository {
        private final List<EventComment> store = new ArrayList<>();
        private int nextId = 0;

        @Override
        public void addComment(EventComment comment, OperationCallback callback) {
            comment.setCommentId("comment_" + nextId++);
            store.add(comment);
            callback.onSuccess();
        }

        @Override
        public void getCommentsByEvent(String eventId, CommentsCallback callback) {
            List<EventComment> result = new ArrayList<>();
            for (EventComment c : store) {
                if (eventId.equals(c.getEventId())) result.add(c);
            }
            callback.onSuccess(result);
        }

        @Override
        public void deleteComment(String commentId, OperationCallback callback) {
            store.removeIf(c -> commentId.equals(c.getCommentId()));
            callback.onSuccess();
        }
    }

    private FakeCommentRepository repo;

    @Before
    public void setUp() {
        repo = new FakeCommentRepository();
    }

    //Testing US 01.08.01

    /**
     * Verifies that adding a comment stores it in the repository and
     * can be retrieved with its original text content.
     */
    @Test
    public void addComment_storesComment() {
        // Checks that after posting, the comment is retrievable with correct content
        EventComment c = new EventComment("event1", "device1", "Alice", "Hello!");

        repo.addComment(c, new CommentRepository.OperationCallback() {
            @Override public void onSuccess() {}
            @Override public void onFailure(Exception e) { fail(); }
        });

        repo.getCommentsByEvent("event1", new CommentRepository.CommentsCallback() {
            @Override
            public void onSuccess(List<EventComment> comments) {
                assertEquals(1, comments.size());
                assertEquals("Hello!", comments.get(0).getText());
            }
            @Override public void onFailure(Exception e) { fail(); }
        });
    }

    // Testing US 01.08.02

    /**
     * Verifies that getCommentsByEvent() returns only comments for the specified event,
     * filtering out comments from other events.
     */
    @Test
    public void getCommentsByEvent_returnsOnlyCommentsForThatEvent() {
        // Checks that fetching event1's comments does not include event2's comments
        repo.addComment(new EventComment("event1", "d1", "Alice", "For event 1"), new CommentRepository.OperationCallback() {
            @Override public void onSuccess() {}
            @Override public void onFailure(Exception e) {}
        });
        repo.addComment(new EventComment("event2", "d2", "Bob", "For event 2"), new CommentRepository.OperationCallback() {
            @Override public void onSuccess() {}
            @Override public void onFailure(Exception e) {}
        });

        repo.getCommentsByEvent("event1", new CommentRepository.CommentsCallback() {
            @Override
            public void onSuccess(List<EventComment> comments) {
                assertEquals(1, comments.size());
                assertEquals("Alice", comments.get(0).getAuthorName());
            }
            @Override public void onFailure(Exception e) { fail(); }
        });
    }

    /**
     * Verifies that getCommentsByEvent() returns an empty list for events
     * that have no comments without crashing.
     */
    @Test
    public void getCommentsByEvent_noComments_returnsEmpty() {
        //Event with no comments returns an empty list
        repo.getCommentsByEvent("event1", new CommentRepository.CommentsCallback() {
            @Override
            public void onSuccess(List<EventComment> comments) { assertTrue(comments.isEmpty()); }
            @Override public void onFailure(Exception e) { fail(); }
        });
    }

    // Testing US 02.08.01

    /**
     * Verifies that deleteComment() removes a comment from the event,
     * so subsequent queries for that event's comments do not include it.
     */
    @Test
    public void deleteComment_removesCommentFromEvent() {
        // Organizer deletes a comment
        EventComment c = new EventComment("event1", "device1", "Alice", "Hello!");

        repo.addComment(c, new CommentRepository.OperationCallback() {
            @Override public void onSuccess() {}
            @Override public void onFailure(Exception e) { fail(); }
        });

        repo.deleteComment(c.getCommentId(), new CommentRepository.OperationCallback() {

            @Override public void onSuccess() {}
            @Override public void onFailure(Exception e) { fail(); }
        });

        repo.getCommentsByEvent("event1", new CommentRepository.CommentsCallback() {
            @Override
            public void onSuccess(List<EventComment> comments) {
                assertTrue(comments.isEmpty());
            }
            @Override public void onFailure(Exception e) { fail(); }
        });
    }
}
