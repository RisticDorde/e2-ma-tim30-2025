package com.example.ma2025.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ma2025.R;
import com.example.ma2025.auth.AuthManager;
import com.example.ma2025.repository.UserRepository;
import android.widget.FrameLayout;

public class RegisterActivity extends AppCompatActivity {

    private EditText editEmail, editUsername, editPassword, editConfirmPassword;
    private Button btnRegister;
    private GridLayout avatarContainer; // PROMENJEN TIP
    private String selectedAvatar = "avatar_1";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicijalizacija UI elemenata
        editEmail = findViewById(R.id.editEmail);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        avatarContainer = findViewById(R.id.avatarContainer); // GridLayout

        // Postavi avatar selektore
        setupAvatarSelection();

        // Klik na Register dugme
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void setupAvatarSelection() {
        String[] avatarResources = {
                "avatar_1",
                "avatar_2",
                "avatar_3",
                "avatar_4",
                "avatar_5"
        };


        for (int i = 0; i < avatarResources.length; i++) {
            // Kreiraj FrameLayout kontejner
            FrameLayout avatarFrame = new FrameLayout(this);

            int size = (int) (getResources().getDisplayMetrics().density * 80);
            GridLayout.LayoutParams frameParams = new GridLayout.LayoutParams();
            frameParams.width = size;
            frameParams.height = size;
            frameParams.setMargins(16, 16, 16, 16);
            avatarFrame.setLayoutParams(frameParams);

            // ImageView za avatar
            ImageView avatarView = new ImageView(this);
            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            avatarView.setLayoutParams(imageParams);
            int resId = getResources().getIdentifier(avatarResources[i], "drawable", getPackageName());
            avatarView.setImageResource(resId);
            avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // ImageView za border (overlay)
            ImageView borderView = new ImageView(this);
            borderView.setLayoutParams(imageParams);
            borderView.setScaleType(ImageView.ScaleType.FIT_XY);

            // Označi selektovani
            if (avatarResources[i] == selectedAvatar) {
                borderView.setImageResource(R.drawable.avatar_selected_border);
            }

            // Tag za identifikaciju
            avatarFrame.setTag(avatarResources[i]);

            // Dodaj ImageView-ove u FrameLayout
            avatarFrame.addView(avatarView);
            avatarFrame.addView(borderView);

            // Klik listener
            final String avatarRes = avatarResources[i];
            avatarFrame.setOnClickListener(v -> {
                selectedAvatar = avatarRes;
                updateAvatarSelection();
            });

            avatarContainer.addView(avatarFrame);
        }
    }

    private void updateAvatarSelection() {
        for (int i = 0; i < avatarContainer.getChildCount(); i++) {
            FrameLayout avatarFrame = (FrameLayout) avatarContainer.getChildAt(i);
            String avatarRes = (String) avatarFrame.getTag();

            // Dobij border ImageView (drugi child)
            ImageView borderView = (ImageView) avatarFrame.getChildAt(1);

            if (avatarRes.equals(selectedAvatar)) {
                borderView.setImageResource(R.drawable.avatar_selected_border);
            } else {
                borderView.setImageDrawable(null);
            }

        }
    }

    private void registerUser() {
        String email = editEmail.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString();
        String confirmPassword = editConfirmPassword.getText().toString();

        // Validacija
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Molimo popunite sva polja", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Lozinke se ne poklapaju", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Lozinka mora imati najmanje 6 karaktera", Toast.LENGTH_SHORT).show();
            return;
        }

        UserRepository userRepo = new UserRepository(this);
        if (userRepo.emailExists(email)) {
            Toast.makeText(this, "Email je već registrovan!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userRepo.usernameExists(username)) {
            Toast.makeText(this, "Korisničko ime je već zauzeto!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Poziv AuthManager.register sa selektovanim avatarom
        AuthManager.registerUser(this, email, password, username, selectedAvatar);
    }
}