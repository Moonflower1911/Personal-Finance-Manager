package com.example.personal_finance_manager.Model;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.lifecycle.LiveData;


import com.example.personal_finance_manager.Model.UserCategorySetting;

@Dao
public interface UserCategorySettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserCategorySetting setting);

    @Query("SELECT monthlyLimit FROM user_category_settings WHERE userId = :userId AND categoryId = :categoryId")
    LiveData<Double> getLimit(String userId, int categoryId);
}

