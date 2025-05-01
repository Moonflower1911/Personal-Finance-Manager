package com.example.personal_finance_manager.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;



@Dao
public interface CategoryDao {
    @Insert
    void insert(CategoryEntity category);

    @Query("SELECT * FROM categories WHERE userId IS NULL OR userId = :userId")
    LiveData<List<CategoryEntity>> getCategoriesForUser(String userId);

    @Query("SELECT * FROM categories WHERE userId IS NULL OR userId = :userId")
    List<CategoryEntity> getCategoriesForUserNow(String userId); // Synchronous

    @Query("SELECT * FROM categories")
    List<CategoryEntity> getCategoriesNow();

    @Delete
    void delete(CategoryEntity category);

}

