package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.utilities.PreferenceManager;
import com.example.chatapp.utilities.constant;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class profileImage extends AppCompatActivity {

    private ImageView profileImage;
    private TextView textUserName, textUserEmail,textPhoneNumber;
    private AppCompatImageView back;
    private PreferenceManager preferenceManager;
    private EditText editTextName, editTextEmail, editTextPhoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_image);

        back = findViewById(R.id.imagesBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button btnSaveProfile = findViewById(R.id.btnSave);
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String updatedName = editTextName.getText().toString().trim();
                String updatedEmail = editTextEmail.getText().toString().trim();
                String updatedPhoneNumber = editTextPhoneNumber.getText().toString().trim();

                // Update the user information in the Firestore document
                String currentUserId = preferenceManager.getString(constant.KEY_USER_ID);
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                database.collection(constant.KEY_COLLECTION_USERS)
                        .document(currentUserId)
                        .update(
                                constant.KEY_NAME, updatedName,
                                constant.KEY_EMAIL, updatedEmail,
                                constant.KEY_PHONE, updatedPhoneNumber
                        )
                        .addOnSuccessListener(aVoid -> {
                            // If the update is successful, update the displayed information as well
                            textUserName.setText(updatedName);
                            textUserEmail.setText(updatedEmail);
                            textPhoneNumber.setText(updatedPhoneNumber);

                            showToast("Profile updated successfully!");
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> showToast("Failed to update profile."));
            }
        });


        preferenceManager = new PreferenceManager(getApplicationContext());

        // Initialize views
        profileImage = findViewById(R.id.profileImage);
        textUserName = findViewById(R.id.textUserName);
        textUserEmail = findViewById(R.id.textUserEmail);
        textPhoneNumber = findViewById(R.id.textPhoneNumber);

         editTextName = findViewById(R.id.editName);
         editTextEmail = findViewById(R.id.editEmailAddress);
         editTextPhoneNumber = findViewById(R.id.editPhone);
        // Get the current user ID
        String currentUserId = preferenceManager.getString(constant.KEY_USER_ID);

        // Query Firestore to get the user document
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(constant.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            // Retrieve user information from the document
                            String name = documentSnapshot.getString(constant.KEY_NAME);
                            String email = documentSnapshot.getString(constant.KEY_EMAIL);
                            String phoneNumber = documentSnapshot.getString(constant.KEY_PHONE); // Retrieve phone number

                            // Display user information in EditText fields
                            textUserName.setText(name);
                            textUserEmail.setText(email);
                            textPhoneNumber.setText(phoneNumber);

                            // Load profile image using Base64 decoding
                            String imageBase64 = documentSnapshot.getString(constant.KEY_IMAGE);
                            byte[] decodedImageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                            Bitmap decodedImageBitmap = BitmapFactory.decodeByteArray(decodedImageBytes, 0, decodedImageBytes.length);
                            profileImage.setImageBitmap(decodedImageBitmap);

                            // Display user information in EditText fields
                            editTextName.setText(name);
                            editTextEmail.setText(email);
                            editTextPhoneNumber.setText(phoneNumber);
                        }
                    } else {
                        showToast("User data not found.");
                    }
                });
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void saveProfileChanges() {
        // Get updated information from the EditText fields
    }

}