package com.example.ma2025.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.ma2025.helper.NotificationHelper;
import com.example.ma2025.model.AllianceInvitation;
import com.example.ma2025.model.User;
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

        if (currentUser != null) {
            startListeningForInvitations();
        } else {
            Log.e(TAG, "No current user, stopping service");
            stopSelf();
        }

        return START_STICKY; // Automatski restartuj servis ako se ugasi
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