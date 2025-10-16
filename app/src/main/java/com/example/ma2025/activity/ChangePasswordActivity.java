package com.example.ma2025.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ma2025.R;
import com.example.ma2025.auth.AuthManager;
import com.example.ma2025.repository.UserRepository;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editCurrentPassword, editNewPassword, editConfirmPassword;
    private Button btnChangePassword;

    private UserRepository userRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        editCurrentPassword = findViewById(R.id.editCurrentPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        userRepo = new UserRepository(this);

        btnChangePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void handleChangePassword() {
        String currentPassword = editCurrentPassword.getText().toString().trim();
        String newPassword = editNewPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Popunite sva polja", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Nove lozinke se ne poklapaju", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Korisnik nije prijavljen", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseUser.reauthenticate(EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseUser.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        // Promena lozinke u lokalnoj bazi
                                        boolean updated = userRepo.changePasswordByEmail(firebaseUser.getEmail(), newPassword);

                                        if (updated) {
                                            Toast.makeText(this, "Lozinka uspešno promenjena. Prijavite se ponovo.", Toast.LENGTH_LONG).show();

                                            // Logout i povratak na login
                                            FirebaseAuth.getInstance().signOut();
                                            Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();

                                        } else {
                                            Toast.makeText(this, "Greška pri promeni lozinke u bazi", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(this, "Greška pri promeni lozinke: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Trenutna lozinka nije tačna", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
