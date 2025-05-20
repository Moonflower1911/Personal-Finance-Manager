package com.example.personal_finance_manager.Activity;

import android.content.Intent;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.personal_finance_manager.R;

public class BaseActivity extends AppCompatActivity {

    protected void setupBottomNavBar(String userId) {
        LinearLayout navBudget = findViewById(R.id.navBudget);
        LinearLayout navAccount = findViewById(R.id.navAccount);
        LinearLayout navRecords = findViewById(R.id.navRecords);
        LinearLayout navAnalysis = findViewById(R.id.navAnalysis);


        navBudget.setOnClickListener(v -> {
            if (!(this instanceof BudgetActivity)) { // Avoid reopening if already in BudgetActivity
                Intent intent = new Intent(this, BudgetActivity.class);
                intent.putExtra("userId", userId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });



        navAccount.setOnClickListener(v -> {
            if (!(this instanceof AccountActivity)) { // Avoid reopening if already in BudgetActivity
                Intent intent = new Intent(this, AccountActivity.class);
                intent.putExtra("userId", userId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        navRecords.setOnClickListener(v -> {
            if (!(this instanceof RecordsActivity)) { // Avoid reopening if already in BudgetActivity
                Intent intent = new Intent(this, RecordsActivity.class);
                intent.putExtra("userId", userId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        navAnalysis.setOnClickListener(v -> {
            if (!(this instanceof AnalysisActivity)) {
                Intent i = new Intent(this, AnalysisActivity.class);
                i.putExtra("userId", userId);
                startActivity(i);
                finish();
            }
        });
//        if (navAnalysis != null) {
//            navAnalysis.setOnClickListener(v -> {
//                Intent intent = new Intent(this, AnalysisActivity.class);
//                intent.putExtra("userId", userId);
//                startActivity(intent);
//            });
//        }
    }
}
