package com.example.ma2025.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ma2025.R;
import com.example.ma2025.model.Clothing;
import com.example.ma2025.model.Potion;
import com.example.ma2025.model.Shop;
import com.example.ma2025.model.User;
import com.example.ma2025.repository.UserRepository;

public class ShopActivity extends AppCompatActivity {

    private TextView tvCoins;
    private LinearLayout shopItemsContainer;
    private User currentUser;
    private Shop shop;
    private UserRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        repo = new UserRepository(this);
        currentUser = repo.getCurrentAppUser(this);

        tvCoins = findViewById(R.id.tvCoins);
        shopItemsContainer = findViewById(R.id.shopItemsContainer);

        shop = new Shop(); // kreira listu itema
        updateCoinsView();

        showShopItems();
    }

    private void updateCoinsView() {
        tvCoins.setText("Coins: " + currentUser.getCoins());
    }

    private void showShopItems() {
        // prikazujemo POTIONE
        for (Potion potion : shop.getPotions()) {
            addItemToShop(potion.getName(), potion.getPrice(), () -> {
                if (currentUser.getCoins() >= potion.getPrice()) {
                    int coins = currentUser.getCoins() - potion.getPrice();
                    currentUser.setCoins(coins);

                    // Dodaj opremu
                    currentUser.getEquipment().add(potion.getName());
                    currentUser.getPotions().add(potion);

                    // Sačuvaj u bazu
                    boolean success = repo.updateUser(currentUser);

                    if (success) {
                        updateCoinsView();
                        Toast.makeText(this, "Successful purchase of " + potion.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error while saving purchase!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "You don't have enough coins!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // prikazujemo CLOTHING
        for (Clothing clothing : shop.getClothes()) {
            addItemToShop(clothing.getName(), clothing.getPrice(), () -> {
                if (currentUser.getCoins() >= clothing.getPrice()) {
                    int coins = currentUser.getCoins() - clothing.getPrice();
                    currentUser.setCoins(coins);

                    // Dodaj opremu
                    currentUser.getEquipment().add(clothing.getName());
                    currentUser.getClothings().add(clothing);

                    // Sačuvaj u bazu
                    boolean success = repo.updateUser(currentUser);

                    if (success) {
                        updateCoinsView();
                        Toast.makeText(this, "Successful purchase of " + clothing.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error while saving purchase!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "You don't have enough coins!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void addItemToShop(String name, int price, Runnable onBuyClick) {
        // kreiramo jedan red za item
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(0, 10, 0, 10);

        TextView tvName = new TextView(this);
        tvName.setText(name + " - " + price + " coins");
        tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button btnBuy = new Button(this);
        btnBuy.setText("Buy");
        btnBuy.setOnClickListener(v -> onBuyClick.run());

        itemLayout.addView(tvName);
        itemLayout.addView(btnBuy);

        shopItemsContainer.addView(itemLayout);
    }
}
