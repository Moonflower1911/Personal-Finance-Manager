package com.example.personal_finance_manager.Activity;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_finance_manager.Adapter.CategoryAdapter;
import com.example.personal_finance_manager.Adapter.IconGridAdapter;
import com.example.personal_finance_manager.Model.CategoryEntity;
import com.example.personal_finance_manager.Model.ExpenseEntity;
import com.example.personal_finance_manager.Model.UserCategorySetting;
import com.example.personal_finance_manager.R;
import com.example.personal_finance_manager.ViewModel.CategoryViewModel;
import com.example.personal_finance_manager.ViewModel.ExpenseViewModel;
import com.example.personal_finance_manager.ViewModel.UserCategorySettingViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private CategoryViewModel categoryViewModel;
    private String userId;

    private UserCategorySettingViewModel userCategorySettingViewModel;
    private ExpenseViewModel expenseViewModel;


    FloatingActionButton fab;
    int[] iconIds = {
            R.drawable.ic_groceries,
            R.drawable.ic_transport,
            R.drawable.ic_home,
            R.drawable.ic_budget,
            R.drawable.ic_dumbbell,
            R.drawable.ic_game,
            R.drawable.ic_clothing,
            R.drawable.ic_account
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        fab = findViewById(R.id.fabAddCategory);
        fab.setOnClickListener(v -> showAddCategoryDialog());
        userCategorySettingViewModel = new ViewModelProvider(this).get(UserCategorySettingViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);



        userId = getIntent().getStringExtra("userId"); // ✅ safely assign here

        recyclerView = findViewById(R.id.categoryRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new CategoryAdapter(new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(CategoryEntity category) {
                observeOnce(userCategorySettingViewModel.getLimit(userId, category.id), limit -> {
                    if (limit == null) {
                        promptSetLimit(category);
                    } else {
                        showExpenseDialog(category);
                    }
                });
            }

            @Override
            public void onCategoryLongClick(CategoryEntity category) {
                if (category.userId != null && category.userId.equals(userId)) {
                    // Show confirm delete dialog
                    new AlertDialog.Builder(CategoryActivity.this)
                            .setTitle("Delete Category")
                            .setMessage("Are you sure you want to delete \"" + category.name + "\"?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                categoryViewModel.deleteCategory(category); // Add this method in ViewModel + DAO
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    Toast.makeText(CategoryActivity.this, "Default categories can't be deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });



        recyclerView.setAdapter(adapter);

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        categoryViewModel.insertDefaultCategoriesIfEmpty(); // default ones (userId=null)

        categoryViewModel.getCategoriesForUser(userId).observe(this, categories -> {
            adapter.setCategories(categories);
        });

    }
    private void showAddCategoryDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText nameInput = dialogView.findViewById(R.id.editCategoryName);
        GridView iconGrid = dialogView.findViewById(R.id.iconGrid);

        final int[] selectedIcon = {R.drawable.ic_placeholder}; // Default

        // Set up the icon grid
        iconGrid.setAdapter(new IconGridAdapter(this, iconIds));
        iconGrid.setOnItemClickListener((parent, view, position, id) -> {
            selectedIcon[0] = iconIds[position];
        });

        new AlertDialog.Builder(this)
                .setTitle("Add New Category")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    if (!name.isEmpty()) {
                        CategoryEntity category = new CategoryEntity(name, selectedIcon[0], userId);
                        categoryViewModel.insertCategory(category);
                    } else {
                        Toast.makeText(this, "Name can't be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void promptSetLimit(CategoryEntity category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Monthly Limit for " + category.name);

        final EditText input = new EditText(this);
        input.setHint("Enter limit amount");
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Set", (dialog, which) -> {
            double limit = Double.parseDouble(input.getText().toString());
            UserCategorySetting setting = new UserCategorySetting(userId, category.id, limit);
            userCategorySettingViewModel.insertLimit(setting);
            showExpenseDialog(category); // continue with adding expense
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    private void showExpenseDialog(CategoryEntity category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Expense to " + category.name);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null);
        EditText amountInput = view.findViewById(R.id.inputAmount);
        EditText noteInput = view.findViewById(R.id.inputNote);
        DatePicker datePicker = view.findViewById(R.id.datePicker); // optional

        builder.setView(view);

        builder.setPositiveButton("Add", (dialog, which) -> {
            double amount = Double.parseDouble(amountInput.getText().toString());
            String note = noteInput.getText().toString();
            String date = getDateFromPicker(datePicker); // fallback to today if null

            ExpenseEntity expense = new ExpenseEntity();
            expense.userId = userId;
            expense.categoryId = category.id;
            expense.amount = amount;
            expense.note = note;
            expense.date = date;

            expenseViewModel.insertExpense(expense);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    private String getDateFromPicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1; // Months are 0-indexed
        int year = datePicker.getYear();

        // Format as yyyy-MM-dd (e.g., 2025-05-01)
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    private <T> void observeOnce(LiveData<T> liveData, androidx.lifecycle.Observer<T> observer) {
        liveData.observe(this, new androidx.lifecycle.Observer<T>() {
            @Override
            public void onChanged(T t) {
                liveData.removeObserver(this);
                observer.onChanged(t);
            }
        });
    }







}
