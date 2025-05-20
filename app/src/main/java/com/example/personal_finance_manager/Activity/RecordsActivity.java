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
import com.example.personal_finance_manager.ViewModel.UserCategorySettingViewModel;
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
    private UserCategorySettingViewModel userCategorySettingViewModel;
    private FloatingActionButton fab;

    private final Map<Integer, CategoryEntity> categoryMap = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        userId = getIntent().getStringExtra("userId");
        if (userId == null) finish();

        setupBottomNavBar(userId);
        currentMonth = LocalDate.now().toString().substring(0, 7);

        recordsViewModel = new ViewModelProvider(this).get(RecordsViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        incomeViewModel = new ViewModelProvider(this).get(IncomeViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userCategorySettingViewModel = new ViewModelProvider(this).get(UserCategorySettingViewModel.class);

        tvMonth = findViewById(R.id.budgetMonth);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvBalance = findViewById(R.id.tvBalance);
        btnPrev = findViewById(R.id.btnPreviousMonth);
        btnNext = findViewById(R.id.btnNextMonth);
        expenseDetailsLayout = findViewById(R.id.expenseDetailsLayout);
        fab = findViewById(R.id.fabAddCategory);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        setupMonthNavigation();
        loadCategoryMapThenRender();
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
            for (CategoryEntity c : categories) categoryMap.put(c.id, c);
            loadSummary();
            loadGroupedExpenses();
        });
    }

    private void loadSummary() {
        incomeViewModel.ensureIncomeForMonth(userId, currentMonth);
        incomeViewModel.getIncome(userId, currentMonth).observe(this, incomeEntity -> {
            if (incomeEntity == null || incomeEntity.incomeAmount == 0.0) {
                userViewModel.getUserById(userId).observe(this, user -> {
                    double fallbackIncome = (user != null) ? user.defaultIncome : 0.0;
                    tvIncome.setText(fallbackIncome + " MAD");
                    expenseViewModel.getTotalExpensesForUserMonth(userId, currentMonth).observe(this, totalExpenses -> {
                        double expenses = (totalExpenses != null) ? totalExpenses : 0.0;
                        tvExpense.setText(expenses + " MAD");
                        tvBalance.setText((fallbackIncome - expenses) + " MAD");
                    });
                });
            } else {
                double income = incomeEntity.incomeAmount;
                tvIncome.setText(income + " MAD");
                expenseViewModel.getTotalExpensesForUserMonth(userId, currentMonth).observe(this, totalExpenses -> {
                    double expenses = (totalExpenses != null) ? totalExpenses : 0.0;
                    tvExpense.setText(expenses + " MAD");
                    tvBalance.setText((income - expenses) + " MAD");
                });
            }
        });
    }

    private void loadGroupedExpenses() {
        recordsViewModel.getDailyGroupedExpenses(userId, currentMonth).observe(this, groupedList -> {
            expenseDetailsLayout.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(this);

            for (DailyExpenseGroup group : groupedList) {
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
                    TextView tvLimitInfo = card.findViewById(R.id.tvLimitInfo);
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

                    userCategorySettingViewModel.getLimit(userId, expense.categoryId, currentMonth).observe(this, limit -> {
                        double categoryLimit = (limit != null) ? limit : 0.0;
                        expenseViewModel.getTotalExpensesForCategory(userId, expense.categoryId, currentMonth).observe(this, spent -> {
                            double totalSpent = (spent != null) ? spent : 0.0;
                            double remaining = categoryLimit - totalSpent;

                            if (limit == null) {
                                tvLimitInfo.setText("No limit set");
                                tvLimitInfo.setTextColor(Color.GRAY);
                            } else {
                                tvLimitInfo.setText("Limit: " + categoryLimit + " MAD, Remaining: " + remaining + " MAD");
                                tvLimitInfo.setTextColor(remaining < 0 ? Color.RED : Color.DKGRAY);
                            }
                        });
                    });

                    expenseDetailsLayout.addView(card);
                }
            }
        });
    }
}
