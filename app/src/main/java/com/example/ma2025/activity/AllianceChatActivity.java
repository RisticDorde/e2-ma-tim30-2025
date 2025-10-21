package com.example.ma2025.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ma2025.R;
import com.example.ma2025.adapter.MessageAdapter;
import com.example.ma2025.model.Alliance;
import com.example.ma2025.model.AllianceMessage;
import com.example.ma2025.model.User;
import com.example.ma2025.repository.AllianceRepository;
import com.example.ma2025.repository.UserRepository;
import com.google.firebase.firestore.ListenerRegistration;

public class AllianceChatActivity extends AppCompatActivity {

    private static final String TAG = "AllianceChatActivity";

    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private TextView allianceNameTextView;
    private TextView memberCountTextView;

    private MessageAdapter messageAdapter;
    private AllianceRepository allianceRepository;
    private UserRepository userRepository;

    private Alliance currentAlliance;
    private User currentUser;
    private ListenerRegistration messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_chat);

        // Omogući back dugme u action bar-u
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Alliance Chat");
        }

        // Inicijalizuj repozitorijume
        allianceRepository = new AllianceRepository(this);
        userRepository = new UserRepository(this);
        currentUser = userRepository.getCurrentAppUser(this);

        if (currentUser == null) {
            Toast.makeText(this, "Error: No user logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicijalizuj UI elemente
        initViews();
        setupRecyclerView();

        // Učitaj savez
        loadAlliance();

        // Postavi listener za send dugme
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void initViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        allianceNameTextView = findViewById(R.id.tv_alliance_name);
        memberCountTextView = findViewById(R.id.tv_member_count);
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(this, String.valueOf(currentUser.getId()));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Scroll na dno

        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void loadAlliance() {
        allianceRepository.getUserAlliance(currentUser.getEmail(), new AllianceRepository.OnAllianceFetchedListener() {
            @Override
            public void onSuccess(Alliance alliance) {
                if (alliance != null) {
                    currentAlliance = alliance;
                    updateHeader();
                    loadMessages();
                    startListeningForNewMessages();
                } else {
                    Toast.makeText(AllianceChatActivity.this, "You are not in any alliance", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AllianceChatActivity.this, "Error loading alliance: " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading alliance: " + error);
            }
        });
    }

    private void updateHeader() {
        if (currentAlliance != null) {
            allianceNameTextView.setText(currentAlliance.getName());
            memberCountTextView.setText(currentAlliance.getMemberCount() + " members");
        }
    }

    private void loadMessages() {
        if (currentAlliance == null) return;

        allianceRepository.getAllianceMessages(currentAlliance.getId(), new AllianceRepository.OnMessagesFetchedListener() {
            @Override
            public void onSuccess(java.util.List<AllianceMessage> messages) {
                messageAdapter.setMessages(messages);
                scrollToBottom();
                Log.d(TAG, "Loaded " + messages.size() + " messages");
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AllianceChatActivity.this, "Error loading messages: " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading messages: " + error);
            }
        });
    }

    private void startListeningForNewMessages() {
        if (currentAlliance == null) return;

        // Ovo je REAL-TIME listener - reaguje čim neko pošalje poruku!
        messageListener = allianceRepository.listenForNewMessages(currentAlliance.getId(),
                new AllianceRepository.OnNewMessageListener() {
                    @Override
                    public void onNewMessage(AllianceMessage message) {
                        // Dodaj poruku u adapter
                        messageAdapter.addMessage(message);
                        scrollToBottom();

                        // Ovde možeš dodati zvuk notifikacije ili vibraciju
                        Log.d(TAG, "New message from: " + message.getSenderUsername());
                    }
                });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Cannot send empty message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentAlliance == null) {
            Toast.makeText(this, "No alliance loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kreiraj poruku
        AllianceMessage message = new AllianceMessage(
                currentAlliance.getId(),
                String.valueOf(currentUser.getId()),
                currentUser.getEmail(),
                currentUser.getUsername(),
                currentUser.getAvatar(), // Ako imaš avatar u User modelu
                messageText
        );

        // Pošalji poruku
        allianceRepository.sendMessage(message, new AllianceRepository.OnOperationListener() {
            @Override
            public void onSuccess() {
                // Poruka uspešno poslata
                messageEditText.setText(""); // Očisti input polje
                Log.d(TAG, "Message sent successfully");

                // Real-time listener će automatski dodati poruku u RecyclerView!
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AllianceChatActivity.this, "Failed to send message: " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error sending message: " + error);
            }
        });
    }

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // VAŽNO: Ukloni listener kad korisnik napusti čet
        if (messageListener != null) {
            messageListener.remove();
            Log.d(TAG, "Message listener removed");
        }
    }
}