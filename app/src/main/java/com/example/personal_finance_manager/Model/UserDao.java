package com.example.personal_finance_manager.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {

    @Insert
    void insert(UserEntity user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int countByEmail(String email);

    @Query("SELECT defaultIncome FROM users WHERE email = :userId LIMIT 1")
    LiveData<Double> getDefaultIncome(String userId);
    @Query("SELECT defaultIncome FROM users WHERE email = :userId")
    Double getDefaultIncomeRaw(String userId);
    @Query("UPDATE users SET defaultIncome = :amount WHERE email = :userId")
    void updateDefaultIncome(String userId, double amount);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    LiveData<UserEntity> getUserById(String email);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByIdRaw(String email);


    @Update
    void update(UserEntity user);



}
