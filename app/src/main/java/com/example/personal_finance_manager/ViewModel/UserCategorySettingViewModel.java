package com.example.personal_finance_manager.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.personal_finance_manager.Model.AppDatabase;
import com.example.personal_finance_manager.Model.UserCategorySetting;
import com.example.personal_finance_manager.Model.UserCategorySettingDao;

import java.util.concurrent.Executors;

public class UserCategorySettingViewModel extends AndroidViewModel {
    private final UserCategorySettingDao settingDao;

    public UserCategorySettingViewModel(@NonNull Application application) {
        super(application);
        settingDao = AppDatabase.getInstance(application).userCategorySettingDao();
    }

    public LiveData<Double> getLimit(String userId, int categoryId) {
        return settingDao.getLimit(userId, categoryId);
    }

    public void insertLimit(UserCategorySetting setting) {
        Executors.newSingleThreadExecutor().execute(() -> settingDao.insert(setting));
    }
}
