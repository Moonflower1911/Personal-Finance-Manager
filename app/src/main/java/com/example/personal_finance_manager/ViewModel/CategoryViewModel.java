package com.example.personal_finance_manager.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.personal_finance_manager.Model.AppDatabase;
import com.example.personal_finance_manager.Model.CategoryDao;
import com.example.personal_finance_manager.Model.CategoryEntity;
import com.example.personal_finance_manager.R;

import java.util.List;
import java.util.concurrent.Executors;

public class CategoryViewModel extends AndroidViewModel {
    private final CategoryDao categoryDao;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        categoryDao = AppDatabase.getInstance(application).categoryDao();
    }

    public LiveData<List<CategoryEntity>> getCategoriesForUser(String userId) {
        return categoryDao.getCategoriesForUser(userId);
    }

    public void insertCategory(CategoryEntity category) {
        Executors.newSingleThreadExecutor().execute(() -> categoryDao.insert(category));
    }

    // ✅ Add default categories if none exist
    public void insertDefaultCategoriesIfEmpty() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CategoryEntity> existing = categoryDao.getCategoriesForUserNow(null);
            if (existing == null || existing.isEmpty()) {
                categoryDao.insert(new CategoryEntity("Rent / Mortgage", R.drawable.ic_home, null));
                categoryDao.insert(new CategoryEntity("Water", R.drawable.ic_water, null));
                categoryDao.insert(new CategoryEntity("Electricity", R.drawable.ic_electricity, null));
                categoryDao.insert(new CategoryEntity("Gas", R.drawable.ic_gas, null));
                categoryDao.insert(new CategoryEntity("Internet", R.drawable.ic_wifi, null));
                categoryDao.insert(new CategoryEntity("Groceries", R.drawable.ic_groceries, null));
                categoryDao.insert(new CategoryEntity("Transport", R.drawable.ic_transport, null));
                categoryDao.insert(new CategoryEntity("Car Maintenance",R.drawable.ic_wrench,null));
                categoryDao.insert(new CategoryEntity("Food & Dining", R.drawable.ic_food, null));
                categoryDao.insert(new CategoryEntity("Entertainment", R.drawable.ic_game, null));
                categoryDao.insert(new CategoryEntity("Clothing", R.drawable.ic_clothing, null));
                categoryDao.insert(new CategoryEntity("Fitness", R.drawable.ic_dumbbell, null));
                // Add more if needed
            }
        });
}
    public void deleteCategory(CategoryEntity category) {
        Executors.newSingleThreadExecutor().execute(() -> categoryDao.delete(category));
    }

}
