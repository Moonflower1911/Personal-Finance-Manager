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
    private EditText etIncome,etUsername, etEmail;
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
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail); // still readonly for now

        btnSave = findViewById(R.id.btnSave);
        etEmail.setFocusable(false);
        etEmail.setClickable(false);
        etEmail.setLongClickable(false);
        etEmail.setCursorVisible(false);
        etEmail.setBackground(null); // remove underline if needed

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        userViewModel.getDefaultIncome(userId).observe(this, income -> {
            if (income != null) {
                etIncome.setText(String.valueOf(income));
            }
        });

        userViewModel.getUserById(userId).observe(this, user -> {
            if (user != null) {
                etUsername.setText(user.fullName);
                etEmail.setText(user.email); // just display, not editable
                etIncome.setText(String.valueOf(user.defaultIncome));
            }
        });


        btnSave.setOnClickListener(v -> {
            String name = etUsername.getText().toString().trim();
            String incomeStr = etIncome.getText().toString().trim();

            if (name.isEmpty() || incomeStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double income = Double.parseDouble(incomeStr);
            userViewModel.updateUserInfo(userId, name, income); // email unchanged

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String currentMonth = LocalDate.now().toString().substring(0, 7);
                incomeViewModel.setIncomeForMonth(userId, currentMonth, income);
            }

            Toast.makeText(this, "Account info updated", Toast.LENGTH_SHORT).show();
        });

    }
}
