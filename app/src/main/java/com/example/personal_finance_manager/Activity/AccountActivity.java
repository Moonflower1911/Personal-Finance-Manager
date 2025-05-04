package com.example.personal_finance_manager.Activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.personal_finance_manager.R;
import com.example.personal_finance_manager.ViewModel.IncomeViewModel;
import com.example.personal_finance_manager.ViewModel.UserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;

public class AccountActivity extends BaseActivity {

    private UserViewModel userViewModel;

    private IncomeViewModel incomeViewModel;
    private String userId;
    private EditText etIncome;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        userId = getIntent().getStringExtra("userId");
        setupBottomNavBar(userId);

        LinearLayout navAccount = findViewById(R.id.navAccount);
        navAccount.post(() -> {
            ImageView budgetIcon = navAccount.findViewById(R.id.iconAccount);
            if (budgetIcon != null) budgetIcon.setAlpha(1.0f);
        });
        FloatingActionButton fab = findViewById(R.id.fabAddCategory);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, CategoryActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        etIncome = findViewById(R.id.etDefaultIncome);
        btnSave = findViewById(R.id.btnSaveIncome);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        userViewModel.getDefaultIncome(userId).observe(this, income -> {
            if (income != null) {
                etIncome.setText(String.valueOf(income));
            }
        });

        btnSave.setOnClickListener(v -> {
            String incomeText = etIncome.getText().toString().trim();
            if (!incomeText.isEmpty()) {
                double income = Double.parseDouble(incomeText);

                // Save to user table
                userViewModel.updateDefaultIncome(userId, income);

                // Save to income table for current month
                String currentMonth = null; // "yyyy-MM"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    currentMonth = LocalDate.now().toString().substring(0, 7);
                }
                incomeViewModel.setIncomeForMonth(userId, currentMonth, income);  // implement this

                Toast.makeText(this, "Default income updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter a valid income", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
