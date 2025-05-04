package com.example.personal_finance_manager.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.personal_finance_manager.Model.IncomeEntity;
import com.example.personal_finance_manager.R;
import com.example.personal_finance_manager.ViewModel.IncomeViewModel;

public class SetIncomeActivity extends AppCompatActivity {

    private EditText etIncome;
    private Button btnSave;
    private IncomeViewModel incomeViewModel;
    private String userId;
    private String month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_income);

        etIncome = findViewById(R.id.etIncomeAmount);
        btnSave = findViewById(R.id.btnSaveIncome);

        userId = getIntent().getStringExtra("userId");
        month = getIntent().getStringExtra("month");

        incomeViewModel = new ViewModelProvider(this).get(IncomeViewModel.class);

        // Pre-fill current income if exists
        incomeViewModel.getIncome(userId, month).observe(this, incomeEntity -> {
            if (incomeEntity != null) {
                etIncome.setText(String.valueOf(incomeEntity.incomeAmount));
            }
        });

        btnSave.setOnClickListener(v -> {
            String input = etIncome.getText().toString().trim();
            if (!input.isEmpty()) {
                double income = Double.parseDouble(input);
                incomeViewModel.setIncomeForMonth(userId, month, income);
                Toast.makeText(this, "Income updated for " + month, Toast.LENGTH_SHORT).show();
                finish(); // go back to BudgetActivity
            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
