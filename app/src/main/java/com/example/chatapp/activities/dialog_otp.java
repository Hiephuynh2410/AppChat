package com.example.chatapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class dialog_otp extends AppCompatActivity {

    private EditText editTextOTP;
    private Button buttonVerify;
    private String phoneNumber;
    private String verificationId;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_otp);

        editTextOTP = findViewById(R.id.t2);
//        buttonVerify = findViewById(R.id.b2);

        firebaseAuth = FirebaseAuth.getInstance();

        // Retrieve the phone number and verification ID from the previous activity
        Intent intent = getIntent();
        if (intent != null) {
            phoneNumber = intent.getStringExtra("phoneNumber");
            verificationId = intent.getStringExtra("verificationId");
        }

        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = editTextOTP.getText().toString().trim();
                if (!TextUtils.isEmpty(otp)) {
                    verifyOTP(otp);
                } else {
                    showToast("Please enter the OTP.");
                }
            }
        });
        // Send OTP to the provided phone number
        sendVerificationCode();
    }

    private void sendVerificationCode() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60, // Timeout duration
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        // Auto-retrieval of OTP completed, directly verify
                        verifyOTP(phoneAuthCredential.getSmsCode());
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        showToast("Verification failed: " + e.getMessage());
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        verificationId = s;
                        showToast("OTP sent successfully!");
                    }
                }
        );
    }

    private void verifyOTP(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // OTP verified successfully, user is logged in
                            FirebaseUser user = task.getResult().getUser();
                            showToast("OTP verified successfully!");

                            // Proceed with your app's login logic or navigate to the main activity
                            // For example, if you want to navigate to MainActivity:
                            Intent intent = new Intent(dialog_otp.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Optional, to prevent the user from coming back to this OTP verification screen

                        } else {
                            showToast("OTP verification failed. Please try again.");
                        }
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}