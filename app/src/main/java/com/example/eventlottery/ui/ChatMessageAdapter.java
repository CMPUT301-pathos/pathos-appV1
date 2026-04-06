package com.example.eventlottery.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.ChatMessage;

import java.util.List;

/**
 * RecyclerView adapter for displaying chat messages in the AI assistant.
 *
 * Uses two view types:
 * - VIEW_TYPE_USER: right-aligned bubble (item_message_user.xml)
 * - VIEW_TYPE_AI:   left-aligned bubble  (item_message_ai.xml)
 *
 * Follows the same pattern as EventCommentAdapter, AdminEventAdapter,
 * and NotificationLogAdapter in this package.
 *
 * @author AI-assisted implementation
 * @version 1.0
 * @see ChatMessage
 * @see com.example.eventlottery.AiAssistantFragment
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_AI   = 1;

    private final List<ChatMessage> messages;

    /**
     * Constructs the chat message adapter with the provided message list.
     *
     * @param messages list of chat messages to display
     */
    public ChatMessageAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    /**
     * Returns the item view type based on whether the message was sent by the user.
     *
     * @param position position of the item
     * @return view type constant for user or AI message
     */
    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    /**
     * Creates a ViewHolder for the message bubble layout.
     *
     * @param parent parent view group
     * @param viewType layout type for the message bubble
     * @return new MessageViewHolder instance
     */
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == VIEW_TYPE_USER)
                ? R.layout.itemmessageuser
                : R.layout.itemmessageai;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }

    /**
     * Binds a chat message to the ViewHolder.
     *
     * @param holder view holder for the message row
     * @param position position of the message in the list
     */
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    /**
     * Returns the total number of chat messages.
     *
     * @return item count
     */
    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * ViewHolder for a single chat bubble.
     */
    static class MessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMessage;

        /**
         * Constructs the view holder for a chat message bubble.
         *
         * @param itemView inflated message item view
         */
        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message_text);
        }

        /**
         * Displays the text content of the chat message.
         *
         * @param message chat message model
         */
        void bind(ChatMessage message) {
            tvMessage.setText(message.getText());
        }
    }
}