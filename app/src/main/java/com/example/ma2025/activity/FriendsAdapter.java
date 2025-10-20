package com.example.ma2025.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.ma2025.R;
import com.example.ma2025.model.Friend;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    public interface OnFriendClickListener {
        void onProfileClick(String userId);
        void onAddFriendClick(String userId);
    }

    private List<Friend> friends;
    private OnFriendClickListener listener;

    public FriendsAdapter(List<Friend> friends, OnFriendClickListener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friends.get(position);

        int avatarResId = holder.itemView.getContext()
                .getResources()
                .getIdentifier(friend.getAvatar(), "drawable", holder.itemView.getContext().getPackageName());

        holder.avatar.setImageResource(avatarResId);

        holder.username.setText(friend.getUsername());

        holder.itemView.setOnClickListener(v -> listener.onProfileClick(friend.getEmail()));
        holder.btnAdd.setOnClickListener(v -> listener.onAddFriendClick(friend.getId()));
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username;
        Button btnAdd;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.friend_avatar);
            username = itemView.findViewById(R.id.friend_username);
            btnAdd = itemView.findViewById(R.id.add_friend_btn);
        }
    }
}

