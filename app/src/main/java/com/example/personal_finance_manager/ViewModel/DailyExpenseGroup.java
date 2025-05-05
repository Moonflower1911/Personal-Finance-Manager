package com.example.personal_finance_manager.ViewModel;


import com.example.personal_finance_manager.Model.ExpenseEntity;

import java.util.List;

public class DailyExpenseGroup {
    public String date; // e.g. "2025-05-03"
    public List<ExpenseEntity> expenses;

    public DailyExpenseGroup(String date, List<ExpenseEntity> expenses) {
        this.date = date;
        this.expenses = expenses;
    }
}
