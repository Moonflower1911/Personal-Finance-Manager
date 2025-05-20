package com.example.personal_finance_manager.Activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
    private EditText etIncome, etUsername, etEmail;
    private Button btnEdit, btnLogout;

    private String originalUsername = "";
    private String originalIncome = "";
    boolean isEditing = false;

    private void setFieldsEnabled(boolean enabled) {
        etUsername.setEnabled(enabled);
        etIncome.setEnabled(enabled);
        etEmail.setEnabled(false); // Optional: still readonly
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Get userId
        userId = getIntent().getStringExtra("userId");
        setupBottomNavBar(userId);

        // Highlight nav icon
        LinearLayout navAccount = findViewById(R.id.navAccount);
        navAccount.post(() -> {
            ImageView icon = navAccount.findViewById(R.id.iconAccount);
            if (icon != null) icon.setAlpha(1.0f);
        });

        // FAB redirects to category
        FloatingActionButton fab = findViewById(R.id.fabAddCategory);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, CategoryActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        // View references
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etIncome = findViewById(R.id.etDefaultIncome);
        btnEdit = findViewById(R.id.btnEdit);
        btnLogout = findViewById(R.id.btnLogout);

        setFieldsEnabled(false);

        // Remove underline for readonly email field
        etEmail.setBackground(null);

        // ViewModels
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        incomeViewModel = new ViewModelProvider(this).get(IncomeViewModel.class);

        // Load user data
        userViewModel.getUserById(userId).observe(this, user -> {
            if (user != null) {
                originalUsername = user.fullName;
                originalIncome = String.valueOf(user.defaultIncome);

                etUsername.setText(originalUsername);
                etEmail.setText(user.email);
                etIncome.setText(originalIncome);
            }
        });

        // Save button logic
        btnEdit.setOnClickListener(v -> {
            if (!isEditing) {
                isEditing = true;
                setFieldsEnabled(true);
                btnEdit.setText("Save Changes");
            } else {
                String name = etUsername.getText().toString().trim();
                String incomeStr = etIncome.getText().toString().trim();

                if (name.isEmpty() || incomeStr.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(this)
                        .setTitle("Confirm Update")
                        .setMessage("Apply changes to your account?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            double income = Double.parseDouble(incomeStr);
                            userViewModel.updateUserInfo(userId, name, income);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                String currentMonth = LocalDate.now().toString().substring(0, 7);
                                incomeViewModel.setIncomeForMonth(userId, currentMonth, income);
                            }

                            Toast.makeText(this, "Account info updated", Toast.LENGTH_SHORT).show();
                            isEditing = false;
                            setFieldsEnabled(false);
                            btnEdit.setText("Update your info");
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        // Logout logic
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Clear any stored session (if implemented)
                        Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
