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

import java.util.ArrayList;

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

        // Proslijedi trenutni boss index za izračunavanje cijena
        shop = new Shop(currentUser.getCurrentBossIndex());
        updateCoinsView();

        showShopItems();
    }

    private void updateCoinsView() {
        tvCoins.setText("Coins: " + currentUser.getCoins());
    }

    private void showShopItems() {
        // Inicijalizuj liste ako su null
        if (currentUser.getPotions() == null) {
            currentUser.setPotions(new ArrayList<>());
        }
        if (currentUser.getClothings() == null) {
            currentUser.setClothings(new ArrayList<>());
        }

        // Prikazujemo POTIONE
        for (Potion potion : shop.getPotions()) {
            addItemToShop(potion.getName(), potion.getPrice(), () -> {
                if (currentUser.getCoins() >= potion.getPrice()) {
                    int coins = currentUser.getCoins() - potion.getPrice();
                    currentUser.setCoins(coins);

                    // Kreiraj NOVI potion sa defaultnim vrednostima
                    Potion newPotion = new Potion(
                            potion.getName(),
                            potion.isSingleUse(),
                            potion.getPowerBonus(),
                            potion.getPrice(),
                            potion.isSingleUse() ? 1 : -1 // duration
                    );
                    newPotion.setActivated(false); // NIJE aktiviran odmah!

                    currentUser.getPotions().add(newPotion);

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

        // Prikazujemo CLOTHING
        for (Clothing clothing : shop.getClothes()) {
            addItemToShop(clothing.getName(), clothing.getPrice(), () -> {
                if (currentUser.getCoins() >= clothing.getPrice()) {
                    int coins = currentUser.getCoins() - clothing.getPrice();
                    currentUser.setCoins(coins);

                    // Proveri da li već ima istu odeću
                    boolean alreadyHas = false;
                    for (Clothing c : currentUser.getClothings()) {
                        if (c.getType().equals(clothing.getType())) {
                            // SABERI bonuse!
                            c.setBonus(c.getBonus() + clothing.getBonus());
                            c.setDuration(2); // Resetuj trajanje na 2
                            alreadyHas = true;
                            Toast.makeText(this, "Bonus increased! Now: +" + (int)(c.getBonus() * 100) + "%", Toast.LENGTH_LONG).show();
                            break;
                        }
                    }

                    if (!alreadyHas) {
                        // Dodaj novu odeću
                        Clothing newClothing = new Clothing(
                                clothing.getName(),
                                clothing.getType(),
                                clothing.getBonus(),
                                clothing.getPrice()
                        );
                        newClothing.setActivated(false);
                        newClothing.setOwned(true);
                        currentUser.getClothings().add(newClothing);
                    }

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