package com.example.personal_finance_manager.Activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import androidx.lifecycle.ViewModelProvider;

import com.example.personal_finance_manager.Model.AppDatabase;
import com.example.personal_finance_manager.Model.CategoryEntity;
import com.example.personal_finance_manager.Model.ExpenseEntity;
import com.example.personal_finance_manager.R;
import com.example.personal_finance_manager.ViewModel.CategoryViewModel;
import com.example.personal_finance_manager.ViewModel.ExpenseViewModel;
import com.example.personal_finance_manager.ViewModel.IncomeViewModel;
import com.example.personal_finance_manager.ViewModel.UserCategorySettingViewModel;
import com.example.personal_finance_manager.ViewModel.UserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

        Button btnImport = findViewById(R.id.btnImportTransactions);
        btnImport.setOnClickListener(v -> importBankTransactions());

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
        incomeViewModel.ensureIncomeForMonth(userId, currentMonth); // still run this to insert if needed

        incomeViewModel.getIncome(userId, currentMonth).observe(this, incomeEntity -> {
            if (incomeEntity == null || incomeEntity.incomeAmount == 0.0) {
                // Fallback to defaultIncome
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
        // Observe limit for current month
        userCategorySettingViewModel.getLimit(userId, category.id, currentMonth).observe(this, limitValue -> {
            double limit = (limitValue != null) ? limitValue : 0.0;

            // Observe spent for current month
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

        userCategorySettingViewModel.getLimit(userId, category.id, currentMonth).observe(this, limitValue -> {
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
        card.setOnClickListener(v -> {
            showTransactionListDialog(category);
        });
        TextView tvCategory = card.findViewById(R.id.tvCategoryName);
        TextView tvLimit = card.findViewById(R.id.tvLimit);
        TextView tvSpent = card.findViewById(R.id.tvSpent);
        TextView tvRemaining = card.findViewById(R.id.tvRemaining);
        ProgressBar progressBar = card.findViewById(R.id.budgetProgressBar);
        ImageView iconView = card.findViewById(R.id.budgetIcon);
        ImageView btnRecommend = card.findViewById(R.id.btnRecommend);

// Only show for test user
        if (userId.equals("test1@email.com")) {
            btnRecommend.setVisibility(View.VISIBLE);
            btnRecommend.setOnClickListener(v -> {
                showCsvBasedRecommendation(category); // You define this method next
            });
        } else {
            btnRecommend.setVisibility(View.GONE);
        }


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
        int percent = (limit > 0.001) ? (int) ((spent / limit) * 100) : 0;
        progressBar.setProgress(percent);

// Color tint
        if (percent <= 50) {
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // green
        } else if (percent <= 75) {
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFA500"))); // orange
        } else {
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#F44336"))); // red
            sendSpendingNotification(category.name, percent); // ✅ Trigger the notification
        }
        iconView.setImageResource(category.iconResId);

        return card;
    }
    private void showCsvBasedRecommendation(CategoryEntity category) {
        try {
            // Simulated CSV lines (excluding the header)
            String[] csvLines = {
                    "1,1,4000,200,190,210", "2,1,4000,180,175,200", "3,1,4200,220,220,230",
                    "4,1,4300,210,200,220", "5,1,4400,215,210,225", "6,1,4500,225,220,230",
                    "7,1,4600,230,225,235", "8,1,4700,240,230,240", "9,1,4800,245,240,245",
                    "10,1,4900,250,248,255", "11,1,5000,255,252,260", "12,1,5100,260,260,265",
                    "1,2,4000,100,95,110", "2,2,4200,110,100,115", "3,2,4300,115,110,120",
                    "4,2,4400,120,115,125", "5,2,4500,125,120,130", "6,2,4600,130,125,135",
                    "7,2,4700,135,130,140", "8,2,4800,140,135,145", "9,2,4900,145,140,150",
                    "10,2,5000,150,145,155", "11,2,5100,155,150,160", "12,2,5200,160,155,165",
                    "1,3,4000,300,280,310", "2,3,4200,310,300,320", "3,3,4300,320,310,330",
                    "4,3,4400,330,320,340", "5,3,4500,340,330,350", "6,3,4600,350,340,360",
                    "7,3,4700,360,350,370", "8,3,4800,370,360,380", "9,3,4900,380,370,390",
                    "10,3,5000,390,380,400", "11,3,5100,400,390,410", "12,3,5200,410,400,420",
                    "1,4,4000,150,145,155", "2,4,4100,155,150,160", "3,4,4200,160,155,165",
                    "4,4,4300,165,160,170", "5,4,4400,170,165,175", "6,4,4500,175,170,180",
                    "7,4,4600,180,175,185", "8,4,4700,185,180,190", "9,4,4800,190,185,195",
                    "10,4,4900,195,190,200", "11,4,5000,200,195,205", "12,4,5100,205,200,210",
                    "1,5,4000,80,75,85", "2,5,4100,85,80,90", "3,5,4200,90,85,95",
                    "4,5,4300,95,90,100", "5,5,4400,100,95,105", "6,5,4500,105,100,110",
                    "7,5,4600,110,105,115", "8,5,4700,115,110,120", "9,5,4800,120,115,125",
                    "10,5,4900,125,120,130", "11,5,5000,130,125,135", "12,5,5100,135,130,140",
                    "1,6,4000,200,190,210", "2,6,4100,210,200,220", "3,6,4200,220,210,230",
                    "4,6,4300,230,220,240", "5,6,4400,240,230,250", "6,6,4500,250,240,260",
                    "7,6,4600,260,250,270", "8,6,4700,270,260,280", "9,6,4800,280,270,290",
                    "10,6,4900,290,280,300", "11,6,5000,300,290,310", "12,6,5100,310,300,320"
            };

            float sum = 0;
            int count = 0;

            for (String line : csvLines) {
                String[] tokens = line.split(",");
                if (tokens.length < 6) continue;

                int categoryId = Integer.parseInt(tokens[1].trim());
                float predictedLimit = Float.parseFloat(tokens[5].trim());

                if (categoryId == category.id) {
                    sum += predictedLimit;
                    count++;
                }
            }

            if (count > 0) {
                int avgRecommendation = Math.round(sum / count);
                new AlertDialog.Builder(this)
                        .setTitle("💡 Recommended Limit")
                        .setMessage("Based on your past data,\nwe recommend: " + avgRecommendation + " MAD for " + category.name)
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                Toast.makeText(this, "No data found for this category", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("CSV_ERROR", "Simulated CSV read failed: " + e.getMessage());
            Toast.makeText(this, "Failed to read embedded CSV", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTransactionListDialog(CategoryEntity category) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_transaction_list, null);
        LinearLayout transactionListLayout = dialogView.findViewById(R.id.transactionListLayout);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setNegativeButton("Close", null)
                .create();

        expenseViewModel.getExpensesForCategoryMonth(userId, category.id, currentMonth)
                .observe(this, expenses -> {
                    transactionListLayout.removeAllViews();
                    for (ExpenseEntity expense : expenses) {
                        View row = LayoutInflater.from(this).inflate(R.layout.item_transaction_row, transactionListLayout, false);

                        TextView text = row.findViewById(R.id.transactionText);
                        ImageView deleteIcon = row.findViewById(R.id.deleteIcon);
                        String note = (expense.note != null && !expense.note.trim().isEmpty())
                                ? expense.note
                                : "Not Specified";

                        String line = note + " | " + expense.amount + " MAD | " + expense.date;
                        text.setText(line);



                        deleteIcon.setOnClickListener(v -> {
                            expenseViewModel.deleteExpense(expense);
                            transactionListLayout.removeView(row);
                        });

                        transactionListLayout.addView(row);
                    }
                });

        dialog.show();
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

    private void sendSpendingNotification(String categoryName, int percent) {
        String channelId = "spending_alerts";
        String channelName = "Spending Alerts";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifies when category spending exceeds set limits");
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_warning) // Make sure you have this icon
                .setContentTitle("⚠️ Budget Alert: " + categoryName)
                .setContentText("You've spent " + percent + "% of your " + categoryName + " budget.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Notify (use category name hash to prevent ID collision)
        notificationManager.notify(categoryName.hashCode(), builder.build());
    }

    private void importBankTransactions() {
        // Simulation de lignes CSV
        String[] csvLines = {
                "date,amount,categoryName,description",
                "2025-05-01,800,Rent / Mortgage,Loyer du mois",
                "2025-05-02,50,Water,Lydec facture avril",
                "2025-05-03,300,Electricity,Electricité mars",
                "2025-05-04,150,Gas,Bon de gaz",
                "2025-05-05,200,Internet,Maroc Telecom",
                "2025-05-06,600,Groceries,Marjane courses",
                "2025-05-07,40,Transport,Uber Casablanca",
                "2025-05-08,120,Health,Pharmacie centrale",
                "2025-05-09,90,Food & Dining,McDonald's",
                "2025-05-10,100,Leisure,Cinéma IMAX",
                "2025-05-11,250,Clothing,Zara shopping",
                "2025-05-12,180,Fitness,Abonnement salle"
        };

        // Mappage nom de catégorie -> ID (doit correspondre à ta DB)
        Map<String, Integer> categoryMap = new HashMap<>();
        categoryMap.put("Rent / Mortgage", 1);
        categoryMap.put("Water", 2);
        categoryMap.put("Electricity", 3);
        categoryMap.put("Gas", 4);
        categoryMap.put("Internet", 5);
        categoryMap.put("Groceries", 6);
        categoryMap.put("Transport", 7);
        categoryMap.put("Health", 8);
        categoryMap.put("Food & Dining", 9);
        categoryMap.put("Leisure", 10);
        categoryMap.put("Clothing", 11);
        categoryMap.put("Fitness", 12);


        for (int i = 1; i < csvLines.length; i++) {
            String line = csvLines[i];
            String[] tokens = line.split(",");

            String dateStr = tokens[0];
            float amount = Float.parseFloat(tokens[1]);
            String categoryName = tokens[2];
            String description = tokens[3];

            Integer categoryId = categoryMap.get(categoryName);
            if (categoryId == null) {
                Log.w("IMPORT", "Catégorie inconnue ignorée: " + categoryName);
                continue;
            }

            ExpenseEntity expense = new ExpenseEntity();
            expense.date = dateStr;
            expense.amount = amount;
            expense.categoryId = categoryId;
            expense.note = description;
            expense.userId = userId; // si applicable

            AppDatabase.databaseWriteExecutor.execute(() ->
                    AppDatabase.getInstance(this).expenseDao().insertExpense(expense)
            );
        }

        Toast.makeText(this, "Transactions importées avec succès", Toast.LENGTH_SHORT).show();
    }



}
