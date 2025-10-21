package com.example.ma2025.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ma2025.R;

import com.example.ma2025.model.Alliance;
import com.example.ma2025.model.AllianceInvitation;
import com.example.ma2025.model.Friend;
import com.example.ma2025.model.User;
import com.example.ma2025.repository.AllianceRepository;
import com.example.ma2025.repository.FriendshipRepository;
import com.example.ma2025.repository.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendsAdapter adapter;
    private List<Friend> friendList = new ArrayList<>();
    private List<Friend> allUsersList = new ArrayList<>();
    private List<Friend> myFriendsList = new ArrayList<>();
    private Map<String, Boolean> friendshipStatusMap = new HashMap<>();

    private UserRepository userRepository;
    private AllianceRepository allianceRepository;
    private FriendshipRepository friendshipRepository;
    private User currentUser;
    private FirebaseFirestore firestore;
    private TextInputEditText searchEditText;
    private MaterialButton btnCreateAlliance, btnMyAlliance;
    private TabLayout tabLayout;

    private boolean showingFriendsOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("FRIENDS_DEBUG", "onCreate() start");
        setContentView(R.layout.activity_friends_list);

        recyclerView = findViewById(R.id.friends_recycler_view);
        searchEditText = findViewById(R.id.search_edit_text);
        btnCreateAlliance = findViewById(R.id.btn_create_alliance);
        btnMyAlliance = findViewById(R.id.btn_my_alliance);
        tabLayout = findViewById(R.id.tab_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userRepository = new UserRepository(this);
        allianceRepository = new AllianceRepository(this);
        friendshipRepository = new FriendshipRepository(this);
        firestore = FirebaseFirestore.getInstance();
        currentUser = userRepository.getCurrentAppUser(this);

        adapter = new FriendsAdapter(friendList, new FriendsAdapter.OnFriendClickListener() {
            @Override
            public void onProfileClick(String email) {
                Intent intent = new Intent(FriendsListActivity.this, ProfileActivity.class);
                intent.putExtra("viewed_email", email);
                startActivity(intent);
            }

            @Override
            public void onAddFriendClick(Friend friend) {
                addFriend(friend);
            }

            @Override
            public void onRemoveFriendClick(Friend friend) {
                removeFriend(friend);
            }
        });

        recyclerView.setAdapter(adapter);

        setupSearchListener();
        setupButtons();
        setupTabs();
        loadAllUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMyAllianceButton();
        loadFriendships(); // Osveži prijateljstva
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Svi Korisnici"));
        tabLayout.addTab(tabLayout.newTab().setText("Prijatelji"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showingFriendsOnly = tab.getPosition() == 1;
                filterUsers(searchEditText.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupButtons() {
        btnCreateAlliance.setOnClickListener(v -> checkUserAllianceAndProceed());

        btnMyAlliance.setOnClickListener(v -> {
            Intent intent = new Intent(FriendsListActivity.this, AllianceViewActivity.class);
            startActivity(intent);
        });

        updateMyAllianceButton();
    }

    private void updateMyAllianceButton() {
        allianceRepository.getUserAlliance(currentUser.getEmail(), new AllianceRepository.OnAllianceFetchedListener() {
            @Override
            public void onSuccess(Alliance alliance) {
                if (alliance != null) {
                    btnMyAlliance.setVisibility(View.VISIBLE);
                    btnMyAlliance.setText("Moj Savez: " + alliance.getName());
                } else {
                    btnMyAlliance.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String error) {
                btnMyAlliance.setVisibility(View.GONE);
            }
        });
    }

    private void addFriend(Friend friend) {
        friendshipRepository.addFriend(
                currentUser.getId() + "",
                currentUser.getEmail(),
                currentUser.getUsername(),
                friend.getId(),
                friend.getEmail(),
                friend.getUsername(),
                new FriendshipRepository.OnOperationListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(FriendsListActivity.this,
                                "Prijatelj dodat: " + friend.getUsername(),
                                Toast.LENGTH_SHORT).show();
                        loadFriendships(); // Osveži listu
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(FriendsListActivity.this,
                                "Greška: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeFriend(Friend friend) {
        new AlertDialog.Builder(this)
                .setTitle("Ukloni prijatelja")
                .setMessage("Da li si siguran da želiš da ukloniš " + friend.getUsername() + " iz prijatelja?")
                .setPositiveButton("Da", (dialog, which) -> {
                    friendshipRepository.removeFriend(
                            currentUser.getEmail(),
                            friend.getEmail(),
                            new FriendshipRepository.OnOperationListener() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(FriendsListActivity.this,
                                            "Prijatelj uklonjen",
                                            Toast.LENGTH_SHORT).show();
                                    loadFriendships(); // Osveži listu
                                }

                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(FriendsListActivity.this,
                                            "Greška: " + error,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Ne", null)
                .show();
    }

    private void loadFriendships() {
        friendshipRepository.getUserFriendEmails(currentUser.getEmail(),
                new FriendshipRepository.OnFriendEmailsListener() {
                    @Override
                    public void onSuccess(List<String> friendEmails) {
                        // Ažuriraj mapu statusa prijateljstva
                        friendshipStatusMap.clear();
                        for (String email : friendEmails) {
                            friendshipStatusMap.put(email, true);
                        }

                        // Ažuriraj listu "Moji prijatelji"
                        myFriendsList.clear();
                        for (Friend friend : allUsersList) {
                            if (friendshipStatusMap.containsKey(friend.getEmail())) {
                                myFriendsList.add(friend);
                            }
                        }

                        adapter.setFriendshipStatusMap(friendshipStatusMap);

                        // Osveži prikaz
                        filterUsers(searchEditText.getText().toString());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("FRIENDS_DEBUG", "Error loading friendships: " + error);
                    }
                });
    }

    private void checkUserAllianceAndProceed() {
        allianceRepository.getUserAlliance(currentUser.getEmail(), new AllianceRepository.OnAllianceFetchedListener() {
            @Override
            public void onSuccess(Alliance alliance) {
                if (alliance != null) {
                    Toast.makeText(FriendsListActivity.this,
                            "Već si član saveza: " + alliance.getName(),
                            Toast.LENGTH_LONG).show();
                } else {
                    showAllianceNameDialog();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(FriendsListActivity.this,
                        "Greška: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAllianceNameDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_alliance_name, null);
        TextInputEditText allianceNameInput = dialogView.findViewById(R.id.alliance_name_input);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Dalje", (dialog, which) -> {
                    String allianceName = allianceNameInput.getText().toString().trim();

                    if (TextUtils.isEmpty(allianceName)) {
                        Toast.makeText(this, "Unesi naziv saveza", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showFriendSelectionDialog(allianceName);
                })
                .setNegativeButton("Otkaži", null)
                .show();
    }

    private void showFriendSelectionDialog(String allianceName) {
        // SAMO PRIJATELJI MOGU BITI POZVANI
        if (myFriendsList.isEmpty()) {
            Toast.makeText(this, "Nemaš prijatelja da pozivaš u savez", Toast.LENGTH_LONG).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_friends, null);
        RecyclerView friendsRecycler = dialogView.findViewById(R.id.friends_selection_recycler);

        friendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        FriendSelectionAdapter selectionAdapter = new FriendSelectionAdapter(myFriendsList);
        friendsRecycler.setAdapter(selectionAdapter);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Kreiraj Savez", (dialog, which) -> {
                    List<Friend> selectedFriends = selectionAdapter.getSelectedFriends();

                    if (selectedFriends.isEmpty()) {
                        Toast.makeText(this,
                                "Odaberi bar jednog prijatelja",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createAllianceAndSendInvitations(allianceName, selectedFriends);
                })
                .setNegativeButton("Nazad", null)
                .show();
    }

    private void createAllianceAndSendInvitations(String allianceName, List<Friend> selectedFriends) {
        Toast.makeText(this, "Kreiram savez...", Toast.LENGTH_SHORT).show();

        Alliance alliance = new Alliance(
                null,
                allianceName,
                currentUser.getId() + "",
                currentUser.getEmail()
        );

        allianceRepository.createAlliance(alliance, new AllianceRepository.OnAllianceCreatedListener() {
            @Override
            public void onSuccess(Alliance createdAlliance) {
                sendInvitationsToFriends(createdAlliance, selectedFriends);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(FriendsListActivity.this,
                        "Greška pri kreiranju saveza: " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendInvitationsToFriends(Alliance alliance, List<Friend> friends) {
        int totalInvitations = friends.size();
        final int[] sentCount = {0};

        for (Friend friend : friends) {
            AllianceInvitation invitation = new AllianceInvitation(
                    null,
                    alliance.getId(),
                    alliance.getName(),
                    currentUser.getId() + "",
                    currentUser.getEmail(),
                    currentUser.getUsername(),
                    friend.getId(),
                    friend.getEmail()
            );

            allianceRepository.sendInvitation(invitation, new AllianceRepository.OnOperationListener() {
                @Override
                public void onSuccess() {
                    sentCount[0]++;

                    if (sentCount[0] == totalInvitations) {
                        Toast.makeText(FriendsListActivity.this,
                                "Savez kreiran! Pozivi poslati.",
                                Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(FriendsListActivity.this, AllianceViewActivity.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e("ALLIANCE_DEBUG", "Failed to send invitation: " + error);
                }
            });
        }
    }

    private void filterUsers(String query) {
        friendList.clear();

        // Odaberi izvora podataka na osnovu taba
        List<Friend> sourceList = showingFriendsOnly ? myFriendsList : allUsersList;

        if (query.isEmpty()) {
            friendList.addAll(sourceList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Friend friend : sourceList) {
                if (friend.getUsername() != null &&
                        friend.getUsername().toLowerCase().contains(lowerCaseQuery)) {
                    friendList.add(friend);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void loadAllUsers() {
        List<User> localUsers = userRepository.getAllUsers();

        if (localUsers == null) {
            return;
        }

        List<User> filteredUsers = new ArrayList<>();
        for (User u : localUsers) {
            if (u != null && u.getEmail() != null && !u.getEmail().equals(currentUser.getEmail())) {
                filteredUsers.add(u);
            }
        }

        if (!filteredUsers.isEmpty()) {
            populateAdapter(filteredUsers);
        }

        firestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> usersFromFirestore = new ArrayList<>();

                        for (DocumentSnapshot doc : task.getResult()) {
                            User user = doc.toObject(User.class);
                            if (user != null && user.getEmail() != null && !user.getEmail().equals(currentUser.getEmail())) {
                                usersFromFirestore.add(user);
                            }
                        }

                        for (User u : usersFromFirestore) {
                            if (!userRepository.emailExists(u.getEmail())) {
                                userRepository.createUser(u);
                            }
                        }

                        populateAdapter(usersFromFirestore);
                        loadFriendships(); // Učitaj prijateljstva nakon učitavanja korisnika

                    } else {
                        Toast.makeText(FriendsListActivity.this,
                                "Failed to load users", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateAdapter(List<User> users) {
        allUsersList.clear();
        friendList.clear();

        for (User u : users) {
            Friend friend = new Friend(u.getId()+"", u.getUsername(), u.getAvatar(), u.getEmail());
            allUsersList.add(friend);
            friendList.add(friend);
        }

        adapter.notifyDataSetChanged();
    }
}