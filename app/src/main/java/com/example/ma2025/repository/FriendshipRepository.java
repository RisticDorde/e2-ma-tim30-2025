package com.example.ma2025.repository;

import android.content.Context;
import android.util.Log;

import com.example.ma2025.model.Friendship;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendshipRepository {

    private static final String TAG = "FriendshipRepository";
    private FirebaseFirestore firestore;
    private Context context;

    public FriendshipRepository(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Dodaje prijatelja (obostrano - automatski accepted)
     */
    public void addFriend(String currentUserId, String currentUserEmail, String currentUsername,
                          String friendId, String friendEmail, String friendUsername,
                          OnOperationListener listener) {

        // Kreiraj prijateljstvo (automatski accepted)
        Friendship friendship = new Friendship(
                null,
                currentUserId,
                currentUserEmail,
                currentUsername,
                friendId,
                friendEmail,
                friendUsername
        );
        friendship.accept(); // Odmah postavi kao prihvaćeno

        String friendshipId = firestore.collection("friendships").document().getId();
        friendship.setId(friendshipId);

        firestore.collection("friendships")
                .document(friendshipId)
                .set(friendship)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Friend added successfully");
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding friend", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    /**
     * Uklanja prijatelja
     */
    public void removeFriend(String currentUserEmail, String friendEmail, OnOperationListener listener) {
        firestore.collection("friendships")
                .whereEqualTo("fromUserEmail", currentUserEmail)
                .whereEqualTo("toUserEmail", friendEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        firestore.collection("friendships")
                                .document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Friend removed");
                                    if (listener != null) listener.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    if (listener != null) listener.onFailure(e.getMessage());
                                });
                    } else {
                        // Proveri i obrnuti smer
                        removeFriendReverse(currentUserEmail, friendEmail, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    private void removeFriendReverse(String currentUserEmail, String friendEmail, OnOperationListener listener) {
        firestore.collection("friendships")
                .whereEqualTo("toUserEmail", currentUserEmail)
                .whereEqualTo("fromUserEmail", friendEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        firestore.collection("friendships")
                                .document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    if (listener != null) listener.onSuccess();
                                });
                    }
                });
    }

    /**
     * Proverava da li su dva korisnika prijatelji
     */
    public void areFriends(String userEmail1, String userEmail2, OnFriendshipCheckListener listener) {
        firestore.collection("friendships")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean areFriends = false;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Friendship fs = doc.toObject(Friendship.class);
                        if (fs != null) {
                            boolean match1 = fs.getFromUserEmail().equals(userEmail1) && fs.getToUserEmail().equals(userEmail2);
                            boolean match2 = fs.getFromUserEmail().equals(userEmail2) && fs.getToUserEmail().equals(userEmail1);
                            if (match1 || match2) {
                                areFriends = true;
                                break;
                            }
                        }
                    }
                    if (listener != null) listener.onResult(areFriends);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking friendship", e);
                    if (listener != null) listener.onResult(false);
                });
    }

    /**
     * Dohvata sve prijatelje korisnika (email-ove)
     */
    public void getUserFriendEmails(String userEmail, OnFriendEmailsListener listener) {
        firestore.collection("friendships")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> friendEmails = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Friendship fs = doc.toObject(Friendship.class);
                        if (fs != null) {
                            if (fs.getFromUserEmail().equals(userEmail)) {
                                friendEmails.add(fs.getToUserEmail());
                            } else if (fs.getToUserEmail().equals(userEmail)) {
                                friendEmails.add(fs.getFromUserEmail());
                            }
                        }
                    }
                    Log.d(TAG, "Found " + friendEmails.size() + " friends for " + userEmail);
                    if (listener != null) listener.onSuccess(friendEmails);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching friends", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    // Callback interfejsi
    public interface OnOperationListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnFriendshipCheckListener {
        void onResult(boolean areFriends);
    }

    public interface OnFriendEmailsListener {
        void onSuccess(List<String> friendEmails);
        void onFailure(String error);
    }
}