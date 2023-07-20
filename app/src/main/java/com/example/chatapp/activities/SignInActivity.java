package com.example.chatapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

    ImageView twGoogle;
    Button btngoole;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "GOOGLEAUTH";
    Dialog dialog;
    GoogleSignInClient googleSignInClient ;


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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        googleSignInClient = GoogleSignIn.getClient(this,gso);

        twGoogle = findViewById(R.id.google_btn);
        twGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignInGoogle();
            }
        });

    }
    private  void SignInGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            dialog.show();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                dialog.dismiss();
                // ...
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Intent i = new Intent(SignInActivity.this,MainActivity.class);
                            startActivity(i);
                            finish();
                            dialog.dismiss();
                            //  updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //  Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //  Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            // updateUI(null);
                            dialog.dismiss();
                            Toast.makeText(SignInActivity.this,"Login failed",Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
    //////////////////////////////////
    private void setListeners() {
        binding.TextCreateNewaccount.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v -> {
            if(isValidSignIn()) {
                SignIn();
            }
        });

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
//        if(binding.inputemail.getText().toString().trim().isEmpty()) {
//            showToast("enter email");
//            return false;
//        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputemail.getText().toString()).matches()) {
//            showToast("enter valid email");
//            return false;
//        } else if(binding.inputPassword.getText().toString().trim().isEmpty()) {
//            showToast("enter password");
//            return false;
//        } else {
//            return true;
//        }
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
