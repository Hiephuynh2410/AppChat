package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatapp.R;
import com.example.chatapp.adapter.UserAdapter;
import com.example.chatapp.databinding.ActivityUserBinding;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;
import com.example.chatapp.utilities.PreferenceManager;
import com.example.chatapp.utilities.constant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends BaseActivity implements UserListener {


    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUser();
    }

    private void setListeners() {
        binding.imagesBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUser() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(constant.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                   loading(false);
                   String currentUserId = preferenceManager.getString(constant.KEY_USER_ID);
                       if(task.isSuccessful() && task.getResult() != null) {
                           List<User> users = new ArrayList<>();
                           for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                               if(currentUserId.equals(queryDocumentSnapshot.getId())) {
                                   continue;
                               }
                               User user = new User();
                               user.name = queryDocumentSnapshot.getString(constant.KEY_NAME);
                               user.email = queryDocumentSnapshot.getString(constant.KEY_EMAIL);
                               user.image = queryDocumentSnapshot.getString(constant.KEY_IMAGE);
                               user.token = queryDocumentSnapshot.getString(constant.KEY_FCM_TOKEN);
                               user.id = queryDocumentSnapshot.getId();
                               users.add(user);
                           }
                           if(users.size() > 0) {
                               UserAdapter userAdapter = new UserAdapter(users, this) ;
                               binding.userRecyclerView.setAdapter(userAdapter);
                               binding.userRecyclerView.setVisibility(View.VISIBLE);
                           } else {
                               showErr();
                           }
                       } else {
                         showErr();
                       }
                   });
    }

    private void showErr() {
        binding.textErrorMess.setText(String.format("%s", "No User available"));
        binding.textErrorMess.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isloading) {
        if(isloading) {
            binding.progressbar.setVisibility(View.VISIBLE);
        } else {
            binding.progressbar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), chatActivity.class);
        intent.putExtra(constant.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}