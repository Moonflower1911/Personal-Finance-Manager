package com.example.personal_finance_manager.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;

import com.example.personal_finance_manager.Model.CategoryEntity;
import com.example.personal_finance_manager.Model.ExpenseEntity;
import com.example.personal_finance_manager.R;
import com.example.personal_finance_manager.ViewModel.CategoryViewModel;
import com.example.personal_finance_manager.ViewModel.DailyExpenseGroup;
import com.example.personal_finance_manager.ViewModel.ExpenseViewModel;
import com.example.personal_finance_manager.ViewModel.IncomeViewModel;
import com.example.personal_finance_manager.ViewModel.RecordsViewModel;
import com.example.personal_finance_manager.ViewModel.UserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecordsActivity extends BaseActivity {

    private String userId;
    private String currentMonth;
    private TextView tvMonth, tvIncome, tvExpense, tvBalance;
    private ImageView btnPrev, btnNext;
    private LinearLayout expenseDetailsLayout;

    private RecordsViewModel recordsViewModel;
    private CategoryViewModel categoryViewModel;
    private ExpenseViewModel expenseViewModel;
    private IncomeViewModel incomeViewModel;
    private UserViewModel userViewModel;
    private FloatingActionButton fab;


    private final Map<Integer, CategoryEntity> categoryMap = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        // ✅ Get userId and validate
        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            Log.e("RecordsActivity", "No userId provided. Finishing activity.");
            finish();
            return;
        }

        // ✅ Bottom navbar highlight
        setupBottomNavBar(userId);
        LinearLayout navRecords = findViewById(R.id.navRecords);
        navRecords.post(() -> {
            ImageView recordsIcon = navRecords.findViewById(R.id.iconRecords);
            if (recordsIcon != null) recordsIcon.setAlpha(1.0f);
        });

        currentMonth = LocalDate.now().toString().substring(0, 7);

        // ✅ ViewModel init
        recordsViewModel = new ViewModelProvider(this).get(RecordsViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        incomeViewModel = new ViewModelProvider(this).get(IncomeViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // ✅ Bind views
        tvMonth = findViewById(R.id.budgetMonth);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvBalance = findViewById(R.id.tvBalance);
        btnPrev = findViewById(R.id.btnPreviousMonth);
        btnNext = findViewById(R.id.btnNextMonth);
        expenseDetailsLayout = findViewById(R.id.expenseDetailsLayout);
        fab = findViewById(R.id.fabAddCategory);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(RecordsActivity.this, CategoryActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        setupMonthNavigation();
        loadCategoryMapThenRender();
    }
    private void openAnalysis() {
        Intent i = new Intent(RecordsActivity.this, AnalysisActivity.class);
        i.putExtra("userId", userId);
        i.putExtra("month", currentMonth);   //  <-- ajoute cette ligne
        startActivity(i);
    }

    private void setupMonthNavigation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            updateMonthLabel();
        }

        btnPrev.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                currentMonth = LocalDate.parse(currentMonth + "-01").minusMonths(1).toString().substring(0, 7);
                updateMonthLabel();
            }
            loadCategoryMapThenRender();
        });

        btnNext.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                currentMonth = LocalDate.parse(currentMonth + "-01").plusMonths(1).toString().substring(0, 7);
                updateMonthLabel();
            }
            loadCategoryMapThenRender();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateMonthLabel() {
        LocalDate parsed = LocalDate.parse(currentMonth + "-01");
        String label = parsed.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " - " + parsed.getYear();
        tvMonth.setText(label);
    }

    private void loadCategoryMapThenRender() {
        categoryViewModel.getCategoriesForUser(userId).observe(this, categories -> {
            categoryMap.clear();
            for (CategoryEntity c : categories) {
                categoryMap.put(c.id, c);
            }
            loadSummary();
            loadGroupedExpenses();
        });
    }

    private void loadSummary() {
        incomeViewModel.ensureIncomeForMonth(userId, currentMonth); // ensures entry exists

        incomeViewModel.getIncome(userId, currentMonth).observe(this, incomeEntity -> {
            if (incomeEntity == null || incomeEntity.incomeAmount == 0.0) {
                userViewModel.getUserById(userId).observe(this, user -> {
                    double fallbackIncome = (user != null) ? user.defaultIncome : 0.0;
                    tvIncome.setText(fallbackIncome + " MAD");

                    expenseViewModel.getTotalExpensesForUserMonth(userId, currentMonth).observe(this, totalExpenses -> {
                        double expenses = (totalExpenses != null) ? totalExpenses : 0.0;
                        double balance = fallbackIncome - expenses;

                        tvExpense.setText(expenses + " MAD");
                        tvBalance.setText(balance + " MAD");
                    });
                });
            } else {
                double incomeAmount = incomeEntity.incomeAmount;
                tvIncome.setText(incomeAmount + " MAD");

                expenseViewModel.getTotalExpensesForUserMonth(userId, currentMonth).observe(this, totalExpenses -> {
                    double expenses = (totalExpenses != null) ? totalExpenses : 0.0;
                    double balance = incomeAmount - expenses;

                    tvExpense.setText(expenses + " MAD");
                    tvBalance.setText(balance + " MAD");
                });
            }
        });
    }

    private void loadGroupedExpenses() {
        recordsViewModel.getDailyGroupedExpenses(userId, currentMonth).observe(this, groupedList -> {
            expenseDetailsLayout.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(this);

            for (DailyExpenseGroup group : groupedList) {
                // Date header
                TextView dayHeader = new TextView(this);
                dayHeader.setText(group.date);
                dayHeader.setTextSize(16f);
                dayHeader.setTextColor(Color.BLACK);
                dayHeader.setPadding(0, 16, 0, 8);
                expenseDetailsLayout.addView(dayHeader);

                for (ExpenseEntity expense : group.expenses) {
                    View card = inflater.inflate(R.layout.item_expense_card, expenseDetailsLayout, false);

                    TextView tvCategory = card.findViewById(R.id.tvCategoryName);
                    TextView tvNote = card.findViewById(R.id.tvNote);
                    TextView tvAmount = card.findViewById(R.id.tvAmount);
                    ImageView icon = card.findViewById(R.id.ivCategoryIcon);

                    CategoryEntity category = categoryMap.get(expense.categoryId);
                    if (category != null) {
                        tvCategory.setText(category.name);
                        icon.setImageResource(category.iconResId);
                    } else {
                        tvCategory.setText("Unknown");
                        icon.setImageResource(R.drawable.ic_piggy);
                    }

                    tvNote.setText(expense.note != null ? expense.note : "");
                    tvAmount.setText("-" + expense.amount + " MAD");
                    tvAmount.setTextColor(Color.RED);

                    expenseDetailsLayout.addView(card);
                }
            }
        });
    }
}
