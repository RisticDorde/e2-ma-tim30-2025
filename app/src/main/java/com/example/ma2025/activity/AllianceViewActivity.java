package com.example.ma2025.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ma2025.R;
import com.example.ma2025.model.Alliance;
import com.example.ma2025.model.User;
import com.example.ma2025.repository.AllianceRepository;
import com.example.ma2025.repository.UserRepository;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AllianceViewActivity extends AppCompatActivity {

    private TextView tvAllianceName, tvLeaderInfo, tvMemberCount, tvMissionStatus;
    private RecyclerView recyclerMembers;
    private MaterialButton btnLeaveAlliance, btnDisbandAlliance, btnBack;

    private AllianceRepository allianceRepository;
    private UserRepository userRepository;
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
        btnLeaveAlliance = findViewById(R.id.btn_leave_alliance);
        btnDisbandAlliance = findViewById(R.id.btn_disband_alliance);
        btnBack = findViewById(R.id.btn_back);

        allianceRepository = new AllianceRepository(this);
        userRepository = new UserRepository(this);
        currentUser = userRepository.getCurrentAppUser(this);

        recyclerMembers.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());
        btnLeaveAlliance.setOnClickListener(v -> confirmLeaveAlliance());
        btnDisbandAlliance.setOnClickListener(v -> confirmDisbandAlliance());

        loadAllianceData();
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

        // Prikaži odgovarajuća dugmad
        boolean isLeader = alliance.isLeader(currentUser.getId() + "");

        if (isLeader) {
            btnLeaveAlliance.setVisibility(View.GONE);
            btnDisbandAlliance.setVisibility(View.VISIBLE);
            // Može ukinuti savez samo ako misija nije aktivna
            btnDisbandAlliance.setEnabled(!alliance.isMissionActive());
        } else {
            btnLeaveAlliance.setVisibility(View.VISIBLE);
            btnDisbandAlliance.setVisibility(View.GONE);
            // Može napustiti savez samo ako misija nije aktivna
            btnLeaveAlliance.setEnabled(!alliance.isMissionActive());
        }
    }

    private void loadMembers(Alliance alliance) {
        membersList.clear();

        // Prvo dodaj vođu
        User leader = userRepository.getUserByEmail(alliance.getLeaderEmail());
        if (leader != null) {
            membersList.add(leader);
            tvLeaderInfo.setText("Vođa: " + leader.getUsername());
        }

        // Zatim dodaj ostale članove
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