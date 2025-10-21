package com.example.ma2025.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.ma2025.helper.NotificationHelper;
import com.example.ma2025.model.AcceptanceNotification;
import com.example.ma2025.model.AllianceInvitation;
import com.example.ma2025.model.User;
import com.example.ma2025.repository.AllianceRepository;
import com.example.ma2025.repository.UserRepository;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class AllianceInvitationService extends Service {

    private static final String TAG = "AllianceInvService";
    private FirebaseFirestore firestore;
    private ListenerRegistration listenerRegistration;
    private NotificationHelper notificationHelper;
    private UserRepository userRepository;
    private User currentUser;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");

        firestore = FirebaseFirestore.getInstance();
        notificationHelper = new NotificationHelper(this);
        userRepository = new UserRepository(this);
        currentUser = userRepository.getCurrentAppUser(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        // 🔹 1. Kreiraj kanal za foreground notifikaciju (obavezno za Android 8+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    "alliance_service_channel",
                    "Alliance Service Channel",
                    android.app.NotificationManager.IMPORTANCE_LOW
            );
            android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // 🔹 2. Kreiraj notifikaciju koja se prikazuje dok servis radi
        androidx.core.app.NotificationCompat.Builder builder =
                new androidx.core.app.NotificationCompat.Builder(this, "alliance_service_channel")
                        .setContentTitle("Alliance service active")
                        .setContentText("Listening for invitations and alliance updates")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setOngoing(true); // da korisnik ne može lako da je ukloni

        // 🔹 3. Pokreni servis u foreground režimu
        startForeground(1, builder.build());

        // 🔹 4. Tvoj postojeći kod ostaje isti
        if (currentUser != null) {
            startListeningForInvitations();
            startListeningForAcceptanceNotifications();
        } else {
            Log.e(TAG, "No current user, stopping service");
            stopSelf();
        }

        return START_STICKY;
    }


    private void startListeningForAcceptanceNotifications() {
        Log.d(TAG, "Starting to listen for acceptance notifications for: " + currentUser.getEmail());

        AllianceRepository allianceRepository = new AllianceRepository(this);

        firestore.collection("acceptance_notifications")
                .whereEqualTo("leaderEmail", currentUser.getEmail())
                .whereEqualTo("seen", false)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen for acceptance failed", error);
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                AcceptanceNotification notification = dc.getDocument().toObject(AcceptanceNotification.class);
                                Log.d(TAG, "New acceptance notification: " + notification.getAcceptedUsername());

                                // Prikaži notifikaciju
                                notificationHelper.showAcceptedNotification(
                                        notification.getAcceptedUsername(),
                                        notification.getAllianceName()
                                );

                                // Označi kao pročitanu
                                allianceRepository.markAcceptanceNotificationAsSeen(
                                        notification.getId(),
                                        new AllianceRepository.OnOperationListener() {
                                            @Override
                                            public void onSuccess() {
                                                Log.d(TAG, "Notification marked as seen");
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                Log.e(TAG, "Failed to mark as seen: " + error);
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void startListeningForInvitations() {
        Log.d(TAG, "Starting to listen for invitations for: " + currentUser.getEmail());

        listenerRegistration = firestore.collection("alliance_invitations")
                .whereEqualTo("toUserEmail", currentUser.getEmail())
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed", error);
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    AllianceInvitation invitation = dc.getDocument().toObject(AllianceInvitation.class);
                                    Log.d(TAG, "New invitation received: " + invitation.getId());
                                    notificationHelper.showInvitationNotification(invitation);
                                    break;

                                case MODIFIED:
                                    // Ako se poziv promeni (prihvaćen/odbijen), ukloni notifikaciju
                                    AllianceInvitation modifiedInvitation = dc.getDocument().toObject(AllianceInvitation.class);
                                    if (!modifiedInvitation.isPending()) {
                                        notificationHelper.cancelInvitationNotification(modifiedInvitation.getId());
                                    }
                                    break;

                                case REMOVED:
                                    String removedId = dc.getDocument().getId();
                                    notificationHelper.cancelInvitationNotification(removedId);
                                    break;
                            }
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}