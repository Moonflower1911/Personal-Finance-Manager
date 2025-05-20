package com.example.personal_finance_manager.Model;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.lifecycle.LiveData;


import com.example.personal_finance_manager.Model.UserCategorySetting;

import java.util.List;

@Dao
public interface UserCategorySettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserCategorySetting setting);

    @Query("SELECT monthlyLimit FROM user_category_settings WHERE userId = :userId AND categoryId = :categoryId AND month = :month")
    LiveData<Double> getLimit(String userId, int categoryId, String month);

    @Query("SELECT * FROM user_category_settings WHERE userId = :userId AND month = :month")
    LiveData<List<UserCategorySetting>> getAllLimitsForUserInMonth(String userId, String month);
}


