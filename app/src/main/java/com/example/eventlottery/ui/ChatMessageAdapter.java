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

    public ChatMessageAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

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

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * ViewHolder for a single chat bubble.
     */
    static class MessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMessage;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message_text);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getText());
        }
    }
}