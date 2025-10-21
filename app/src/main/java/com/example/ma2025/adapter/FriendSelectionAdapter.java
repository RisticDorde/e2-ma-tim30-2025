package com.example.ma2025.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ma2025.R;
import com.example.ma2025.model.Friend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendSelectionAdapter extends RecyclerView.Adapter<FriendSelectionAdapter.ViewHolder> {

    private List<Friend> friends;
    private Set<String> selectedFriendEmails; // Čuvamo email-ove odabranih prijatelja

    public FriendSelectionAdapter(List<Friend> friends) {
        this.friends = friends;
        this.selectedFriendEmails = new HashSet<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Friend friend = friends.get(position);

        holder.tvUsername.setText(friend.getUsername());
        holder.tvEmail.setText(friend.getEmail());

        // Postavi stanje checkbox-a
        holder.checkbox.setChecked(selectedFriendEmails.contains(friend.getEmail()));

        // Listener za checkbox
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedFriendEmails.add(friend.getEmail());
            } else {
                selectedFriendEmails.remove(friend.getEmail());
            }
        });

        // Listener za ceo red (klik bilo gde toggleuje checkbox)
        holder.itemView.setOnClickListener(v -> {
            holder.checkbox.setChecked(!holder.checkbox.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    // Vraća listu odabranih prijatelja
    public List<Friend> getSelectedFriends() {
        List<Friend> selected = new ArrayList<>();
        for (Friend friend : friends) {
            if (selectedFriendEmails.contains(friend.getEmail())) {
                selected.add(friend);
            }
        }
        return selected;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;
        TextView tvUsername;
        TextView tvEmail;

        ViewHolder(View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox_friend);
            tvUsername = itemView.findViewById(R.id.tv_friend_username);
            tvEmail = itemView.findViewById(R.id.tv_friend_email);
        }
    }
}
