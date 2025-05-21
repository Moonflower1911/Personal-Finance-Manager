package com.example.personal_finance_manager.ViewModel;
import androidx.lifecycle.LiveData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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

    public LiveData<List<ExpenseEntity>> getExpensesFromLastSixMonths(String userId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -6); // Go back 6 months

        // Format the date to yyyy-MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = sdf.format(calendar.getTime()); // <--- calendar.getTime() returns a Date

        return expenseDao.getExpensesFromLastSixMonths(userId, startDate);
    }
    private float totalExpenses;





}
