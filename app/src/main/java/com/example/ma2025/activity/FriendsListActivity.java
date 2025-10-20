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
        setContentView(R.layout.activity_friends_list);

        recyclerView = findViewById(R.id.friends_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userRepository = new UserRepository(this);
        firestore = FirebaseFirestore.getInstance();
        currentUser = userRepository.getCurrentAppUser(this);

        adapter = new FriendsAdapter(friendList, new FriendsAdapter.OnFriendClickListener() {
            @Override
            public void onProfileClick(String userId) {
                Intent intent = new Intent(FriendsListActivity.this, ProfileActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }

            @Override
            public void onAddFriendClick(String userId) {
                // Ovde ćemo dodati logiku za dodavanje prijatelja
                Toast.makeText(FriendsListActivity.this,
                        "Friend added (logic pending)!", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        loadAllUsers();
    }

    private void loadAllUsers() {
        // 1️⃣ Povuci sve korisnike iz lokalne baze
        List<User> localUsers = userRepository.getAllUsers();
        List<User> filteredUsers = new ArrayList<>();

        for (User u : localUsers) {
            if (!u.getEmail().equals(currentUser.getEmail())) {
                filteredUsers.add(u);
            }
        }

        if (!filteredUsers.isEmpty()) {
            populateAdapter(filteredUsers);
        } else {
            // 2️⃣ Ako nema u SQLite, povuci iz Firestore
            firestore.collection("users")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<User> usersFromFirestore = new ArrayList<>();
                            for (DocumentSnapshot doc : task.getResult()) {
                                User user = doc.toObject(User.class);
                                if (user != null && !user.getEmail().equals(currentUser.getEmail())) {
                                    usersFromFirestore.add(user);
                                }
                            }

                            // Sačuvaj u lokalnu SQLite bazu
                            for (User u : usersFromFirestore) {
                                if (!userRepository.emailExists(u.getEmail())) {
                                    userRepository.createUser(u);
                                }
                            }

                            populateAdapter(usersFromFirestore);
                        } else {
                            Log.e("FriendsList", "Error fetching users from Firestore", task.getException());
                            Toast.makeText(FriendsListActivity.this,
                                    "Failed to load users", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void populateAdapter(List<User> users) {
        friendList.clear();
        for (User u : users) {
            friendList.add(new Friend(u.getId()+"", u.getUsername(), u.getAvatar()));
        }
        adapter.notifyDataSetChanged();
    }


}
