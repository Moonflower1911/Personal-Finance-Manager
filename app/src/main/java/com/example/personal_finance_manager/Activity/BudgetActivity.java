package com.example.personal_finance_manager.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.example.personal_finance_manager.Model.CategoryEntity;
import com.example.personal_finance_manager.R;
import com.example.personal_finance_manager.ViewModel.CategoryViewModel;
import com.example.personal_finance_manager.ViewModel.ExpenseViewModel;
import com.example.personal_finance_manager.ViewModel.IncomeViewModel;
import com.example.personal_finance_manager.ViewModel.UserCategorySettingViewModel;
import com.example.personal_finance_manager.ViewModel.UserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BudgetActivity extends BaseActivity {

    private String userId;
    private String currentMonth;
    private TextView tvIncome, tvExpense, tvBalance;
    private LinearLayout budgetListLayout;
    private FloatingActionButton fab;

    private CategoryViewModel categoryViewModel;
    private ExpenseViewModel expenseViewModel;
    private UserCategorySettingViewModel userCategorySettingViewModel;
    private IncomeViewModel incomeViewModel;
    private final List<View> pendingBudgetCards = new ArrayList<>();
    private final Set<Integer> renderedCategoryIds = new HashSet<>();
    private UserViewModel userViewModel;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        userId = getIntent().getStringExtra("userId"); // ✅ safely assign here
        setupBottomNavBar(userId); // ✅ ← THIS IS MISSING RIGHT NOW
        LinearLayout navBudget = findViewById(R.id.navBudget);
        navBudget.post(() -> {
            ImageView budgetIcon = navBudget.findViewById(R.id.iconBudget);
            if (budgetIcon != null) budgetIcon.setAlpha(1.0f);
        });


        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentMonth = LocalDate.now().toString().substring(0, 7); // "yyyy-MM"
        }

        TextView tvMonth = findViewById(R.id.budgetMonth);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate now = LocalDate.now();
            currentMonth = now.toString().substring(0, 7);
            String formattedMonth = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            tvMonth.setText(formattedMonth + " - " + now.getYear());
        }


        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvBalance = findViewById(R.id.tvBalance);
        budgetListLayout = findViewById(R.id.budgetListLayout);
        fab = findViewById(R.id.fabAddCategory);

        // ViewModels
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        userCategorySettingViewModel = new ViewModelProvider(this).get(UserCategorySettingViewModel.class);
        incomeViewModel = new ViewModelProvider(this).get(IncomeViewModel.class);

        tvIncome.setOnClickListener(v -> {
            Intent intent = new Intent(BudgetActivity.this, SetIncomeActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("month", currentMonth);
            startActivity(intent);
        });
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(BudgetActivity.this, CategoryActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        loadSummary();
        loadBudgetDetails();
    }

    private void loadSummary() {
        incomeViewModel.ensureIncomeForMonth(userId, currentMonth);

        incomeViewModel.getIncome(userId, currentMonth).observe(this, incomeEntity -> {
            double incomeAmount = (incomeEntity != null) ? incomeEntity.incomeAmount : 0.0;
            tvIncome.setText(incomeAmount + " MAD");

            expenseViewModel.getTotalExpensesForUserMonth(userId, currentMonth).observe(this, totalExpenses -> {
                double expenses = (totalExpenses != null) ? totalExpenses : 0.0;
                double balance = incomeAmount - expenses;

                tvExpense.setText(expenses + " MAD");
                tvBalance.setText(balance + " MAD");
            });
        });
    }


    private void updateSummaryUI(double incomeAmount) {
        tvIncome.setText(incomeAmount + " MAD");

        expenseViewModel.getTotalExpensesForUserMonth(userId, currentMonth).observe(this, totalExpenses -> {
            double expenses = (totalExpenses != null) ? totalExpenses : 0.0;
            double balance = incomeAmount - expenses;

            tvExpense.setText(expenses + " MAD");
            tvBalance.setText(balance + " MAD");
        });
    }


    private void loadCategoryBudgetData(CategoryEntity category) {
        // Observe limit once
        userCategorySettingViewModel.getLimit(userId, category.id).observe(this, limitValue -> {
            double limit = (limitValue != null) ? limitValue : 0.0;

            // Observe spent once
            expenseViewModel.getTotalExpensesForCategory(userId, category.id, currentMonth)
                    .observe(this, spent -> {
                        double used = (spent != null) ? spent : 0.0;
                        double remaining = limit - used;

                        // Prevent duplicates: only add if not already displayed
                        addBudgetCard(category, limit, used, remaining);
                    });
        });
    }


    private void loadBudgetDetails() {
        categoryViewModel.getCategoriesForUser(userId).observe(this, categories -> {
            budgetListLayout.removeAllViews();
            renderedCategoryIds.clear(); // ✅ reset tracking
            for (CategoryEntity category : categories) {
                observeLimitAndSpent(category);
            }
        });
    }




    @Override
    protected void onResume() {
        super.onResume();
        loadSummary();        // Reload top section
        loadBudgetDetails();  // Reload dynamic budget cards
    }

    private void observeLimitAndSpent(CategoryEntity category) {
        final double[] limitHolder = { -1 };
        final double[] spentHolder = { -1 };

        userCategorySettingViewModel.getLimit(userId, category.id).observe(this, limitValue -> {
            limitHolder[0] = (limitValue != null) ? limitValue : 0.0;
            maybeAddCard(category, limitHolder[0], spentHolder[0]);
        });

        expenseViewModel.getTotalExpensesForCategory(userId, category.id, currentMonth).observe(this, spentValue -> {
            spentHolder[0] = (spentValue != null) ? spentValue : 0.0;
            maybeAddCard(category, limitHolder[0], spentHolder[0]);
        });
    }

    private void maybeAddCard(CategoryEntity category, double limit, double spent) {
        if (limit >= 0 && spent >= 0 && !renderedCategoryIds.contains(category.id)) {
            double remaining = limit - spent;
            View card = createBudgetCard(category, limit, spent, remaining);
            budgetListLayout.addView(card);
            renderedCategoryIds.add(category.id); // ✅ prevent future duplicates
        }
    }


    private View createBudgetCard(CategoryEntity category, double limit, double spent, double remaining) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_budget_card, budgetListLayout, false);

        TextView tvCategory = card.findViewById(R.id.tvCategoryName);
        TextView tvLimit = card.findViewById(R.id.tvLimit);
        TextView tvSpent = card.findViewById(R.id.tvSpent);
        TextView tvRemaining = card.findViewById(R.id.tvRemaining);
        ProgressBar progressBar = card.findViewById(R.id.budgetProgressBar);
        ImageView iconView = card.findViewById(R.id.budgetIcon);

        tvCategory.setText(category.name);
        if (limit == 0.0) {
            tvLimit.setText("No limit set");
            tvRemaining.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else {
            tvLimit.setText("Limit: " + limit + " MAD");
            progressBar.setVisibility(View.VISIBLE);
        }
        tvSpent.setText("Spent: " + spent + " MAD");

        if (spent > limit && limit > 0) {
            tvRemaining.setText("Over Limit: " + (spent - limit) + " MAD");
            tvRemaining.setTextColor(Color.RED);
        } else {
            tvRemaining.setText("Remaining: " + remaining + " MAD");
            tvRemaining.setTextColor(Color.BLACK);
        }

        progressBar.setMax(100);
        progressBar.setProgress(limit > 0 ? (int) ((spent / limit) * 100) : 0);
        iconView.setImageResource(category.iconResId);

        return card;
    }


    private void addBudgetCard(CategoryEntity category, double limit, double spent, double remaining) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_budget_card, budgetListLayout, false);

        TextView tvCategory = card.findViewById(R.id.tvCategoryName);
        TextView tvLimit = card.findViewById(R.id.tvLimit);
        TextView tvSpent = card.findViewById(R.id.tvSpent);
        TextView tvRemaining = card.findViewById(R.id.tvRemaining);
        ProgressBar progressBar = card.findViewById(R.id.budgetProgressBar);
        ImageView iconView = card.findViewById(R.id.budgetIcon);

        tvCategory.setText(category.name);
        if (limit == 0.0) {
            tvLimit.setText("No limit set");
            tvRemaining.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else {
            tvLimit.setText("Limit: " + limit + " MAD");
            progressBar.setVisibility(View.VISIBLE);
        }
        tvSpent.setText("Spent: " + spent + " MAD");

        // Remaining vs Over Limit
        if (spent > limit && limit > 0) {
            tvRemaining.setText("Over Limit: " + (spent - limit) + " MAD");
            tvRemaining.setTextColor(Color.RED);
        } else {
            tvRemaining.setText("Remaining: " + remaining + " MAD");
            tvRemaining.setTextColor(Color.BLACK);
        }

        // ProgressBar logic
        if (limit > 0) {
            int percent = (int) ((spent / limit) * 100);
            progressBar.setProgress(percent);
        } else {
            progressBar.setProgress(0);
        }

        progressBar.setMax(100);
        iconView.setImageResource(category.iconResId);

        budgetListLayout.addView(card);
    }
}
