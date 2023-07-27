package com.example.chatapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivitySignInBinding;
import com.example.chatapp.utilities.PreferenceManager;
import com.example.chatapp.utilities.constant;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    Dialog dialog;
    TextView btnotp;
    private AlertDialog phoneNumberDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new Dialog(SignInActivity.this);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(constant.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        btnotp = findViewById(R.id.textOTP);
        btnotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), OTP.class);
                startActivity(intent);
            }
        });
    }

    //////////////////////////////////
    private void setListeners() {
        binding.TextCreateNewaccount.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSignIn()) {
                //SignIn();
                checkCredentials();
            }
        });
    }

    private void checkCredentials() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(constant.KEY_COLLECTION_USERS)
                .whereEqualTo(constant.KEY_EMAIL, binding.inputemail.getText().toString())
                .whereEqualTo(constant.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(constant.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(constant.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(constant.KEY_NAME, documentSnapshot.getString(constant.KEY_NAME));
                        preferenceManager.putString(constant.KEY_IMAGE, documentSnapshot.getString(constant.KEY_IMAGE));

                        // Show dialog to enter phone number
                        showPhoneNumberDialog();
                    } else {
                        loading(false);
                        showToast("Invalid credentials. Please try again.");
                    }
                });
    }

    private void showPhoneNumberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Phone Number");
        View view = getLayoutInflater().inflate(R.layout.activity_otp, null);
        EditText editTextPhoneNumber = view.findViewById(R.id.t1);
        builder.setView(view);
        builder.setPositiveButton("Get OTP", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String phoneNumber = editTextPhoneNumber.getText().toString().trim();
                if (!TextUtils.isEmpty(phoneNumber)) {
                    // Start OTP activity and pass the phone number
                    Intent intent = new Intent(SignInActivity.this, OTP.class);
                    intent.putExtra("phoneNumber", phoneNumber);
                    startActivity(intent);
                } else {
                    showToast("Please enter a valid phone number.");
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog phoneNumberDialog = builder.create();
        phoneNumberDialog.show();
    }

    private void loading(Boolean loading) {
        if (loading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressbar.setVisibility(View.VISIBLE);
        } else {
            binding.progressbar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }
    private void SignIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(constant.KEY_COLLECTION_USERS)
            .whereEqualTo(constant.KEY_EMAIL, binding.inputemail.getText().toString())
            .whereEqualTo(constant.KEY_PASSWORD, binding.inputPassword.getText().toString())
            .get().addOnCompleteListener(task -> {
                if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                    preferenceManager.putBoolean(constant.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(constant.KEY_USER_ID, documentSnapshot.getId());
                    preferenceManager.putString(constant.KEY_NAME, documentSnapshot.getString(constant.KEY_NAME));
                    preferenceManager.putString(constant.KEY_IMAGE, documentSnapshot.getString(constant.KEY_IMAGE));
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }  else {
                    loading(false);
                    showToast("Unable to Sign In");
                }
            });
    }

    private void showToast(String mess) {
        Toast.makeText(getApplicationContext(), mess, Toast.LENGTH_SHORT).show();
    }

    private  Boolean isValidSignIn() {
        String email, Password;
        email = String.valueOf(binding.inputemail.getText());
        Password = String.valueOf(binding.inputPassword.getText());
        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(Password)) {
                binding.inputemail.setError("Enter Valid Email");
                binding.inputPassword.setError("Enter Valid Password");
                return true;
        }
        return true;
    }
}
