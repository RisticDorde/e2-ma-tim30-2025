package com.example.ma2025.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ma2025.R;
import com.example.ma2025.helper.NotificationHelper;
import com.example.ma2025.model.Alliance;
import com.example.ma2025.model.User;
import com.example.ma2025.repository.AllianceRepository;
import com.example.ma2025.repository.UserRepository;
import com.google.android.material.button.MaterialButton;

public class AllianceInvitationActivity extends AppCompatActivity {

    private TextView tvInvitationMessage;
    private TextView tvWarningMessage;
    private MaterialButton btnAccept;
    private MaterialButton btnReject;

    private AllianceRepository allianceRepository;
    private UserRepository userRepository;
    private NotificationHelper notificationHelper;
    private User currentUser;

    private String invitationId;
    private String allianceId;
    private String allianceName;
    private String fromUsername;
    private String fromEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_invitation);

        tvInvitationMessage = findViewById(R.id.tv_invitation_message);
        tvWarningMessage = findViewById(R.id.tv_warning_message);
        btnAccept = findViewById(R.id.btn_accept);
        btnReject = findViewById(R.id.btn_reject);

        allianceRepository = new AllianceRepository(this);
        userRepository = new UserRepository(this);
        notificationHelper = new NotificationHelper(this);
        currentUser = userRepository.getCurrentAppUser(this);

        // Dohvati podatke iz Intent-a
        invitationId = getIntent().getStringExtra("invitation_id");
        allianceId = getIntent().getStringExtra("alliance_id");
        allianceName = getIntent().getStringExtra("alliance_name");
        fromUsername = getIntent().getStringExtra("from_username");
        fromEmail = getIntent().getStringExtra("from_email");

        tvInvitationMessage.setText(fromUsername + " te je pozvao/la u savez: " + allianceName);

        // Proveri da li korisnik već ima savez
        checkCurrentAlliance();

        btnAccept.setOnClickListener(v -> handleAccept());
        btnReject.setOnClickListener(v -> handleReject());
    }

    private void checkCurrentAlliance() {
        allianceRepository.getUserAlliance(currentUser.getEmail(), new AllianceRepository.OnAllianceFetchedListener() {
            @Override
            public void onSuccess(Alliance alliance) {
                if (alliance != null) {
                    // Korisnik već ima savez
                    if (alliance.isMissionActive()) {
                        // Misija je pokrenuta - ne može napustiti
                        tvWarningMessage.setText("Ne možeš prihvatiti poziv jer je misija u toku u tvom trenutnom savezu.");
                        tvWarningMessage.setVisibility(android.view.View.VISIBLE);
                        btnAccept.setEnabled(false);
                    } else {
                        // Može napustiti savez
                        tvWarningMessage.setVisibility(android.view.View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AllianceInvitationActivity.this,
                        "Greška: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleAccept() {
        btnAccept.setEnabled(false);
        btnReject.setEnabled(false);

        // Prvo proveri da li korisnik ima trenutni savez
        allianceRepository.getUserAlliance(currentUser.getEmail(), new AllianceRepository.OnAllianceFetchedListener() {
            @Override
            public void onSuccess(Alliance currentAlliance) {
                if (currentAlliance != null) {
                    // Napusti trenutni savez
                    allianceRepository.removeMemberFromAlliance(
                            currentAlliance.getId(),
                            currentUser.getId() + "",
                            currentUser.getEmail(),
                            new AllianceRepository.OnOperationListener() {
                                @Override
                                public void onSuccess() {
                                    // Sada pristup novom savezu
                                    joinNewAlliance();
                                }

                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(AllianceInvitationActivity.this,
                                            "Greška pri napuštanju saveza: " + error,
                                            Toast.LENGTH_SHORT).show();
                                    btnAccept.setEnabled(true);
                                    btnReject.setEnabled(true);
                                }
                            });
                } else {
                    // Nema trenutni savez, samo pristup novom
                    joinNewAlliance();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AllianceInvitationActivity.this,
                        "Greška: " + error,
                        Toast.LENGTH_SHORT).show();
                btnAccept.setEnabled(true);
                btnReject.setEnabled(true);
            }
        });
    }

    private void joinNewAlliance() {
        // Dodaj korisnika u novi savez
        allianceRepository.addMemberToAlliance(
                allianceId,
                currentUser.getId() + "",
                currentUser.getEmail(),
                new AllianceRepository.OnOperationListener() {
                    @Override
                    public void onSuccess() {
                        // Prihvati poziv (promeni status)
                        allianceRepository.acceptInvitation(invitationId, new AllianceRepository.OnOperationListener() {
                            @Override
                            public void onSuccess() {
                                // Ukloni notifikaciju
                                notificationHelper.cancelInvitationNotification(invitationId);

                                // POŠALJI NOTIFIKACIJU VOĐI
                                sendAcceptanceNotificationToLeader();

                                Toast.makeText(AllianceInvitationActivity.this,
                                        "Pridružio/la si se savezu!",
                                        Toast.LENGTH_SHORT).show();

                                // OTVORI STRANICU ZA PRIKAZ SAVEZA
                                Intent intent = new Intent(AllianceInvitationActivity.this, AllianceViewActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(AllianceInvitationActivity.this,
                                        "Greška: " + error,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(AllianceInvitationActivity.this,
                                "Greška pri pridruživanju: " + error,
                                Toast.LENGTH_SHORT).show();
                        btnAccept.setEnabled(true);
                        btnReject.setEnabled(true);
                    }
                });
    }

    private void sendAcceptanceNotificationToLeader() {
        // Kreiraj notifikaciju u Firebase za vođu
        allianceRepository.createAcceptanceNotification(
                allianceId,
                allianceName,
                fromEmail, // Email vođe
                currentUser.getEmail(),
                currentUser.getUsername(),
                new AllianceRepository.OnOperationListener() {
                    @Override
                    public void onSuccess() {
                        android.util.Log.d("ALLIANCE", "Acceptance notification sent to leader: " + fromEmail);
                    }

                    @Override
                    public void onFailure(String error) {
                        android.util.Log.e("ALLIANCE", "Failed to send acceptance notification: " + error);
                    }
                });
    }

    private void handleReject() {
        btnAccept.setEnabled(false);
        btnReject.setEnabled(false);

        allianceRepository.rejectInvitation(invitationId, new AllianceRepository.OnOperationListener() {
            @Override
            public void onSuccess() {
                // Ukloni notifikaciju
                notificationHelper.cancelInvitationNotification(invitationId);

                Toast.makeText(AllianceInvitationActivity.this,
                        "Poziv odbijen",
                        Toast.LENGTH_SHORT).show();

                // VRATI SE NA MAIN ACTIVITY
                Intent intent = new Intent(AllianceInvitationActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AllianceInvitationActivity.this,
                        "Greška: " + error,
                        Toast.LENGTH_SHORT).show();
                btnAccept.setEnabled(true);
                btnReject.setEnabled(true);
            }
        });
    }
}