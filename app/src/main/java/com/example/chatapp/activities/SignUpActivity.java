package com.example.chatapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivitySignUpBinding;
import com.example.chatapp.utilities.PreferenceManager;
import com.example.chatapp.utilities.constant;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodeImage;
    private EditText inputPhoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        inputPhoneNumber = binding.inputPhoneNumber; // Initialize the inputPhoneNumber EditText
        setListeners();

    }


/////////////////
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
        user.put(constant.KEY_PHONE, inputPhoneNumber.getText().toString());
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
    private boolean isValidPassword(String password) {
        // The regex pattern for a valid password (minimum 8 characters, at least one uppercase, one lowercase, and one digit)
        String regexPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    private Boolean isvalidSignUp() {
        String name = binding.inputName.getText().toString().trim();
        String email = binding.inputemail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();
        String confirmPassword = binding.inputConfirmPassword.getText().toString().trim();
        String phoneNumber = inputPhoneNumber.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)) {
            inputPhoneNumber.setError("Enter phone number");
            return false;
        }
        if (name.contains(" ")) {
            binding.inputName.setError("Name cannot contain spaces");
            return false;
        }

        if (encodeImage == null) {
            showToast("Select profile image");
            return false;
        }
        if (!isValidPassword(password)) {
            binding.inputPassword.setError("Password must be at least 8 characters long, contain one uppercase letter, one lowercase letter, and one digit.");
            return false;
        }
        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(email) && TextUtils.isEmpty(password) && TextUtils.isEmpty(confirmPassword)) {
            binding.inputName.setError("Enter name");
            binding.inputemail.setError("Enter email");
            binding.inputPassword.setError("Enter password");
            binding.inputConfirmPassword.setError("Confirm your password");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputemail.setError("Enter valid email");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.inputConfirmPassword.setError("Password & confirm password must be the same");
            return false;
        }

        // Clear error indicators if there were any
        binding.inputName.setError(null);
        binding.inputemail.setError(null);
        binding.inputPassword.setError(null);
        binding.inputConfirmPassword.setError(null);

        // kiểm tra gmail đã tồn tại
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(constant.KEY_COLLECTION_USERS)
                .whereEqualTo(constant.KEY_EMAIL, email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        binding.inputemail.setError("Email is already registered");
                    } else {
                        signUp();
                    }
                })
                .addOnFailureListener(e -> showToast("Error checking email existence"));

        return false;
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

