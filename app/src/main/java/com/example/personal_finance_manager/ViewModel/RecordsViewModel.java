package com.example.personal_finance_manager.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.personal_finance_manager.Model.AppDatabase;
import com.example.personal_finance_manager.Model.ExpenseDao;
import com.example.personal_finance_manager.Model.ExpenseEntity;
import com.example.personal_finance_manager.Model.IncomeDao;
import com.example.personal_finance_manager.Model.IncomeEntity;
import com.example.personal_finance_manager.Model.UserDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RecordsViewModel extends AndroidViewModel {

    private final ExpenseDao expenseDao;
    private final IncomeDao incomeDao;
    private final UserDao userDao;

    public RecordsViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.expenseDao = db.expenseDao();
        this.incomeDao = db.incomeDao();
        this.userDao = db.userDao();
    }

    public LiveData<IncomeEntity> getIncomeForMonth(String userId, String month) {
        return incomeDao.getIncomeForUserMonth(userId, month);
    }

    public LiveData<Double> getTotalExpensesForMonth(String userId, String month) {
        return expenseDao.getTotalExpensesForUserMonth(userId, month);
    }

    public LiveData<List<ExpenseEntity>> getExpensesForMonth(String userId, String month) {
        return expenseDao.getExpensesForUserInMonth(userId, month);
    }

    public LiveData<List<DailyExpenseGroup>> getDailyGroupedExpenses(String userId, String month) {
        MutableLiveData<List<DailyExpenseGroup>> result = new MutableLiveData<>();

        getExpensesForMonth(userId, month).observeForever(expenseList -> {
            Map<String, List<ExpenseEntity>> grouped = new TreeMap<>(Collections.reverseOrder());
            for (ExpenseEntity e : expenseList) {
                String day = e.date; // format: yyyy-MM-dd
                grouped.computeIfAbsent(day, k -> new ArrayList<>()).add(e);
            }

            List<DailyExpenseGroup> output = new ArrayList<>();
            for (String day : grouped.keySet()) {
                output.add(new DailyExpenseGroup(day, grouped.get(day)));
            }

            result.setValue(output);
        });

        return result;
    }
}

