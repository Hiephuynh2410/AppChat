package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.UnsupportedEncodingException;

public class forgot_password extends AppCompatActivity {
    private Button btnBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password);

        ShowPass();

        btnBack = findViewById(R.id.buttonback);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
    // Hàm giải mã mật khẩu từ chuỗi Base64
    private String decodePassword(String encodedPassword) {
        try {
            byte[] decodedData = Base64.decode(encodedPassword, Base64.DEFAULT);
            return new String(decodedData, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void ShowPass() {
        Button resetButton = findViewById(R.id.buttonResetPassword);
        EditText emailEditText = findViewById(R.id.editTextEmail);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = emailEditText.getText().toString();

                // Query Firestore for the user's password based on the userEmail
                firestore.collection("users")
                        .whereEqualTo("email", userEmail)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (!task.getResult().isEmpty()) {
                                    // Assuming the password is stored in a field called "password"
                                    DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                    String encryptedPassword = document.getString("password");

                                    // Giải mã mật khẩu đã mã hoá
                                    String decryptedPassword = decodePassword(encryptedPassword);

                                    // Show a dialog with the decrypted password
                                    AlertDialog alertDialog = new AlertDialog.Builder(forgot_password.this).create();
                                    alertDialog.setTitle("Password Recovery");
                                    alertDialog.setMessage("Your password: " + decryptedPassword);
                                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialog, which) -> alertDialog.dismiss());
                                    alertDialog.show();
                                } else {
                                    // Handle the case where the email is not found in Firestore
                                    // For example, show a toast message or an error dialog
                                    Toast.makeText(forgot_password.this, "Email not found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Handle the case where the Firestore query failed
                                // You can log an error or show an error message
                                Toast.makeText(forgot_password.this, "Failed to retrieve password", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}