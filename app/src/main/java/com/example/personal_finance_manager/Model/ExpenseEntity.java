package com.example.personal_finance_manager.Model;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class ExpenseEntity{
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String userId;
    public int categoryId;
    public double amount;
    public String date;
    public String note;
}
