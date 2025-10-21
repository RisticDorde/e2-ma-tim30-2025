package com.example.ma2025.repository;

import android.content.Context;
import android.util.Log;

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