package com.example.personal_finance_manager.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "user_category_settings", primaryKeys = {"userId", "categoryId"})
public class UserCategorySetting {
    @NonNull
    public String userId;
    public int categoryId;
    public double monthlyLimit;

    public UserCategorySetting(@NonNull String userId, int categoryId, double monthlyLimit) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.monthlyLimit = monthlyLimit;
    }
}



