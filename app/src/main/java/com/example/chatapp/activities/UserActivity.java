package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;

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
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                // Khi người dùng thay đổi nội dung trong thanh tìm kiếm, gọi getUser() để cập nhật danh sách người dùng
                getUser();
                return true;
            }
        });
    }

    private void setListeners() {
        binding.imagesBack.setOnClickListener(v -> onBackPressed());
    }

    //    private void getUser() {
    //        loading(true);
    //        FirebaseFirestore database = FirebaseFirestore.getInstance();
    //        database.collection(constant.KEY_COLLECTION_USERS)
    //                .get()
    //                .addOnCompleteListener(task -> {
    //                    loading(false);
    //
    //                    // Xử lý danh sách người dùng để lọc theo tìm kiếm
    //                    List<User> filteredUsers = new ArrayList<>();
    //                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
    //
    //                        // Lấy thông tin người dùng từ Firestore
    //                        User user = new User();
    //                        user.name = queryDocumentSnapshot.getString(constant.KEY_NAME);
    //                        user.email = queryDocumentSnapshot.getString(constant.KEY_EMAIL);
    //                        user.image = queryDocumentSnapshot.getString(constant.KEY_IMAGE);
    //                        user.token = queryDocumentSnapshot.getString(constant.KEY_FCM_TOKEN);
    //                        user.id = queryDocumentSnapshot.getId();
    //
    //                        // Kiểm tra nếu tên người dùng chứa chuỗi tìm kiếm
    //                        String searchQuery = binding.searchView.getQuery().toString().toLowerCase();
    //                        if (user.name.toLowerCase().contains(searchQuery)) {
    //                            filteredUsers.add(user);
    //                        }
    //                    }
    //
    //                    // Hiển thị danh sách người dùng đã lọc trong RecyclerView
    //                    if (filteredUsers.size() > 0) {
    //                        UserAdapter userAdapter = new UserAdapter(filteredUsers, this);
    //                        binding.userRecyclerView.setAdapter(userAdapter);
    //                        binding.userRecyclerView.setVisibility(View.VISIBLE);
    //                    } else {
    //                        showErr();
    //                    }
    //                });
    //    }

    private void getUser() {
    loading(true);
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    database.collection(constant.KEY_COLLECTION_USERS)
        .get()
        .addOnCompleteListener(task -> {
            loading(false);

            // Get the current user ID
            String currentUserId = preferenceManager.getString(constant.KEY_USER_ID);

            // Process the user list to filter based on search and exclude the current user
            List<User> filteredUsers = new ArrayList<>();
            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                // Retrieve user information from Firestore
                User user = new User();
                user.name = queryDocumentSnapshot.getString(constant.KEY_NAME);
                user.email = queryDocumentSnapshot.getString(constant.KEY_EMAIL);
                user.image = queryDocumentSnapshot.getString(constant.KEY_IMAGE);
                user.token = queryDocumentSnapshot.getString(constant.KEY_FCM_TOKEN);
                user.id = queryDocumentSnapshot.getId();

                // search, Check if the user's name contains the search query
                String searchQuery = binding.searchView.getQuery().toString().toLowerCase();
                if (user.name.toLowerCase().contains(searchQuery) && !user.id.equals(currentUserId)) {
                    filteredUsers.add(user);
                }
            }

            // Display the filtered user list in the RecyclerView
            if (filteredUsers.size() > 0) {
                UserAdapter userAdapter = new UserAdapter(filteredUsers, this);
                binding.userRecyclerView.setAdapter(userAdapter);
                binding.userRecyclerView.setVisibility(View.VISIBLE);
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