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

public class SignUpViewModel extends AndroidViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();

    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SignUpViewModel(@NonNull Application application) {
        super(application);
        userDao = AppDatabase.getInstance(application).userDao();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getRegistrationSuccess() {
        return registrationSuccess;
    }

    public void registerUser(String fullName, String email, String password, String confirmPassword) {
        if (fullName == null || fullName.isEmpty() ||
                email == null || email.isEmpty() ||
                password == null || password.isEmpty() ||
                confirmPassword == null || confirmPassword.isEmpty()) {
            errorMessage.setValue("Please fill all fields.");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Invalid email format.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorMessage.setValue("Passwords do not match.");
            return;
        }

        if (password.length() < 6) {
            errorMessage.setValue("Password must be at least 6 characters.");
            return;
        }

        executor.execute(() -> {
            int count = userDao.countByEmail(email);
            if (count > 0) {
                errorMessage.postValue("Email already exists. Please log in instead.");
            } else {
                userDao.insert(new UserEntity(fullName, email, password));
                registrationSuccess.postValue(true);
            }
        });
    }
}
