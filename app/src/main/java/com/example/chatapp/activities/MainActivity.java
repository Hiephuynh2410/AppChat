package com.example.chatapp.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.Nullable;

import com.example.chatapp.R;
import com.example.chatapp.adapter.RecentConversationAdapter;
import com.example.chatapp.databinding.ActivityMainBinding;
import com.example.chatapp.listeners.ConversionListener;
import com.example.chatapp.models.ChatMess;
import com.example.chatapp.models.User;
import com.example.chatapp.utilities.PreferenceManager;
import com.example.chatapp.utilities.constant;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends BaseActivity  implements ConversionListener {

    private ActivityMainBinding binding;

    private PreferenceManager preferenceManager;
    private List<ChatMess> conversation;
    private RecentConversationAdapter conversationAdapter;
    private FirebaseFirestore db;
    private RoundedImageView imgview;
    private static final int REQUEST_CODE_EDIT_PROFILE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUser();
        getToken();
        setListener();
        listenConversation();
        imgview = findViewById(R.id.imageProfile);
        imgview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), profileImage.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void openProfileImageActivity() {
        Intent intent = new Intent(MainActivity.this, profileImage.class);
        startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == RESULT_OK) {
            // Handle the updated data here
            if (data != null) {
                String updatedName = data.getStringExtra("name");
                String updatedEmail = data.getStringExtra("email");
                String updatedPhoneNumber = data.getStringExtra("phoneNumber");

                // Update the UI with the updated data
                binding.textName.setText(updatedName);
            }
        }
    }
    private  void init() {
        conversation = new ArrayList<>();
        conversationAdapter = new RecentConversationAdapter(conversation, this);
        binding.conversationRecyclerView.setAdapter(conversationAdapter );
        db = FirebaseFirestore.getInstance();
    }

    private  void setListener() {
        binding.imageSignOut.setOnClickListener(v -> SignOut());
        binding.fabnewchat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UserActivity.class)));
    }
    private void loadUser() {
        binding.textName.setText(preferenceManager.getString(constant.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(constant.KEY_IMAGE),  Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String mess){
        Toast.makeText(getApplicationContext(), mess, Toast.LENGTH_SHORT).show();
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::upadateToken);
    }

    private void listenConversation() {
        db.collection(constant.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(constant.KEY_SENDER_ID, preferenceManager.getString(constant.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        db.collection(constant.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(constant.KEY_RECEIVER_ID, preferenceManager.getString(constant.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null) {
            return;
        }
        if(value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(constant.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(constant.KEY_RECEIVER_ID);
                    ChatMess chatMess = new ChatMess();
                    chatMess.senderId = senderId;
                    chatMess.receivedId = receiverId;
                    if(preferenceManager.getString(constant.KEY_USER_ID).equals(senderId)) {
                        chatMess.conversionImage = documentChange.getDocument().getString(constant.KEY_RECEIVER_IMAGE);
                        chatMess.conversionName = documentChange.getDocument().getString(constant.KEY_RECEIVER_NAME);
                        chatMess.conversionId = documentChange.getDocument().getString(constant.KEY_RECEIVER_ID);
                    } else {
                        chatMess.conversionImage = documentChange.getDocument().getString(constant.KEY_SENDER_IMAGE);
                        chatMess.conversionName = documentChange.getDocument().getString(constant.KEY_SENDER_NAME);
                        chatMess.conversionId = documentChange.getDocument().getString(constant.KEY_SENDER_ID);
                    }
                    chatMess.message = documentChange.getDocument().getString(constant.KEY_LAST_MESSAGE);
                    chatMess.dateObject = documentChange.getDocument().getDate(constant.KEY_TIMESTAMP);
                    conversation.add(chatMess);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i< conversation.size(); i++ ) {
                        String senderId = documentChange.getDocument().getString(constant.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(constant.KEY_RECEIVER_ID);
                        if(conversation.get(i).senderId.equals(senderId) && conversation.get(i).receivedId.equals(receiverId)) {
                            conversation.get(i).message = documentChange.getDocument().getString(constant.KEY_LAST_MESSAGE);
                            conversation.get(i).dateObject = documentChange.getDocument().getDate(constant.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversation, (obj1, obj2) -> obj1.dateObject.compareTo(obj1.dateObject));
            conversationAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.progressbar.setVisibility(View.GONE);
        }
    };

    private void upadateToken(String token) {
        preferenceManager.putString(constant.KEY_FCM_TOKEN, token);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection(constant.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(constant.KEY_USER_ID)
        );
        documentReference.update(constant.KEY_FCM_TOKEN, token).addOnFailureListener(e -> showToast("unable update token"));
    }

    private void SignOut() {
        showToast("Signing out...");

        // Xóa token FCM từ Firestore
        String userId = preferenceManager.getString(constant.KEY_USER_ID);
        if (userId != null && !userId.isEmpty()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection(constant.KEY_COLLECTION_USERS).document(userId);

            HashMap<String, Object> updates = new HashMap<>();
            updates.put(constant.KEY_FCM_TOKEN, FieldValue.delete());

            documentReference.update(updates)
                    .addOnSuccessListener(unused -> {
                        // Xóa thông tin người dùng khỏi SharedPreferences
                        preferenceManager.clear();
                        // Chuyển đến màn hình đăng nhập (SignInActivity)
                        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> showToast("Unable to sign out. Please try again."));
        } else {
            showToast("User ID is null or empty. Unable to sign out.");
        }
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), chatActivity.class);
        intent.putExtra(constant.KEY_USER, user);
        startActivity(intent);
    }
}