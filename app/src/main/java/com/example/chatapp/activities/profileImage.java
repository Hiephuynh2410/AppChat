package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
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

        preferenceManager = new PreferenceManager(getApplicationContext());

        // Initialize views
        profileImage = findViewById(R.id.profileImage);
        textUserName = findViewById(R.id.textUserName);
        textUserEmail = findViewById(R.id.textUserEmail);
        textPhoneNumber = findViewById(R.id.textPhoneNumber);

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
                            String imageBase64 = documentSnapshot.getString(constant.KEY_IMAGE);
                            String phoneNumber = documentSnapshot.getString(constant.KEY_PHONE); // Retrieve phone number

                            // Display user information
                            textUserName.setText(name);
                            textUserEmail.setText(email);
                            textPhoneNumber.setText(phoneNumber);

                            // Load profile image using Base64 decoding
                            byte[] decodedImageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                            Bitmap decodedImageBitmap = BitmapFactory.decodeByteArray(decodedImageBytes, 0, decodedImageBytes.length);
                            profileImage.setImageBitmap(decodedImageBitmap);
                        }
                    } else {

                    }
                });
    }
}