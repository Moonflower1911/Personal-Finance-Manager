package com.example.personal_finance_manager.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.personal_finance_manager.Model.AppDatabase;
import com.example.personal_finance_manager.Model.UserDao;
import com.example.personal_finance_manager.Model.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginViewModel extends AndroidViewModel {

    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public void login(String email, String password) {
        executor.execute(() -> {
            UserEntity user = userDao.getUserByEmail(email);
            if (user == null) {
                errorMessage.postValue("This email is not registered. Please sign up first.");
            } else if (!password.equals(user.password)) {
                errorMessage.postValue("Incorrect password.");
            } else {
                loginSuccess.postValue(true);
            }
        });
    }

    public void checkGoogleUser(String email, GoogleUserCallback callback) {
        executor.execute(() -> {
            UserEntity user = userDao.getUserByEmail(email);
            callback.onCheckComplete(user != null);
        });
    }

    public interface GoogleUserCallback {
        void onCheckComplete(boolean exists);
    }
}
