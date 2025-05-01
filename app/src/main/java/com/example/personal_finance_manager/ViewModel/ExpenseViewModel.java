package com.example.personal_finance_manager.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.personal_finance_manager.Model.AppDatabase;
import com.example.personal_finance_manager.Model.ExpenseDao;
import com.example.personal_finance_manager.Model.ExpenseEntity;

import java.util.List;
import java.util.concurrent.Executors;

public class ExpenseViewModel extends AndroidViewModel {
    private final ExpenseDao expenseDao;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        expenseDao = AppDatabase.getInstance(application).expenseDao();
    }

    public LiveData<List<ExpenseEntity>> getExpensesForUser(String userId) {
        return expenseDao.getAllExpensesForUser(userId);
    }

    public LiveData<Double> getSpentThisMonth(String userId, int categoryId, String month) {
        return expenseDao.getMonthlySpending(userId, categoryId, month);
    }

    public void insertExpense(ExpenseEntity expense) {
        Executors.newSingleThreadExecutor().execute(() -> expenseDao.insert(expense));
    }
}
