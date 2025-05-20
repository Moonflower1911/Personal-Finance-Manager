package com.example.personal_finance_manager.Model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                UserEntity.class,
                CategoryEntity.class,
                ExpenseEntity.class,
                IncomeEntity.class,
                UserCategorySetting.class
        },
        version = 2
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;


    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public abstract ExpenseDao expenseDao();
    public abstract UserCategorySettingDao userCategorySettingDao();

    public abstract IncomeDao incomeDao();

    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "pfm_db")
                            .fallbackToDestructiveMigration()
                            .addCallback(prepopulateCallback) // ✅ Add this line
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Callback prepopulateCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // Insert historical monthly limits for userId 'demo_user'
            db.execSQL("INSERT INTO user_category_settings (userId, categoryId, month, monthlyLimit) VALUES " +
                    "('test1@email.com', 1, '2025-01', 200.0), " +
                    "('test1@email.com', 1, '2025-02', 180.0), " +
                    "('test1@email.com', 1, '2025-03', 220.0), " +
                    "('test1@email.com', 2, '2025-01', 100.0), " +
                    "('test1@email.com', 2, '2025-02', 110.0)");
        }
    };

}
