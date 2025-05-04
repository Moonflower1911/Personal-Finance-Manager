// IncomeViewModel.java
package com.example.personal_finance_manager.ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.personal_finance_manager.Model.AppDatabase;
import com.example.personal_finance_manager.Model.IncomeDao;
import com.example.personal_finance_manager.Model.IncomeEntity;
import com.example.personal_finance_manager.Model.UserDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IncomeViewModel extends AndroidViewModel {

    private final IncomeDao incomeDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final UserDao userDao;

    public IncomeViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        incomeDao = db.incomeDao();
        userDao = db.userDao();
    }


    public LiveData<IncomeEntity> getIncome(String userId, String month) {
        return incomeDao.getIncomeForUserMonth(userId, month);
    }

    public void insertIncome(IncomeEntity income) {
        Executors.newSingleThreadExecutor().execute(() -> incomeDao.insertIncome(income));
    }

    public void updateIncome(IncomeEntity income) {
        Executors.newSingleThreadExecutor().execute(() -> incomeDao.updateIncome(income));
    }

    public void setIncomeForMonth(String userId, String month, double amount) {
        executor.execute(() -> {
            IncomeEntity existing = incomeDao.getIncomeRaw(userId, month);
            if (existing != null) {
                existing.incomeAmount = amount;
                incomeDao.updateIncome(existing);
            } else {
                incomeDao.insertIncome(new IncomeEntity(userId, month, amount));
            }
        });
    }
    public void ensureIncomeForMonth(String userId, String month) {
        executor.execute(() -> {
            IncomeEntity existing = incomeDao.getIncomeRaw(userId, month);
            if (existing == null) {
                Double defaultIncome = userDao.getDefaultIncomeRaw(userId);
                double amountToInsert = (defaultIncome != null) ? defaultIncome : 0.0;
                incomeDao.insertIncome(new IncomeEntity(userId, month, amountToInsert));
            }
        });
    }



}
