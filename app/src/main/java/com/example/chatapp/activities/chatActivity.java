package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.chatapp.databinding.ActivityChatBinding;
import com.example.chatapp.models.User;
import com.example.chatapp.utilities.constant;

public class chatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;

    private User receiverUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void loadReceiver() {
        //một phương thức trong một ứng dụng Android. Nó được sử dụng để tải thông tin người nhận
        receiverUser = (User) getIntent().getSerializableExtra(constant.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }
}