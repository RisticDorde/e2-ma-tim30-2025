package com.example.ma2025.boss;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ma2025.R;

import java.util.List;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.ViewHolder> {

    private List<EquipmentItem> items;
    private OnEquipmentChangeListener listener;

    public interface OnEquipmentChangeListener {
        void onEquipmentChanged(EquipmentItem item, boolean isEquipped);
    }

    public EquipmentAdapter(List<EquipmentItem> items, OnEquipmentChangeListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EquipmentItem item = items.get(position);

        holder.nameText.setText(item.getName());
        holder.typeText.setText(item.getType().equals("weapon") ? "⚔️ Oružje" : "👕 Odeća");
        holder.bonusText.setText("+" + (int)(item.getBonus() * 100) + "%");
        holder.checkBox.setChecked(item.isEquipped());

        // Listener za checkbox
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setEquipped(isChecked);
            if (listener != null) {
                listener.onEquipmentChanged(item, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView nameText, typeText, bonusText;

        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.equipCheckBox);
            nameText = itemView.findViewById(R.id.equipmentNameText);
            typeText = itemView.findViewById(R.id.equipmentTypeText);
            bonusText = itemView.findViewById(R.id.equipmentBonusText);
        }
    }
}
