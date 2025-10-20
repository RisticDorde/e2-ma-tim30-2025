package com.example.ma2025.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ma2025.R;
import com.example.ma2025.database.DatabaseHelper;
import com.example.ma2025.model.User;
import com.example.ma2025.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private ImageView avatarImage;
    private TextView usernameText;
    private TextView levelText;
    private TextView titleText;
    private TextView ppText;
    private TextView xpText;
    private TextView coinsText;
    private TextView badgesCountText;
    private ProgressBar levelProgressBar;
    private TextView progressText;
    private Button changePasswordBtn;

    private UserRepository userRepo;
    private User currentUser;
    private boolean isOwnProfile = true; // da znamo da li gledamo svoj ili tuđ profil

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.logAllUsers();

        userRepo = new UserRepository(this);
        initViews();

        String viewedEmail = getIntent().getStringExtra("viewed_email");

        if (viewedEmail == null) {
            loadCurrentUser();
            isOwnProfile = true;
        } else {
            loadUserByEmail(viewedEmail);
            isOwnProfile = false;
        }

        setupListeners();
    }

    private void initViews() {
       avatarImage = findViewById(R.id.avatar_image);
        usernameText = findViewById(R.id.username_text);
        levelText = findViewById(R.id.level_text);
        titleText = findViewById(R.id.title_text);
        ppText = findViewById(R.id.pp_text);
        xpText = findViewById(R.id.xp_text);
        coinsText = findViewById(R.id.coins_text);
        badgesCountText = findViewById(R.id.badges_count_text);
        levelProgressBar = findViewById(R.id.level_progress_bar);
        progressText = findViewById(R.id.progress_text);
        changePasswordBtn = findViewById(R.id.change_password_btn);
    }

    private void loadCurrentUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("PROFILE_DEBUG", "Current Firebase email: " + firebaseUser.getEmail());
        if (firebaseUser == null) {
            Toast.makeText(this, "Niste prijavljeni", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUser = userRepo.getUserByEmail(firebaseUser.getEmail());
        Log.d("PROFILE_DEBUG", "Current Firebase email: " + firebaseUser.getEmail());
        if (currentUser == null) {
            Toast.makeText(this, "Greška pri učitavanju profila", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d("PROFILE", "Avatar: '" + currentUser.getAvatar() + "'");
        displayUserData();
    }

    private void loadUserByEmail(String email) {
        currentUser = userRepo.getUserByEmail(email);
        if (currentUser == null) {
            Toast.makeText(this, "Korisnik nije pronađen", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayUserData();
    }

    private void displayUserData() {
        String avatarName = currentUser.getAvatar();
        Log.d("PROFILE", "Avatar iz baze: '" + avatarName + "'");


        if (avatarName != null && !avatarName.isEmpty()) {
            int imageResId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
            if (imageResId != 0) {
                avatarImage.setImageResource(imageResId);
            } else {
                avatarImage.setImageResource(R.drawable.avatar_1); // fallback slika
            }
        } else {
            avatarImage.setImageResource(R.drawable.avatar_1); // ako nema slike
        }


        usernameText.setText(currentUser.getUsername());
        levelText.setText("Nivo " + currentUser.getLevelNumber());
        titleText.setText(currentUser.getTitleDisplayName());

        // --- Badges ---
        LinearLayout badgesContainer = findViewById(R.id.badges_container);
        badgesContainer.removeAllViews();
        if (currentUser.getBadges().isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No badges");
            badgesContainer.addView(empty);
        } else {
            for (String badge : currentUser.getBadges()) {
                TextView badgeView = new TextView(this);
                badgeView.setText(badge); // ili možeš staviti sličicu ako imaš drawable
                badgeView.setPadding(8, 0, 8, 0);
                badgesContainer.addView(badgeView);
            }
        }

        int badgeCount = currentUser.getBadges().size();
        badgesCountText.setText("Badge: " + badgeCount);

        // --- Equipment ---
        LinearLayout equipmentContainer = findViewById(R.id.equipment_container);
        equipmentContainer.removeAllViews();
        if (currentUser.getEquipment().isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No equipment");
            equipmentContainer.addView(empty);
        } else {
            for (String eq : currentUser.getEquipment()) {
                TextView eqView = new TextView(this);
                eqView.setText(eq);
                eqView.setPadding(0, 4, 0, 4);
                equipmentContainer.addView(eqView);
            }
        }

        if (isOwnProfile) {
            ppText.setText("PP: " + currentUser.getPowerPoints());
            xpText.setText("XP: " + currentUser.getExperiencePoints());
            coinsText.setText("Coins: " + currentUser.getCoins());
            float progressPercentage = currentUser.getLevelProgressPercentage();
            levelProgressBar.setProgress((int) progressPercentage);
            int currentProgress = currentUser.getCurrentLevelProgress();
            int requiredXp = currentUser.getNextLevelRequiredXp();
            progressText.setText(currentProgress + " / " + requiredXp + " XP");
            changePasswordBtn.setVisibility(View.VISIBLE);
        } else {
            ppText.setVisibility(View.GONE);
            xpText.setVisibility(View.GONE);
            coinsText.setVisibility(View.GONE);
            levelProgressBar.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            changePasswordBtn.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        changePasswordBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }
}
