package com.example.chatapp.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.utilities.PreferenceManager;
import com.example.chatapp.utilities.constant;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


//được sử dụng để quản lý trạng thái "đang có sẵn" của người dùng trong Firestore khi hoạt động của ứng dụng được tạm dừng hoặc tiếp tục hoạt động
public class BaseActivity extends AppCompatActivity {
    private DocumentReference documentReference;
    //lớp tạo một tham chiếu tới tài liệu người dùng cụ thể trong Firestore
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        documentReference = db.collection(constant.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(constant.KEY_USER_ID));
    }
    //Phương thức này được gọi khi hoạt động của ứng dụng tạm dừng (ví dụ: người dùng chuyển sang ứng dụng khác hoặc màn hình tắt điện thoại).
    // Trong phương thức này, lớp cập nhật trạng thái "đang có sẵn" của người dùng trong Firestore bằng cách gọi update() trên tham chiếu documentReference, và đặt giá trị trạng thái là 0 (hoặc có thể là false).
    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(constant.KEY_AVAILABILITY, 0);
    }
    //Phương thức này được gọi khi hoạt động của ứng dụng tiếp tục hoạt động sau khi đã tạm dừng (ví dụ: người dùng quay lại từ ứng dụng khác).
    // Trong phương thức này, lớp cập nhật lại trạng thái "đang có sẵn" của người dùng trong Firestore bằng cách gọi update() trên tham chiếu documentReference,
    // và đặt giá trị trạng thái là 1 (hoặc có thể là true).
    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(constant.KEY_AVAILABILITY, 1);
    }
}
