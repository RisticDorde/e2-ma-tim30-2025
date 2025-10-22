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

    private Button statisticBtn;

    // QR kod elementi
    private ImageView qrCodeImage;
    private TextView qrCodeText;
    private TextView qrCodeLabel;

    private UserRepository userRepo;
    private User currentUser;
    private boolean isOwnProfile = true;

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
        statisticBtn = findViewById((R.id.btn_statistics));

        // QR kod elementi
        qrCodeImage = findViewById(R.id.qr_code_image);
        qrCodeText = findViewById(R.id.qr_code_text);
        qrCodeLabel = findViewById(R.id.qr_code_label);
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
        displayUserData(true);
    }

    private void loadUserByEmail(String email) {
        currentUser = userRepo.getUserByEmail(email);
        if (currentUser == null) {
            Toast.makeText(this, "Korisnik nije pronađen", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayUserData(false);
    }

    private void displayUserData(boolean isOwnProfile) {
        String avatarName = currentUser.getAvatar();
        Log.d("PROFILE", "Avatar iz baze: '" + avatarName + "'");

        if (avatarName != null && !avatarName.isEmpty()) {
            int imageResId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
            if (imageResId != 0) {
                avatarImage.setImageResource(imageResId);
            } else {
                avatarImage.setImageResource(R.drawable.avatar_1);
            }
        } else {
            avatarImage.setImageResource(R.drawable.avatar_1);
        }

        usernameText.setText(currentUser.getUsername());
        Log.d("PROFILE", "Username iz baze: '" + currentUser.getUsername() + "'");
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
                badgeView.setText(badge);
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

        // --- QR Code ---
        displayQRCode();

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
            statisticBtn.setVisibility(View.VISIBLE);


            // Prikaži QR kod samo na sopstvenom profilu
            qrCodeImage.setVisibility(View.VISIBLE);
            qrCodeText.setVisibility(View.VISIBLE);
            qrCodeLabel.setVisibility(View.VISIBLE);
            qrCodeLabel.setText("Moj QR kod");
        } else {
            ppText.setVisibility(View.GONE);
            xpText.setVisibility(View.GONE);
            coinsText.setVisibility(View.GONE);
            levelProgressBar.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            changePasswordBtn.setVisibility(View.GONE);
            statisticBtn.setVisibility(View.GONE);

            // Prikaži QR kod i na tuđem profilu (za skeniranje)
            qrCodeImage.setVisibility(View.VISIBLE);
            qrCodeText.setVisibility(View.VISIBLE);
            qrCodeLabel.setVisibility(View.VISIBLE);
            qrCodeLabel.setText("Skeniraj QR kod");
        }
    }

    /**
     * Generiše i prikazuje QR kod korisnika
     */
    private void displayQRCode() {
        if (currentUser == null) return;

        String qrContent = currentUser.getQrCode();

        if (qrContent == null || qrContent.isEmpty()) {
            Log.w("PROFILE", "QR kod nije postavljen za korisnika");
            qrCodeImage.setVisibility(View.GONE);
            qrCodeText.setVisibility(View.GONE);
            qrCodeLabel.setVisibility(View.GONE);
            return;
        }

        Log.d("PROFILE", "Generišem QR kod: " + qrContent);

        // Generiši QR kod bitmap
        Bitmap qrBitmap = QRCodeGenerator.generateQRCode(qrContent, 500, 500);

        if (qrBitmap != null) {
            qrCodeImage.setImageBitmap(qrBitmap);
            qrCodeText.setText(qrContent);
            Log.d("PROFILE", "QR kod uspešno generisan");
        } else {
            Log.e("PROFILE", "Neuspešno generisanje QR koda");
            Toast.makeText(this, "Greška pri generisanju QR koda", Toast.LENGTH_SHORT).show();
            qrCodeImage.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        changePasswordBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        // Opciono: Dugme za deljenje QR koda
        qrCodeImage.setOnClickListener(v -> {
            if (isOwnProfile) {
                shareQRCode();
            }
        });


        statisticBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Deljenje QR koda (opciono)
     */
    private void shareQRCode() {
        if (currentUser == null) return;

        String qrContent = currentUser.getQrCode();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Moj QR kod: " + qrContent);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Moj profil");

        startActivity(Intent.createChooser(shareIntent, "Podeli QR kod"));
    }
}