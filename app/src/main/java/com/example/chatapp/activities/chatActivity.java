package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;

import com.example.chatapp.R;
import com.example.chatapp.adapter.ChatAdapter;
import com.example.chatapp.databinding.ActivityChatBinding;
import com.example.chatapp.models.ChatMess;
import com.example.chatapp.models.User;
import com.example.chatapp.utilities.PreferenceManager;
import com.example.chatapp.utilities.constant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class chatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMess> chatMesses;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();
        loadReceiver();
        init();
        listenMess();
        updateSendButtonState();
        binding.inutMess.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateSendButtonState();
            }
        });
    }

    private void sendMess() {
        // code  sử dụng để tạo và gửi một tin nhắn trong ứng dụng.
        // Tin nhắn bao gồm thông tin về người gửi, người nhận, nội dung và thời gian gửi.
        HashMap<String, Object> mess = new HashMap<>();
        mess.put(constant.KEY_SENDER_ID, preferenceManager.getString(constant.KEY_USER_ID));
        mess.put(constant.KEY_RECEIVER_ID, receiverUser.id);
        mess.put(constant.KEY_MESSAGE, binding.inutMess.getText().toString());
        mess.put(constant.KEY_TIMESTAMP, new Date());
        db.collection(constant.KEY_COLLECTION_CHAT).add(mess);
        if (conversionId != null) {
            updateConversion(binding.inutMess.getText().toString());
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(constant.KEY_SENDER_ID, preferenceManager.getString(constant.KEY_USER_ID));
            conversion.put(constant.KEY_SENDER_NAME, preferenceManager.getString(constant.KEY_NAME));
            conversion.put(constant.KEY_SENDER_IMAGE, preferenceManager.getString(constant.KEY_IMAGE));
            conversion.put(constant.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(constant.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(constant.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(constant.KEY_LAST_MESSAGE, binding.inutMess.getText().toString());
            conversion.put(constant.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }
        binding.inutMess.setText(null);
    }

    private Handler handler = new Handler();
    private Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            // Ẩn TextView sau 3 giây
            binding.textavailabilty.setVisibility(View.GONE);
            binding.textavailabiltyoff.setVisibility(View.GONE);
        }
    };

    private void listenAvailabilityOfReceiver() {
        db.collection(constant.KEY_COLLECTION_USERS).document(receiverUser.id).addSnapshotListener(chatActivity.this, (value, error) -> {
            if(error != null) {
                return;
            }
            if(value != null) {
                if(value.getLong(constant.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(value.getLong(constant.KEY_AVAILABILITY).intValue());
                    isReceiverAvailable = availability == 1;
                }
            }
            if(isReceiverAvailable) {
                binding.textavailabilty.setVisibility(View.VISIBLE);
                binding.textavailabiltyoff.setVisibility(View.GONE);

                // Lên lịch ẩn TextView sau 3 giây
                handler.postDelayed(hideRunnable, 2000);
            } else {
                binding.textavailabiltyoff.setVisibility(View.VISIBLE);
                binding.textavailabilty.setVisibility(View.GONE);

                // Lên lịch ẩn TextView sau 3 giây
                handler.postDelayed(hideRunnable, 2000);

            }
        });
    }

    private void listenMess() {
        db.collection(constant.KEY_COLLECTION_CHAT)
                .whereEqualTo(constant.KEY_SENDER_ID, preferenceManager.getString(constant.KEY_USER_ID))
                .whereEqualTo(constant.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        db.collection(constant.KEY_COLLECTION_CHAT)
                .whereEqualTo(constant.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(constant.KEY_RECEIVER_ID, preferenceManager.getString(constant.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private void init () {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMesses = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMesses, getBitmapFromEncodeString(receiverUser.image), preferenceManager.getString(constant.KEY_USER_ID));
        binding.chatRecylerView.setAdapter(chatAdapter);
        db = FirebaseFirestore.getInstance();
    }
    private Bitmap getBitmapFromEncodeString(String encodeimg) {
        byte[] bytes = Base64.decode(encodeimg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
    }

    private void loadReceiver() {
        //một phương thức trong một ứng dụng Android. Nó được sử dụng để tải thông tin người nhận
        receiverUser = (User) getIntent().getSerializableExtra(constant.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    private void setListener() {
        binding.imagesBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                onBackPressed();
                Intent intent = new Intent(chatActivity.this, UserActivity.class);
                startActivity(intent);
                finish();
            }
        });
        binding.layoutSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMess();
            }
        });
    }

    private void checkForConversion() {
        if(chatMesses.size() > 0) {
            checkForConversionRemotely(preferenceManager.getString(constant.KEY_USER_ID), receiverUser.id);
            checkForConversionRemotely(receiverUser.id,preferenceManager.getString(constant.KEY_USER_ID));
        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        db.collection(constant.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(constant.KEY_SENDER_ID,senderId)
                .whereEqualTo(constant.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    private final EventListener<QuerySnapshot> eventListener = (value, err) -> {
      if(err != null) {

      }
      if(value != null) {
          int count = chatMesses.size();
          for (DocumentChange documentChange : value.getDocumentChanges()) {
              if (documentChange.getType() == DocumentChange.Type.ADDED) {
                  ChatMess chatMess = new ChatMess();
                  chatMess.senderId = documentChange.getDocument().getString(constant.KEY_SENDER_ID);
                  chatMess.receivedId = documentChange.getDocument().getString(constant.KEY_RECEIVER_ID);
                  chatMess.message = documentChange.getDocument().getString(constant.KEY_MESSAGE);
                  chatMess.dateTime = getReadableDateTime(documentChange.getDocument().getDate(constant.KEY_TIMESTAMP));
                  chatMess.dateObject = documentChange.getDocument().getDate(constant.KEY_TIMESTAMP);
                  chatMesses.add(chatMess);
              }
          }
          Collections.sort(chatMesses, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
          if(count ==0) {
              chatAdapter.notifyDataSetChanged();
          } else {
              chatAdapter.notifyItemRangeInserted(chatMesses.size(), chatMesses.size());
              binding.chatRecylerView.smoothScrollToPosition(chatMesses.size() - 1);
          }
          binding.chatRecylerView.setVisibility(View.VISIBLE);
      }
      binding.progressbar.setVisibility(View.GONE);
      if(conversionId == null) {
          checkForConversion();
      }
    };

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy - hh:mm", Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String, Object> conversion) {
        db.collection(constant.KEY_COLLECTION_CONVERSATION)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String mess) {
        DocumentReference documentReference = db.collection(constant.KEY_COLLECTION_CONVERSATION).document(conversionId);
        documentReference.update(constant.KEY_LAST_MESSAGE, mess, constant.KEY_TIMESTAMP, new Date());
    }

    private void updateSendButtonState() {
        String message = binding.inutMess.getText().toString().trim();
        boolean isMessageEmpty = message.isEmpty();

        binding.layoutSend.setEnabled(!isMessageEmpty);
        binding.layoutSend.setAlpha(isMessageEmpty ? 0.5f   : 1.0f);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}