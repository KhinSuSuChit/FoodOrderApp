package com.example.foodorderapp.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderapp.R;
import com.example.foodorderapp.databinding.ActivityIntroBinding;

public class IntroActivity extends BaseActivity {
    ActivityIntroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setVariable();
        getWindow().setStatusBarColor(Color.parseColor("#FFE4B5"));

    }

    private void setVariable() {
        binding.loginBtn.setOnClickListener(view -> {
            if(mAuth.getCurrentUser() != null){
                startActivity(new Intent(IntroActivity.this, MainActivity.class));
            }else{
                startActivity(new Intent(IntroActivity.this, LoginActivity.class));
            }

        });
        binding.signupBtn.setOnClickListener(view -> startActivity(new Intent(IntroActivity.this, SignupActivity.class)));
    }
}