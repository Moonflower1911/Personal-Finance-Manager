package com.example.personal_finance_manager.ViewModel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SplashViewModel extends ViewModel {

    private final MutableLiveData<Boolean> showBottomPanel = new MutableLiveData<>(false);

    public SplashViewModel() {
        // Simulate splash delay (1 second)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showBottomPanel.setValue(true);
        }, 1000);
    }

    public LiveData<Boolean> getShowBottomPanel() {
        return showBottomPanel;
    }
}

