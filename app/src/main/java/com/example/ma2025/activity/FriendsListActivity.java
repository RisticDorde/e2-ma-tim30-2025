package com.example.ma2025.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ma2025.R;
import com.example.ma2025.model.Friend;
import com.example.ma2025.model.User;
import com.example.ma2025.repository.UserRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendsAdapter adapter;
    private List<Friend> friendList = new ArrayList<>();
    private UserRepository userRepository;
    private User currentUser;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("FRIENDS_DEBUG", "onCreate() start");
        setContentView(R.layout.activity_friends_list);

        recyclerView = findViewById(R.id.friends_recycler_view);
        Log.d("FRIENDS_DEBUG", "RecyclerView initialized");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userRepository = new UserRepository(this);
        firestore = FirebaseFirestore.getInstance();
        currentUser = userRepository.getCurrentAppUser(this);
        Log.d("FRIENDS_DEBUG", "Current user: " + (currentUser != null ? currentUser.getEmail() : "NULL"));

        adapter = new FriendsAdapter(friendList, new FriendsAdapter.OnFriendClickListener() {
            @Override
            public void onProfileClick(String email) {
                Log.d("FRIENDS_DEBUG", "Profile click: " + email);
                Intent intent = new Intent(FriendsListActivity.this, ProfileActivity.class);
                intent.putExtra("viewed_email", email);
                startActivity(intent);
            }

            @Override
            public void onAddFriendClick(String userId) {
                Toast.makeText(FriendsListActivity.this, "Friend added!", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        loadAllUsers();

    }


    private void loadAllUsers() {
        Log.d("FRIENDS_DEBUG", "STEP 1: In loadAllUsers()");

        //  Povuci sve korisnike iz lokalne baze
        List<User> localUsers = userRepository.getAllUsers();
        Log.d("FRIENDS_DEBUG", "STEP 2: Local users fetched, count = " + (localUsers != null ? localUsers.size() : 0));

        if (localUsers == null) {
            Log.e("FRIENDS_DEBUG", "ERROR: localUsers == null!");
            return;
        }

        List<User> filteredUsers = new ArrayList<>();
        for (User u : localUsers) {
            if (u == null) {
                Log.e("FRIENDS_DEBUG", "ERROR: found null user in localUsers");
                continue;
            }
            if (u.getEmail() == null) {
                Log.e("FRIENDS_DEBUG", "ERROR: user with null email: " + u);
                continue;
            }
            if (!u.getEmail().equals(currentUser.getEmail())) {
                filteredUsers.add(u);
            }
        }
        Log.d("FRIENDS_DEBUG", "STEP 3: Filtered users count = " + filteredUsers.size());

        // ako ih ima u lokalnoj bazi
        if (!filteredUsers.isEmpty()) {
            Log.d("FRIENDS_DEBUG", "STEP 4: Populating adapter from local DB");
            populateAdapter(filteredUsers);
           // return;
        }

        // ako nema — povuci iz Firestore
        Log.d("FRIENDS_DEBUG", "STEP 5: No local users, fetching from Firestore...");
        firestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FRIENDS_DEBUG", "STEP 6: Firestore fetch successful, count = " + task.getResult().size());
                        List<User> usersFromFirestore = new ArrayList<>();

                        for (DocumentSnapshot doc : task.getResult()) {
                            User user = doc.toObject(User.class);
                            if (user != null && user.getEmail() != null && !user.getEmail().equals(currentUser.getEmail())) {
                                usersFromFirestore.add(user);
                                Log.d("FRIENDS_DEBUG", "STEP 7: Added user from Firestore: " + user.getEmail());
                            } else {
                                Log.d("FRIENDS_DEBUG", "STEP 7: Skipped null or current user");
                            }
                        }

                        Log.d("FRIENDS_DEBUG", "STEP 8: Firestore users filtered count = " + usersFromFirestore.size());

                        // Sačuvaj u lokalnu SQLite bazu
                        for (User u : usersFromFirestore) {
                            if (!userRepository.emailExists(u.getEmail())) {
                                userRepository.createUser(u);
                                Log.d("FRIENDS_DEBUG", "STEP 9: Saved new user to local DB: " + u.getEmail());
                            }
                        }

                        populateAdapter(usersFromFirestore);
                        Log.d("FRIENDS_DEBUG", "STEP 10: Adapter populated with Firestore users");

                    } else {
                        Log.e("FRIENDS_DEBUG", "ERROR: Firestore fetch failed", task.getException());
                        Toast.makeText(FriendsListActivity.this,
                                "Failed to load users", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void populateAdapter(List<User> users) {
        friendList.clear();
        for (User u : users) {
            friendList.add(new Friend(u.getId()+"", u.getUsername(), u.getAvatar(), u.getEmail()));
        }
        adapter.notifyDataSetChanged();
    }


}
