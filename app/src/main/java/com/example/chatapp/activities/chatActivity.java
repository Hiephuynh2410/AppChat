package com.example.chatapp.activities;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.adapter.ChatAdapter;
import com.example.chatapp.databinding.ActivityChatBinding;
import com.example.chatapp.models.ChatMess;
import com.example.chatapp.models.User;
import com.example.chatapp.network.ApiClient;
import com.example.chatapp.network.ApiService;
import com.example.chatapp.utilities.PreferenceManager;
import com.example.chatapp.utilities.constant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        if(!isReceiverAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(constant.KEY_USER_ID, preferenceManager.getString(constant.KEY_USER_ID));
                data.put(constant.KEY_NAME, preferenceManager.getString(constant.KEY_NAME));
                data.put(constant.KEY_FCM_TOKEN, preferenceManager.getString(constant.KEY_FCM_TOKEN));
                data.put(constant.KEY_MESSAGE, binding.inutMess.getText().toString());

                JSONObject body = new JSONObject();
                body.put(constant.REMOTE_MSG_DATA , data);
                body.put(constant.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotifi(body.toString());
            } catch (Exception e) {
                showToast(e.getMessage());
            }
        }
        binding.inutMess.setText(null);
    }

    private void showToast(String mess) {
        Toast.makeText(getApplicationContext(), mess, Toast.LENGTH_SHORT).show();
    }

    private void sendNotifi(String messBody) {
        //Đoạn mã này tạo một đối tượng ApiClient thông qua hàm getClient() và tạo một đối tượng ApiService thông qua
        ApiClient.getClient().create(ApiService.class).sendMess(
                constant.getRemoteMsgHeader(),
                messBody
        ).enqueue(new Callback<String>() {
            //enqueue Đây là một phần của thư viện Retrofit, nó được sử dụng để gửi yêu cầu bất đồng bộ và xử lý kết quả trả về. Khi yêu cầu thành công
            // , phương thức onResponse được gọi, và khi yêu cầu thất bại, phương thức onFailure được gọi.
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    try {
                        //kiểm tra xem phản hồi có dữ liệu không. Nếu không có dữ liệu, ta sẽ không tiếp tục xử lý và thoát khỏi phương thức.
                        if(response.body() != null) {
                            //tạo một đối tượng JSONObject từ dữ liệu phản hồi (response.body()). Điều này cho phép truy xuất và xử lý dữ liệu trong dạng JSON.
                            JSONObject responseJson = new JSONObject(response.body());
                            //Chúng ta tiếp tục bẻ khóa JSON để truy cập vào mảng JSON có tên "result" trong dữ liệu phản hồi. Mảng này có thể chứa kết quả thông báo gửi từ máy chủ FCM.
                            JSONArray results = responseJson.getJSONArray("result");
                            if(responseJson.getInt("failure") == 1) { //truy xuất vào giá trị số nguyên có tên "failure" trong JSON. Trong cơ chế của FCM, nếu việc gửi thông báo có lỗi thì giá trị này sẽ là 1.
                                JSONObject err = (JSONObject) results.get(0);//Nếu giá trị "failure" bằng 1, chúng ta lấy phần tử đầu tiên trong mảng "results", giả sử là một đối tượng JSON chứa thông tin lỗi.
                                showToast(err.getString("Error"));
                                return;
                            }
                        }
                    } catch (JSONException e) {
                            e.printStackTrace();
                    }
//                    showToast("Notifycation sent Success");
                } else {
                    showToast("error" + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
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
                receiverUser.token = value.getString(constant.KEY_FCM_TOKEN);
                if(receiverUser.image == null) {
                    receiverUser.image = value.getString(constant.KEY_IMAGE);
                    chatAdapter.setReceiverProfileImg(getBitmapFromEncodeString(receiverUser.image));
                    chatAdapter.notifyItemRangeChanged(0, chatMesses.size());
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
        if(encodeimg != null ) {
            byte[] bytes = Base64.decode(encodeimg, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
        } else  {
            return null;
        }
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
                Intent intent = new Intent(chatActivity.this, MainActivity.class);
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