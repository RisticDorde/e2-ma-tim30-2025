package com.example.ma2025.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ma2025.R;
import com.example.ma2025.model.User;

import java.util.List;

public class AllianceMemberAdapter extends RecyclerView.Adapter<AllianceMemberAdapter.ViewHolder> {

    private List<User> members;
    private String leaderEmail;

    public AllianceMemberAdapter(List<User> members, String leaderEmail) {
        this.members = members;
        this.leaderEmail = leaderEmail;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alliance_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User member = members.get(position);
        holder.tvUsername.setText(member.getUsername());

        // Prikaži badge ako je vođa
        if (member.getEmail().equals(leaderEmail)) {
            holder.tvBadge.setVisibility(View.VISIBLE);
            holder.tvBadge.setText("Vođa");
        } else {
            holder.tvBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        TextView tvBadge;

        ViewHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_member_username);
            tvBadge = itemView.findViewById(R.id.tv_member_badge);
        }
    }
}
