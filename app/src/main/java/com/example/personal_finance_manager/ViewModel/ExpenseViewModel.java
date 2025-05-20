package com.example.personal_finance_manager.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.personal_finance_manager.Model.AppDatabase;
import com.example.personal_finance_manager.Model.ExpenseEntity;
import com.example.personal_finance_manager.Model.ExpenseDao;

import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {

    private final ExpenseDao expenseDao;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        expenseDao = db.expenseDao();
    }

    public void insertExpense(ExpenseEntity expense) {
        AppDatabase.databaseWriteExecutor.execute(() -> expenseDao.insertExpense(expense));
    }

    public void deleteExpense(ExpenseEntity expense) {
        AppDatabase.databaseWriteExecutor.execute(() -> expenseDao.deleteExpense(expense));
    }

    public LiveData<List<ExpenseEntity>> getAllExpensesForUser(String userId) {
        return expenseDao.getAllExpensesForUser(userId);
    }

    public LiveData<Double> getTotalExpensesForUserMonth(String userId, String month) {
        return expenseDao.getTotalExpensesForUserMonth(userId, month);
    }

    public LiveData<Double> getTotalExpensesForCategory(String userId, int categoryId, String month) {
        return expenseDao.getTotalExpensesForCategory(userId, categoryId, month);
    }
    public LiveData<List<ExpenseEntity>> getExpensesForCategoryMonth(String userId, int categoryId, String month) {
        return expenseDao.getExpensesForCategoryMonth(userId, categoryId, month);
    }
}
