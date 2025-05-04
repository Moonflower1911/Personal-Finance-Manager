// IncomeDao.java
package com.example.personal_finance_manager.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface IncomeDao {

    @Insert
    void insertIncome(IncomeEntity income);

    @Update
    void updateIncome(IncomeEntity income);

    @Query("SELECT * FROM income WHERE userId = :userId AND month = :month LIMIT 1")
    LiveData<IncomeEntity> getIncomeForUserMonth(String userId, String month);

    // For background operations (non-LiveData)
    @Query("SELECT * FROM income WHERE userId = :userId AND month = :month LIMIT 1")
    IncomeEntity getIncomeRaw(String userId, String month);

}
