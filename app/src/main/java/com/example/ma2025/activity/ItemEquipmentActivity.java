package com.example.ma2025.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ma2025.R;
import com.example.ma2025.model.Potion;
import com.example.ma2025.model.Weapon;
import com.example.ma2025.model.Clothing;
import java.util.List;

public class ItemEquipmentActivity extends RecyclerView.Adapter<ItemEquipmentActivity.EquipmentViewHolder> {

    private List<Object> equipmentList;
    private boolean showActiveEquipment;

    public ItemEquipmentActivity(List<Object> equipmentList, boolean showActiveEquipment) {
        this.equipmentList = equipmentList;
        this.showActiveEquipment = showActiveEquipment;
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_equipment, parent, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        Object equipment = equipmentList.get(position);
        holder.bind(equipment, showActiveEquipment);
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    static class EquipmentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvBonus, tvDuration;
        private Button btnActivate;

        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEquipmentName);
            tvBonus = itemView.findViewById(R.id.tvEquipmentBonus);
            tvDuration = itemView.findViewById(R.id.tvEquipmentDuration);
            btnActivate = itemView.findViewById(R.id.btnActivate);
        }

        public void bind(Object equipment, boolean isActive) {
            if (equipment instanceof Potion) {
                bindPotion((Potion) equipment, isActive);
            } else if (equipment instanceof Weapon) {
                bindWeapon((Weapon) equipment, isActive);
            } else if (equipment instanceof Clothing) {
                bindClothing((Clothing) equipment, isActive);
            }
        }

        private void bindPotion(Potion potion, boolean isActive) {
            tvName.setText(potion.getName());
            tvBonus.setText("Bonus: +" + (potion.getPowerBonus() * 100) + "% power");

            if (isActive) {
                // AKTIVAN - prikaži koliko je korišćen
                if (potion.isSingleUse()) {
                    tvDuration.setText("Used: 1/1");
                } else {
                    tvDuration.setText("Remaining: " + potion.getDuration());
                }
                btnActivate.setVisibility(View.GONE);
            } else {
                // DOSTUPAN - prikaži dugme
                if (potion.isSingleUse()) {
                    tvDuration.setText("Single-use");
                } else {
                    tvDuration.setText("Duration: " + potion.getDuration() + " battles");
                }
                btnActivate.setVisibility(View.VISIBLE);
                btnActivate.setText("Activate");
            }
        }

        private void bindWeapon(Weapon weapon, boolean isActive) {
            tvName.setText(weapon.getName());
            tvBonus.setText("Bonus: +" + (weapon.getBonus() * 100) + "% | Level: " + weapon.getUpgradeLevel());
            tvDuration.setText("Weapon: Permanent");

            if (isActive) {
                btnActivate.setVisibility(View.GONE);
            } else {
                btnActivate.setVisibility(View.VISIBLE);
                btnActivate.setText("Activate");
            }
        }

        private void bindClothing(Clothing clothing, boolean isActive) {
            tvName.setText(clothing.getName());
            tvBonus.setText("Bonus: +" + (clothing.getBonus() * 100) + "%");

            if (isActive) {
                // AKTIVNA - prikaži koliko je korišćena
                int maxDuration = 2;
                int remaining = clothing.getDuration();
                tvDuration.setText("Used: " + (maxDuration - remaining) + "/" + maxDuration);
                btnActivate.setVisibility(View.GONE);
            } else {
                tvDuration.setText("Duration: " + clothing.getDuration() + " battles");
                btnActivate.setVisibility(View.VISIBLE);
                btnActivate.setText("Activate");
            }
        }
    }
}