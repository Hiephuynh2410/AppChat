package com.example.chatapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivitySignUpBinding;
import com.example.chatapp.utilities.PreferenceManager;
import com.example.chatapp.utilities.constant;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodeImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonSignUp.setOnClickListener( v -> {
            if(isvalidSignUp()) {
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(constant.KEY_NAME, binding.inputName.getText().toString());
        user.put(constant.KEY_EMAIL, binding.inputemail.getText().toString());
        user.put(constant.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(constant.KEY_IMAGE, encodeImage);
        database.collection(constant.KEY_COLLECTION_USERS).add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(constant.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(constant.KEY_USER_ID, documentReference.getId() );
                    preferenceManager.putString(constant.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(constant.KEY_IMAGE, encodeImage);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }


    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;//Đây là chiều rộng (width) mong muốn cho hình ảnh được mã hóa. Trong trường hợp này, chiều rộng được đặt là 150 pixels.
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();//Đây là chiều cao (height) mong muốn cho hình ảnh được mã hóa, tính toán dựa trên tỷ lệ giữa chiều rộng và chiều cao ban đầu của đối tượng Bitmap.
        Bitmap preewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);//: Đây là bước thay đổi kích thước (scale) của đối tượng Bitmap ban đầu thành kích thước mong muốn. Sử dụng phương thức createScaledBitmap() để tạo một đối tượng Bitmap mới với kích thước đã chỉ định.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();//Đây là một ByteArrayOutputStream được sử dụng để ghi dữ liệu từ đối tượng Bitmap vào một mảng byte.
        preewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);//Đây là bước nén (compress) đối tượng Bitmap thành định dạng hình ảnh JPEG với mức độ nén là 50. Kết quả nén sẽ được ghi vào ByteArrayOutputStream.
        byte[] bytes = byteArrayOutputStream.toByteArray();//Đây là bước chuyển đổi dữ liệu từ ByteArrayOutputStream thành một mảng byte.
        return Base64.encodeToString(bytes, Base64.DEFAULT);//: Đây là bước mã hóa mảng byte thành một chuỗi Base64. Phương thức encodeToString() của lớp Base64 được sử dụng để thực hiện mã hóa, và kết quả chuỗi Base64 được trả về.
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if(result.getResultCode() == RESULT_OK) {
                if(result.getData() != null) {
                    Uri imagesUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imagesUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.imageProfile.setImageBitmap(bitmap);
                        binding.textAddImage.setVisibility(View.GONE);
                        encodeImage = encodeImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    );

    private Boolean isvalidSignUp() {
        if(encodeImage == null ) {
            showToast("select profile Images");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("enter name");
            return false;
        } else if (binding.inputemail.getText().toString().trim().isEmpty()) {
            showToast("enter email");
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputemail.getText().toString()).matches()) {
            showToast("enter valid image");
            return false;
        } else if(binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("enter password");
            return false;
        } else if(binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("confirm your password");
            return false;
        } else if(!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("password & confirm password must be same");
            return false;
        } else {
            return  true;
        }
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressbar.setVisibility(View.VISIBLE);
        } else {
            binding.progressbar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}