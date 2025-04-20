package com.example.personal_finance_manager.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    @NonNull
    public String email;

    public String fullName;
    public String password;

    public UserEntity(String fullName, @NonNull String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }
}

