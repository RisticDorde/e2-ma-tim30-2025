package com.example.ma2025.repository;

import android.content.Context;
import android.util.Log;

import com.example.ma2025.model.AcceptanceNotification;
import com.example.ma2025.model.Alliance;
import com.example.ma2025.model.AllianceInvitation;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllianceRepository {
    private static final String TAG = "AllianceRepository";
    private FirebaseFirestore firestore;
    private Context context;
    public AllianceRepository(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
    }
    public void createAlliance(Alliance alliance, OnAllianceCreatedListener listener) {
        String allianceId = firestore.collection("alliances").document().getId();
        alliance.setId(allianceId);

        firestore.collection("alliances")
                .document(allianceId)
                .set(alliance)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Alliance created successfully: " + allianceId);
                    if (listener != null) listener.onSuccess(alliance);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating alliance", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }
    public void getAllianceById(String allianceId, OnAllianceFetchedListener listener) {
        firestore.collection("alliances")
                .document(allianceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Alliance alliance = documentSnapshot.toObject(Alliance.class);
                        if (listener != null) listener.onSuccess(alliance);
                    } else {
                        if (listener != null) listener.onFailure("Alliance not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching alliance", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }
    public void getUserAlliance(String userEmail, OnAllianceFetchedListener listener) {
        firestore.collection("alliances")
                .whereEqualTo("status", "active")
                .whereArrayContains("memberEmails", userEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Alliance alliance = querySnapshot.getDocuments().get(0).toObject(Alliance.class);
                        if (listener != null) listener.onSuccess(alliance);
                    } else {
                        if (listener != null) listener.onSuccess(null); // Nema saveza
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user alliance", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }
    public void addMemberToAlliance(String allianceId, String memberId, String memberEmail, OnOperationListener listener) {
        firestore.collection("alliances")
                .document(allianceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Alliance alliance = documentSnapshot.toObject(Alliance.class);
                    if (alliance != null) {
                        alliance.addMember(memberId, memberEmail);

                        firestore.collection("alliances")
                                .document(allianceId)
                                .set(alliance)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Member added successfully");
                                    if (listener != null) listener.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error adding member", e);
                                    if (listener != null) listener.onFailure(e.getMessage());
                                });
                    }
                });
    }
    public void removeMemberFromAlliance(String allianceId, String memberId, String memberEmail, OnOperationListener listener) {
        firestore.collection("alliances")
                .document(allianceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Alliance alliance = documentSnapshot.toObject(Alliance.class);
                    if (alliance != null) {
                        alliance.removeMember(memberId, memberEmail);

                        firestore.collection("alliances")
                                .document(allianceId)
                                .set(alliance)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Member removed successfully");
                                    if (listener != null) listener.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error removing member", e);
                                    if (listener != null) listener.onFailure(e.getMessage());
                                });
                    }
                });
    }
    public void disbandAlliance(String allianceId, OnOperationListener listener) {
        firestore.collection("alliances")
                .document(allianceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Alliance alliance = documentSnapshot.toObject(Alliance.class);
                    if (alliance != null) {
                        // Očisti članove
                        alliance.getMemberIds().clear();
                        alliance.getMemberEmails().clear();

                        // Postavi status na disbanded
                        alliance.setStatus("disbanded");

                        // Ažuriraj dokument u Firestore
                        firestore.collection("alliances")
                                .document(allianceId)
                                .set(alliance)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Alliance disbanded and members removed successfully");
                                    if (listener != null) listener.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating disbanded alliance", e);
                                    if (listener != null) listener.onFailure(e.getMessage());
                                });
                    } else {
                        Log.w(TAG, "Alliance not found");
                        if (listener != null) listener.onFailure("Alliance not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching alliance before disbanding", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }


    public void sendInvitation(AllianceInvitation invitation, OnOperationListener listener) {
        String invitationId = firestore.collection("alliance_invitations").document().getId();
        invitation.setId(invitationId);

        firestore.collection("alliance_invitations")
                .document(invitationId)
                .set(invitation)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Invitation sent successfully: " + invitationId);
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending invitation", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }
    public void getPendingInvitations(String userEmail, OnInvitationsFetchedListener listener) {
        firestore.collection("alliance_invitations")
                .whereEqualTo("toUserEmail", userEmail)
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<AllianceInvitation> invitations = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        AllianceInvitation invitation = doc.toObject(AllianceInvitation.class);
                        if (invitation != null) {
                            invitations.add(invitation);
                        }
                    }
                    Log.d(TAG, "Fetched " + invitations.size() + " pending invitations");
                    if (listener != null) listener.onSuccess(invitations);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching invitations", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }
    public void acceptInvitation(String invitationId, OnOperationListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "accepted");
        updates.put("respondedAt", new java.util.Date());

        firestore.collection("alliance_invitations")
                .document(invitationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Invitation accepted");
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error accepting invitation", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }
    public void rejectInvitation(String invitationId, OnOperationListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "rejected");
        updates.put("respondedAt", new java.util.Date());

        firestore.collection("alliance_invitations")
                .document(invitationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Invitation rejected");
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error rejecting invitation", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }







    /**
     * Kreira notifikaciju za vođu da je korisnik prihvatio poziv
     */
    public void createAcceptanceNotification(String allianceId, String allianceName,
                                             String leaderEmail, String acceptedUserEmail,
                                             String acceptedUsername, OnOperationListener listener) {
        String notificationId = firestore.collection("acceptance_notifications").document().getId();

        AcceptanceNotification notification = new AcceptanceNotification(
                notificationId,
                allianceId,
                allianceName,
                leaderEmail,
                acceptedUserEmail,
                acceptedUsername
        );

        firestore.collection("acceptance_notifications")
                .document(notificationId)
                .set(notification)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Acceptance notification created");
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating acceptance notification", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    /**
     * Dohvata nepročitane notifikacije o prihvaćenim pozivima za vođu
     */
    public void getUnseenAcceptanceNotifications(String leaderEmail, OnAcceptanceNotificationsListener listener) {
        firestore.collection("acceptance_notifications")
                .whereEqualTo("leaderEmail", leaderEmail)
                .whereEqualTo("seen", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<AcceptanceNotification> notifications = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        AcceptanceNotification notification = doc.toObject(AcceptanceNotification.class);
                        if (notification != null) {
                            notifications.add(notification);
                        }
                    }
                    Log.d(TAG, "Found " + notifications.size() + " unseen acceptance notifications");
                    if (listener != null) listener.onSuccess(notifications);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching acceptance notifications", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    /**
     * Označava notifikaciju kao pročitanu
     */
    public void markAcceptanceNotificationAsSeen(String notificationId, OnOperationListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("seen", true);

        firestore.collection("acceptance_notifications")
                .document(notificationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification marked as seen");
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error marking notification as seen", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    // Novi callback interfejs
    public interface OnAcceptanceNotificationsListener {
        void onSuccess(List<AcceptanceNotification> notifications);
        void onFailure(String error);
    }
    public interface OnAllianceCreatedListener {
        void onSuccess(Alliance alliance);
        void onFailure(String error);
    }
    public interface OnAllianceFetchedListener {
        void onSuccess(Alliance alliance);
        void onFailure(String error);
    }
    public interface OnInvitationsFetchedListener {
        void onSuccess(List<AllianceInvitation> invitations);
        void onFailure(String error);
    }
    public interface OnOperationListener {
        void onSuccess();
        void onFailure(String error);
    }
}