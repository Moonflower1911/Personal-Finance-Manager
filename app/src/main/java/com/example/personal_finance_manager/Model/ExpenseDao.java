package com.example.personal_finance_manager.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insertExpense(ExpenseEntity expense);

    @Delete
    void deleteExpense(ExpenseEntity expense);

    @Query("SELECT * FROM expenses WHERE userId = :userId")
    LiveData<List<ExpenseEntity>> getAllExpensesForUser(String userId);

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date LIKE :month || '%'")
    LiveData<Double> getTotalExpensesForUserMonth(String userId, String month);

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND categoryId = :categoryId AND date LIKE :month || '%'")
    LiveData<Double> getTotalExpensesForCategory(String userId, int categoryId, String month);

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date LIKE :month || '%' ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getExpensesForUserInMonth(String userId, String month);

    @Query("SELECT * FROM expenses  WHERE userId = :userId AND categoryId = :categoryId AND strftime('%Y-%m', date) = :month")
    LiveData<List<ExpenseEntity>> getExpensesForCategoryMonth(String userId, int categoryId, String month);

    @Query("SELECT * FROM expenses  WHERE userId = :userId AND strftime('%Y-%m', date) = :month")
    LiveData<List<ExpenseEntity>> getExpensesForMonth(String userId, String month);
}


