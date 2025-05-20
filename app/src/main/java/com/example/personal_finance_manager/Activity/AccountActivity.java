package com.example.personal_finance_manager.Activity;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.personal_finance_manager.Model.CategoryEntity;
import com.example.personal_finance_manager.Model.ExpenseEntity;
import com.example.personal_finance_manager.R;
import com.example.personal_finance_manager.ViewModel.CategoryViewModel;
import com.example.personal_finance_manager.ViewModel.ExpenseViewModel;
import com.example.personal_finance_manager.ViewModel.IncomeViewModel;
import com.example.personal_finance_manager.ViewModel.UserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountActivity extends BaseActivity {

    private UserViewModel userViewModel;
    private IncomeViewModel incomeViewModel;

    private String userId;
    private EditText etIncome, etUsername, etEmail;
    private Button btnEdit, btnLogout;

    private String originalUsername = "";
    private String originalIncome = "";
    boolean isEditing = false;
    private ExpenseViewModel expenseViewModel;

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
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
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
        Button btnExportPdf = findViewById(R.id.btnExportPdf);

        btnExportPdf.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Exporter les données")
                    .setItems(new String[]{"Ce mois", "Historique complet"}, (dialog, which) -> {
                        if (which == 0) {
                            exportCurrentMonthToPDF(); // 👉 à implémenter
                        } else {
                            exportAllHistoryToPDF();   // 👉 à implémenter
                        }
                    })
                    .show();
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
    private void exportCurrentMonthToPDF() {
        String currentMonth = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? LocalDate.now().toString().substring(0, 7)
                : "2025-05"; // fallback si vieux Android
        expenseViewModel.getExpensesForMonth(userId, currentMonth).observe(this, expenses -> {
            if (expenses != null && !expenses.isEmpty()) {
                generatePdf(expenses, "rapport_" + currentMonth + ".pdf");
            } else {
                Toast.makeText(this, "Aucune dépense pour ce mois", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exportAllHistoryToPDF() {
        expenseViewModel.getAllExpensesForUser(userId).observe(this, expenses -> {
            if (expenses != null && !expenses.isEmpty()) {
                generatePdf(expenses, "rapport_complet.pdf");
            } else {
                Toast.makeText(this, "Aucune donnée à exporter", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void generatePdf(List<ExpenseEntity> expenses, String fileName) {
        // Étape 1 : Charger les noms de catégories
        CategoryViewModel categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        categoryViewModel.getCategoriesForUser(userId).observe(this, categories -> {
            Map<Integer, String> categoryNameMap = new HashMap<>();
            for (CategoryEntity category : categories) {
                categoryNameMap.put(category.id, category.name);
            }

            // Étape 2 : Générer le PDF
            PdfDocument document = new PdfDocument();
            Paint paint = new Paint();
            int pageNumber = 1;
            int y = 50;

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            canvas.drawText("Date", 40, y, paint);
            canvas.drawText("Catégorie", 140, y, paint);
            canvas.drawText("Note", 300, y, paint);
            canvas.drawText("Montant", 500, y, paint);
            y += 25;

            canvas.drawLine(40, y, 550, y, paint); // horizontal line
            y += 15;


            for (ExpenseEntity e : expenses) {
                String categoryName = categoryNameMap.getOrDefault(e.categoryId, "Inconnue");
                String note = (e.note != null && !e.note.isEmpty()) ? e.note : "---";

                canvas.drawText(e.date, 40, y, paint);
                canvas.drawText(categoryName, 140, y, paint);
                canvas.drawText(note, 300, y, paint);
                canvas.drawText(e.amount + " MAD", 500, y, paint);
                y += 20;

                if (y > 800) {
                    document.finishPage(page);
                    pageNumber++;
                    y = 50;

                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                }
            }


            document.finishPage(page);

            File pdfFile = new File(getExternalFilesDir(null), fileName);
            try {
                document.writeTo(new FileOutputStream(pdfFile));
                Toast.makeText(this, "Exporté vers: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (IOException ex) {
                ex.printStackTrace();
                Toast.makeText(this, "Erreur d'export", Toast.LENGTH_SHORT).show();
            } finally {
                document.close();
            }
        });
    }



}
