// IncomeEntity.java
package com.example.personal_finance_manager.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "income")
public class IncomeEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String userId;

    // Format: "2025-05"
    public String month;

    public double incomeAmount;

    public IncomeEntity(String userId, String month, double incomeAmount) {
        this.userId = userId;
        this.month = month;
        this.incomeAmount = incomeAmount;
    }
}
