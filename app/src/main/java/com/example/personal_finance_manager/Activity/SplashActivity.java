package com.example.personal_finance_manager.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.personal_finance_manager.R;
import com.example.personal_finance_manager.ViewModel.SplashViewModel;

public class SplashActivity extends AppCompatActivity {

    private ImageView logoImage;
    private LinearLayout bottomPanel;
    private Button loginButton, signupButton;

    private SplashViewModel splashViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logoImage = findViewById(R.id.logoImage);
        bottomPanel = findViewById(R.id.bottomPanel);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        splashViewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        // Start pulse animation
        Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.scale_pulse);
        logoImage.startAnimation(pulseAnim);

        // Observe panel visibility
        splashViewModel.getShowBottomPanel().observe(this, show -> {
            if (show) {
                bottomPanel.setVisibility(View.VISIBLE);
                Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
                bottomPanel.startAnimation(slideUp);
            }
        });

        // Navigation listeners
        loginButton.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        signupButton.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
    }
}
