package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import com.example.chatapp.databinding.ActivityMainBinding;
import com.example.chatapp.utilities.PreferenceManager;
import com.example.chatapp.utilities.constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUser();
        getToken();
        setListener();
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



    private void upadateToken(String token) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection(constant.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(constant.KEY_USER_ID)
        );
        documentReference.update(constant.KEY_FCM_TOKEN, token).addOnFailureListener(e -> showToast("unable update token"));
    }

    private void SignOut() {
        showToast("signing out...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();//được sử dụng để khởi tạo một đối tượng FirebaseFirestore để có thể sử dụng các phương thức và chức năng của Firestore.
        DocumentReference documentReference = db.collection(constant.KEY_COLLECTION_USERS).document(
            preferenceManager.getString(constant.KEY_USER_ID)//truy cập vào bộ sưu tập người dùng trong Firestore. Sau đó, trả về ID của người dùng đã đăng nhập.
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(constant.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
            preferenceManager.clear();
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        })
            .addOnFailureListener(e -> showToast("unable to sign out"));
    }

}