package com.example.personal_finance_manager.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.personal_finance_manager.Model.AppDatabase;
import com.example.personal_finance_manager.Model.UserDao;
import com.example.personal_finance_manager.Model.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserViewModel extends AndroidViewModel {

    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserViewModel(@NonNull Application application) {
        super(application);
        userDao = AppDatabase.getInstance(application).userDao();
    }

    public LiveData<Double> getDefaultIncome(String userId) {
        return userDao.getDefaultIncome(userId);
    }

    public void updateDefaultIncome(String userId, double income) {
        executor.execute(() -> userDao.updateDefaultIncome(userId, income));
    }

    public void updateUserInfo(String email, String fullName, double defaultIncome) {
        Executors.newSingleThreadExecutor().execute(() -> {
            UserEntity user = userDao.getUserByIdRaw(email);
            if (user != null) {
                user.fullName = fullName;
                user.defaultIncome = defaultIncome;
                userDao.update(user);
            }
        });
    }



    public LiveData<UserEntity> getUserById(String userId) {
        return userDao.getUserById(userId);
    }

}
