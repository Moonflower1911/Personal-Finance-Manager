package com.example.personal_finance_manager.Activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personal_finance_manager.R;

public class RecordsActivity extends BaseActivity{
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        userId = getIntent().getStringExtra("userId"); // ✅ safely assign here
        setupBottomNavBar(userId); // ✅ ← THIS IS MISSING RIGHT NOW
        LinearLayout navRecords = findViewById(R.id.navRecords);
        navRecords.post(() -> {
            ImageView recordsIcon = navRecords.findViewById(R.id.iconRecords);
            if (recordsIcon != null) recordsIcon.setAlpha(1.0f);
        });
    }
}