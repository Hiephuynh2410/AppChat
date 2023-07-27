package com.example.chatapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OTP extends AppCompatActivity {

    private EditText editTextPhoneNumber;
    private Button buttonGetOtp;

    private FirebaseAuth firebaseAuth;
    private String verificationId;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        editTextPhoneNumber = findViewById(R.id.t1);
        buttonGetOtp = findViewById(R.id.b1);

        firebaseAuth = FirebaseAuth.getInstance();

        buttonGetOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = editTextPhoneNumber.getText().toString().trim();
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(OTP.this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show();
                } else {
                    sendVerificationCode(phoneNumber);
                }
            }
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60, // Timeout duration
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        // Not needed in this case. User input is required.
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(OTP.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        verificationId = s;

                        // Show dialog to enter OTP
                        showOTPDialog();
                    }
                }
        );
    }

    private void showOTPDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter OTP");
        View view = getLayoutInflater().inflate(R.layout.activity_dialog_otp, null);
        EditText editTextOTP = view.findViewById(R.id.t2);
        builder.setView(view);
        builder.setPositiveButton("Verify OTP", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String otp = editTextOTP.getText().toString().trim();
                if (!otp.isEmpty()) {
                    // Verify the OTP
                    verifyOTP(otp);
                } else {
                    Toast.makeText(OTP.this, "Please enter the OTP.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.setCancelable(false);
        AlertDialog otpDialog = builder.create();
        otpDialog.show();
    }
    private void verifyOTP(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(OTP.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(OTP.this, "Verification failed. Please enter a valid OTP.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}