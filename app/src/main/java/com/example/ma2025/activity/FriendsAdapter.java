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
import java.util.Map;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private List<Friend> friendList;
    private OnFriendClickListener listener;
    private Map<String, Boolean> friendshipStatusMap;

    public interface OnFriendClickListener {
        void onProfileClick(String email);
        void onAddFriendClick(Friend friend);
        void onRemoveFriendClick(Friend friend);
    }

    public FriendsAdapter(List<Friend> friendList, OnFriendClickListener listener) {
        this.friendList = friendList;
        this.listener = listener;
    }

    public void setFriendshipStatusMap(Map<String, Boolean> friendshipStatusMap) {
        this.friendshipStatusMap = friendshipStatusMap;
        notifyDataSetChanged();
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
        Friend friend = friendList.get(position);
        boolean isFriend = friendshipStatusMap != null &&
                friendshipStatusMap.containsKey(friend.getEmail());
        holder.bind(friend, listener, isFriend);
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername;
        TextView tvEmail;
        Button btnAddFriend;

        FriendViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_friend_avatar);
            tvUsername = itemView.findViewById(R.id.tv_friend_username);
            tvEmail = itemView.findViewById(R.id.tv_friend_email);
            btnAddFriend = itemView.findViewById(R.id.btn_add_friend);
        }

        void bind(Friend friend, OnFriendClickListener listener, boolean isFriend) {
            tvUsername.setText(friend.getUsername());

            // Učitaj avatar
            if (friend.getAvatar() != null && !friend.getAvatar().isEmpty()) {
                try {
                    // Ako je avatar resource name (npr. "avatar1")
                    int avatarResId = itemView.getContext().getResources()
                            .getIdentifier(friend.getAvatar(), "drawable", itemView.getContext().getPackageName());

                    if (avatarResId != 0) {
                        ivAvatar.setImageResource(avatarResId);
                    } else {
                        ivAvatar.setImageResource(R.drawable.avatar_1);
                    }
                } catch (Exception e) {
                    ivAvatar.setImageResource(R.drawable.avatar_1);
                }
            } else {
                ivAvatar.setImageResource(R.drawable.avatar_1);
            }

            // Klik na avatar otvara profil
            ivAvatar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProfileClick(friend.getEmail());
                }
            });

            // Postavi dugme na osnovu statusa prijateljstva
            if (isFriend) {
                btnAddFriend.setText("Ukloni");
                btnAddFriend.setBackgroundColor(itemView.getContext().getResources()
                        .getColor(android.R.color.holo_red_light));
                btnAddFriend.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRemoveFriendClick(friend);
                    }
                });
            } else {
                btnAddFriend.setText("Dodaj");
                btnAddFriend.setBackgroundColor(itemView.getContext().getResources()
                        .getColor(android.R.color.holo_green_light));
                btnAddFriend.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAddFriendClick(friend);
                    }
                });
            }
        }
    }
}