package com.example.personal_finance_manager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private ImageView logoImage;
    private LinearLayout bottomPanel;
    private Button loginButton, signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logoImage = findViewById(R.id.logoImage);
        bottomPanel = findViewById(R.id.bottomPanel);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        // Pulse animation on logo
        Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.scale_pulse);
        logoImage.startAnimation(pulseAnim);

        // Delay to show the panel
        new Handler().postDelayed(() -> {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            bottomPanel.setVisibility(View.VISIBLE);
            bottomPanel.startAnimation(slideUp);
        }, 1000);

        // Button listeners
        loginButton.setOnClickListener(v -> {
            // Navigate to LoginActivity
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        });

        signupButton.setOnClickListener(v -> {
            // Navigate to SignUpActivity
            startActivity(new Intent(SplashActivity.this, SignUpActivity.class));
        });
    }
}
