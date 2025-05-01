package com.example.personal_finance_manager.Model;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public int iconResId;

    @Nullable
    public String userId; // null = default category
    // ✅ Add this constructor
    public CategoryEntity(String name, int iconResId, @Nullable String userId) {
        this.name = name;
        this.iconResId = iconResId;
        this.userId = userId;
    }

    // ✅ Keep this no-arg constructor for Room
    public CategoryEntity() {}


}

