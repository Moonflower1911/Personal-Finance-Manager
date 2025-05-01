package com.example.personal_finance_manager.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.personal_finance_manager.Model.ExpenseEntity;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    void insert(ExpenseEntity expense);

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getAllExpensesForUser(String userId);

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND categoryId = :categoryId AND strftime('%Y-%m', date) = :month")
    LiveData<Double> getMonthlySpending(String userId, int categoryId, String month);
}

