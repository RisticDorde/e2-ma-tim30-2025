package com.example.ma2025.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ma2025.R;
import com.example.ma2025.adapter.AllianceMemberAdapter;
import com.example.ma2025.adapter.FriendSelectionAdapter;
import com.example.ma2025.model.Alliance;
import com.example.ma2025.model.AllianceInvitation;
import com.example.ma2025.model.Friend;
import com.example.ma2025.model.User;
import com.example.ma2025.repository.AllianceRepository;
import com.example.ma2025.repository.FriendshipRepository;
import com.example.ma2025.repository.UserRepository;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllianceViewActivity extends AppCompatActivity {

    private TextView tvAllianceName, tvLeaderInfo, tvMemberCount, tvMissionStatus;
    private RecyclerView recyclerMembers;
    private MaterialButton btnOpenChat, btnLeaveAlliance, btnDisbandAlliance, btnInviteMembers, btnBack;

    private AllianceRepository allianceRepository;
    private UserRepository userRepository;
    private FriendshipRepository friendshipRepository;
    private User currentUser;
    private Alliance currentAlliance;
    private List<User> membersList = new ArrayList<>();
    private AllianceMemberAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_view);

        tvAllianceName = findViewById(R.id.tv_alliance_name);
        tvLeaderInfo = findViewById(R.id.tv_leader_info);
        tvMemberCount = findViewById(R.id.tv_member_count);
        tvMissionStatus = findViewById(R.id.tv_mission_status);
        recyclerMembers = findViewById(R.id.recycler_members);
        btnOpenChat = findViewById(R.id.btn_open_chat);
        btnLeaveAlliance = findViewById(R.id.btn_leave_alliance);
        btnDisbandAlliance = findViewById(R.id.btn_disband_alliance);
        btnInviteMembers = findViewById(R.id.btn_invite_members);
        btnBack = findViewById(R.id.btn_back);

        allianceRepository = new AllianceRepository(this);
        userRepository = new UserRepository(this);
        friendshipRepository = new FriendshipRepository(this);
        currentUser = userRepository.getCurrentAppUser(this);

        recyclerMembers.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());
        btnOpenChat.setOnClickListener(v -> openAllianceChat());
        btnLeaveAlliance.setOnClickListener(v -> confirmLeaveAlliance());
        btnDisbandAlliance.setOnClickListener(v -> confirmDisbandAlliance());
        btnInviteMembers.setOnClickListener(v -> showInviteFriendsDialog());

        loadAllianceData();
    }

    /**
     * Otvara chat stranicu saveza
     */
    private void openAllianceChat() {
        if (currentAlliance != null) {
            Intent intent = new Intent(this, AllianceChatActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Savez nije učitan", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAllianceData() {
        allianceRepository.getUserAlliance(currentUser.getEmail(), new AllianceRepository.OnAllianceFetchedListener() {
            @Override
            public void onSuccess(Alliance alliance) {
                if (alliance != null) {
                    currentAlliance = alliance;
                    displayAllianceInfo(alliance);
                    loadMembers(alliance);
                } else {
                    Toast.makeText(AllianceViewActivity.this,
                            "Nisi član nijednog saveza",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AllianceViewActivity.this,
                        "Greška: " + error,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayAllianceInfo(Alliance alliance) {
        tvAllianceName.setText(alliance.getName());
        tvMemberCount.setText("Članovi: " + alliance.getMemberCount());

        String missionStatus = alliance.isMissionActive() ? "Aktivna" : "Neaktivna";
        tvMissionStatus.setText("Status misije: " + missionStatus);
        tvMissionStatus.setTextColor(getResources().getColor(
                alliance.isMissionActive() ?
                        android.R.color.holo_red_dark :
                        android.R.color.holo_green_dark
        ));

        boolean isLeader = alliance.isLeader(currentUser.getId() + "");

        if (isLeader) {
            btnLeaveAlliance.setVisibility(View.GONE);
            btnDisbandAlliance.setVisibility(View.VISIBLE);
            btnInviteMembers.setVisibility(View.VISIBLE);

            btnDisbandAlliance.setEnabled(!alliance.isMissionActive());
        } else {
            btnLeaveAlliance.setVisibility(View.VISIBLE);
            btnDisbandAlliance.setVisibility(View.GONE);
            btnInviteMembers.setVisibility(View.GONE);

            btnLeaveAlliance.setEnabled(!alliance.isMissionActive());
        }
    }

    private void loadMembers(Alliance alliance) {
        membersList.clear();

        User leader = userRepository.getUserByEmail(alliance.getLeaderEmail());
        if (leader != null) {
            membersList.add(leader);
            tvLeaderInfo.setText("Vođa: " + leader.getUsername());
        }

        for (String memberEmail : alliance.getMemberEmails()) {
            if (!memberEmail.equals(alliance.getLeaderEmail())) {
                User member = userRepository.getUserByEmail(memberEmail);
                if (member != null) {
                    membersList.add(member);
                }
            }
        }

        adapter = new AllianceMemberAdapter(membersList, alliance.getLeaderEmail());
        recyclerMembers.setAdapter(adapter);
    }

    private void showInviteFriendsDialog() {
        // Učitaj prijatelje trenutnog korisnika
        friendshipRepository.getUserFriendEmails(currentUser.getEmail(),
                new FriendshipRepository.OnFriendEmailsListener() {
                    @Override
                    public void onSuccess(List<String> friendEmails) {
                        if (friendEmails.isEmpty()) {
                            Toast.makeText(AllianceViewActivity.this,
                                    "Nemaš prijatelja da pozivaš",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Filtriraj prijatelje koji NISU u savezu
                        Set<String> currentMemberEmails = new HashSet<>(currentAlliance.getMemberEmails());
                        List<Friend> availableFriends = new ArrayList<>();

                        for (String friendEmail : friendEmails) {
                            if (!currentMemberEmails.contains(friendEmail)) {
                                User friendUser = userRepository.getUserByEmail(friendEmail);
                                if (friendUser != null) {
                                    Friend friend = new Friend(
                                            friendUser.getId() + "",
                                            friendUser.getUsername(),
                                            friendUser.getAvatar(),
                                            friendUser.getEmail()
                                    );
                                    availableFriends.add(friend);
                                }
                            }
                        }

                        if (availableFriends.isEmpty()) {
                            Toast.makeText(AllianceViewActivity.this,
                                    "Svi tvoji prijatelji su već u savezu",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        showFriendSelectionDialog(availableFriends);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(AllianceViewActivity.this,
                                "Greška pri učitavanju prijatelja: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showFriendSelectionDialog(List<Friend> availableFriends) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_friends, null);
        RecyclerView friendsRecycler = dialogView.findViewById(R.id.friends_selection_recycler);

        friendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        FriendSelectionAdapter selectionAdapter = new FriendSelectionAdapter(availableFriends);
        friendsRecycler.setAdapter(selectionAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Pozovi Prijatelje u Savez")
                .setView(dialogView)
                .setPositiveButton("Pošalji Pozive", (dialog, which) -> {
                    List<Friend> selectedFriends = selectionAdapter.getSelectedFriends();

                    if (selectedFriends.isEmpty()) {
                        Toast.makeText(this,
                                "Odaberi bar jednog prijatelja",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sendInvitationsToFriends(selectedFriends);
                })
                .setNegativeButton("Otkaži", null)
                .show();
    }

    private void sendInvitationsToFriends(List<Friend> friends) {
        int totalInvitations = friends.size();
        final int[] sentCount = {0};

        Toast.makeText(this, "Šaljem pozive...", Toast.LENGTH_SHORT).show();

        for (Friend friend : friends) {
            AllianceInvitation invitation = new AllianceInvitation(
                    null,
                    currentAlliance.getId(),
                    currentAlliance.getName(),
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
                        Toast.makeText(AllianceViewActivity.this,
                                "Pozivi uspešno poslati!",
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e("ALLIANCE_INVITE", "Failed to send invitation: " + error);
                    Toast.makeText(AllianceViewActivity.this,
                            "Greška pri slanju poziva",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void confirmLeaveAlliance() {
        if (currentAlliance.isMissionActive()) {
            Toast.makeText(this,
                    "Ne možeš napustiti savez dok je misija aktivna",
                    Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Napusti Savez")
                .setMessage("Da li si siguran da želiš da napustiš savez: " + currentAlliance.getName() + "?")
                .setPositiveButton("Da", (dialog, which) -> leaveAlliance())
                .setNegativeButton("Ne", null)
                .show();
    }

    private void leaveAlliance() {
        allianceRepository.removeMemberFromAlliance(
                currentAlliance.getId(),
                currentUser.getId() + "",
                currentUser.getEmail(),
                new AllianceRepository.OnOperationListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(AllianceViewActivity.this,
                                "Napustio/la si savez",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(AllianceViewActivity.this,
                                "Greška: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDisbandAlliance() {
        if (currentAlliance.isMissionActive()) {
            Toast.makeText(this,
                    "Ne možeš ukinuti savez dok je misija aktivna",
                    Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Ukini Savez")
                .setMessage("Da li si siguran da želiš da ukineš savez? Svi članovi će biti uklonjeni.")
                .setPositiveButton("Da", (dialog, which) -> disbandAlliance())
                .setNegativeButton("Ne", null)
                .show();
    }

    private void disbandAlliance() {
        allianceRepository.disbandAlliance(
                currentAlliance.getId(),
                new AllianceRepository.OnOperationListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(AllianceViewActivity.this,
                                "Savez ukinut",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(AllianceViewActivity.this,
                                "Greška: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}