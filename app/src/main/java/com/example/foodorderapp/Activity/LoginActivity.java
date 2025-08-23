package com.example.foodorderapp.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderapp.R;
import com.example.foodorderapp.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;


public class LoginActivity extends BaseActivity {

    ActivityLoginBinding binding;
    FirebaseDatabase firebaseDatabase;
    ProgressDialog progressDialog;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setVariable();
        googleSignIn();
        initFacebookLogin();
    }

    private void initFacebookLogin() {
        binding.btnFacebook.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, FacebookAuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });
    }


    private void googleSignIn() {
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Please Wait\nValidation in Progress");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }

        binding.btnGoogle.setOnClickListener(v -> signIn());
    }

    int RC_SIGN_IN = 65;
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            }
            catch (ApiException e){
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("TAG", "signInWithCredential:success");

                        //FirebaseUser user = mAuth.getCurrentUser();
                        //Users users = new Users();
                        //users.setUserId(user.getUid());
                        //users.setUserName(user.getDisplayName());
                        //users.setProfilePic(user.getPhotoUrl().toString());
                        //firebaseDatabase.getReference().child("Users").child(user.getUid()).setValue(users);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(LoginActivity.this, "Sign in with Google", Toast.LENGTH_SHORT).show();
                    } else {
                        showErrorDialog("signInWithCredential:failure");
                    }
                });
    }




    private void setVariable() {
        binding.loginBtn.setOnClickListener(view -> {
            String email = binding.userEdt.getText().toString();
            String password = binding.passwordEdt.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(LoginActivity.this, "Please fill username and password", Toast.LENGTH_SHORT).show();
            }
        });
        binding.signupText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
        binding.forgotTxt.setOnClickListener(v -> {
            String prefill = binding.userEdt.getText().toString().trim();
            showResetDialog(prefill);
        });
    }

    private void showResetDialog(String prefill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_forgot_password_dialog, null);
        EditText textEdit = view.findViewById(R.id.textEdit);
        if (!TextUtils.isEmpty(prefill)) textEdit.setText(prefill);

        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTitle)).setText(getResources().getString(R.string.forgot_pass_title));
        ((TextView) view.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.forgot_pass_message));
        ((Button) view.findViewById(R.id.buttonYes)).setText(getResources().getString(R.string.send));
        ((Button) view.findViewById(R.id.buttonNo)).setText(getResources().getString(R.string.cancel));
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_password_reset);

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonYes).setOnClickListener(v -> {
            String email = textEdit.getText().toString().trim();
            sendResetEmail(email);
            alertDialog.dismiss();
            //Toast.makeText(LoginActivity.this, "Yes", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.buttonNo).setOnClickListener(v -> alertDialog.dismiss());

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            alertDialog.getWindow().setDimAmount(0.85f);
        }

        if (!isFinishing() && !isDestroyed()) {
            runOnUiThread(alertDialog::show);
        }
    }

    private void sendResetEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            showErrorDialog("Email is required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showErrorDialog("Enter a valid email");
            return;
        }

        setUiEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setUiEnabled(true);
                    if (task.isSuccessful()) {
                        showSuccessDialog();
                    } else {
                        String msg = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Failed to send reset email";
                        showErrorDialog(msg);
                    }
                });
    }

    private void setUiEnabled(boolean enabled) {
        binding.loginBtn.setEnabled(enabled);
        binding.signupText.setEnabled(enabled);
        binding.forgotTxt.setEnabled(enabled);
        binding.userEdt.setEnabled(enabled);
        binding.passwordEdt.setEnabled(enabled);
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.
                Builder(LoginActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_success_dialog, null);

        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTitle)).setText(getResources().getString(R.string.success_title));
        ((TextView) view.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.success_message));
        ((Button) view.findViewById(R.id.buttonAction)).setText(getResources().getString(R.string.okay));
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_success);

        final AlertDialog alertDialog = builder.create();
        view.findViewById(R.id.buttonAction).setOnClickListener(v -> alertDialog.dismiss());
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            alertDialog.getWindow().setDimAmount(0.85f);
        }

        if (!isFinishing() && !isDestroyed()) {
            runOnUiThread(alertDialog::show);
        }
    }

    private void showErrorDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.
                Builder(LoginActivity.this, R.style.AlertDialogTheme);

        View view = LayoutInflater.from(this).inflate(R.layout.layout_error_dialog, null);
        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTitle)).setText(getResources().getString(R.string.error_title));
        ((TextView) view.findViewById(R.id.textMessage)).setText(msg);
        ((Button) view.findViewById(R.id.buttonAction)).setText(getResources().getString(R.string.try_again));
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_error);

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonAction).setOnClickListener(v -> alertDialog.dismiss());

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            alertDialog.getWindow().setDimAmount(0.85f);
        }
        if (!isFinishing() && !isDestroyed()) {
            runOnUiThread(alertDialog::show);
        }
    }

}