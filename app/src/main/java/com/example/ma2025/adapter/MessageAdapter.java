package com.example.ma2025.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ma2025.R;
import com.example.ma2025.model.AllianceMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<AllianceMessage> messages;
    private String currentUserId;
    private Context context;

    public MessageAdapter(Context context, String currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.messages = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        AllianceMessage message = messages.get(position);

        // Ako je pošiljalac trenutni korisnik → SENT (desno)
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED; // RECEIVED (levo)
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AllianceMessage message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // Dodaj novu poruku
    public void addMessage(AllianceMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    // Postavi sve poruke (za inicijalno učitavanje)
    public void setMessages(List<AllianceMessage> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
        notifyDataSetChanged();
    }

    // ========== ViewHolder za POSLATE poruke (desno) ==========
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView timestampTextView;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }

        public void bind(AllianceMessage message) {
            messageTextView.setText(message.getMessageText());
            timestampTextView.setText(message.getFormattedTime());
        }
    }

    // ========== ViewHolder za PRIMLJENE poruke (levo) ==========
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImageView;
        TextView senderNameTextView;
        TextView messageTextView;
        TextView timestampTextView;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            senderNameTextView = itemView.findViewById(R.id.senderNameTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }

        public void bind(AllianceMessage message) {
            senderNameTextView.setText(message.getSenderUsername());
            messageTextView.setText(message.getMessageText());
            timestampTextView.setText(message.getFormattedTime());

            // Dinamički učitavanje avatara iz drawable foldera
            Context context = itemView.getContext();
            int avatarResId = context.getResources().getIdentifier(
                    message.getSenderAvatar(), // npr. "avatar_3"
                    "drawable",
                    context.getPackageName()
            );

            if (avatarResId == 0) {
                avatarResId = R.drawable.avatar_1; // fallback ako ne postoji
            }

            avatarImageView.setImageResource(avatarResId);
        }

    }
}