package com.example.foodorderapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderapp.R;
import com.example.foodorderapp.databinding.ActivitySignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class SignupActivity extends BaseActivity {

    ActivitySignupBinding binding;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setVariable();
        

    }

    private void setVariable() {
        binding.signupBtn.setOnClickListener(view -> {
            String email = binding.userEdt.getText().toString();
            String password = binding.passwordEdt.getText().toString();

            if(password.length()<6){
                Toast.makeText(SignupActivity.this, "your password must be 6 character", Toast.LENGTH_SHORT).show();
            }
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this, task -> {
                if(task.isSuccessful()){
                    Log.i(TAG, "onComplete: ");
                    startActivity(new Intent(SignupActivity.this,MainActivity.class));
                }
                else{
                    Log.i(TAG, "failure: " + task.getException());
                    Toast.makeText(SignupActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}