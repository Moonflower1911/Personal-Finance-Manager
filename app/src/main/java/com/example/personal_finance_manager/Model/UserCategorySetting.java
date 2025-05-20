package com.example.personal_finance_manager.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;


@Entity(tableName = "user_category_settings", primaryKeys = {"userId", "categoryId", "month"})
public class UserCategorySetting {
    @NonNull
    public String userId;
    public int categoryId;

    @NonNull
    public String month; // Format: "YYYY-MM"

    public double monthlyLimit;

    public UserCategorySetting(@NonNull String userId, int categoryId, double monthlyLimit, @NonNull String month) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.month = month;
        this.monthlyLimit = monthlyLimit;
    }
}




