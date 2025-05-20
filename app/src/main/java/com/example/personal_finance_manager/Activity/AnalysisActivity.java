package com.example.personal_finance_manager.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;

import com.example.personal_finance_manager.R;
import com.example.personal_finance_manager.ViewModel.CategoryViewModel;
import com.example.personal_finance_manager.ViewModel.ExpenseViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AnalysisActivity extends BaseActivity {

    private PieChart pieChart;
    private ExpenseViewModel expenseVM;
    private CategoryViewModel categoryVM;
    private TextView tvCurrentMonth;
    private ImageButton btnPrevMonth, btnNextMonth;
    private FloatingActionButton fab;
    private String userId;
    private String currentMonth;
    private DateTimeFormatter monthYearFormatter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        userId = getIntent().getStringExtra("userId");
        setupBottomNavBar(userId);

        // Set the Analysis icon to active
        ImageView analysisIcon = findViewById(R.id.iconAnalysis);
        if (analysisIcon != null) analysisIcon.setAlpha(1f);

        // Initialize month navigation
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        btnPrevMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        // Initialize chart
        pieChart = findViewById(R.id.pieChart);
        configurePieChart();

        // Initialize ViewModels
        expenseVM = new ViewModelProvider(this).get(ExpenseViewModel.class);
        categoryVM = new ViewModelProvider(this).get(CategoryViewModel.class);

        // Initialize date formatters and current month
        monthYearFormatter = DateTimeFormatter.ofPattern("MMMM - yyyy");

        // Set initial month
        currentMonth = getIntent().getStringExtra("month");
        if (currentMonth == null) {
            currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        updateMonthLabel();

        // Set click listeners for month navigation
        btnPrevMonth.setOnClickListener(v -> {
            currentMonth = LocalDate.parse(currentMonth + "-01")
                    .minusMonths(1)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));
            updateMonthLabel();
            pieChart.clear();
            loadChartData();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth = LocalDate.parse(currentMonth + "-01")
                    .plusMonths(1)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));
            updateMonthLabel();
            pieChart.clear();
            loadChartData();
        });
        fab = findViewById(R.id.fabAddCategory);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(AnalysisActivity.this, CategoryActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        // Load initial chart data
        loadChartData();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateMonthLabel() {
        // Parse the yyyy-MM format to get a readable month name
        LocalDate date = LocalDate.parse(currentMonth + "-01");
        String displayMonth = date.format(monthYearFormatter);
        tvCurrentMonth.setText(displayMonth);
    }

    /** Configure pie chart appearance */
    private void configurePieChart() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setRotationEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(14f);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);

        // Configure legend
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(12f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
    }

    /** Load expenses by category and draw the chart */
    private void loadChartData() {
        categoryVM.getCategoriesForUser(userId).observe(this, categories -> {
            List<PieEntry> entries = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();
            final int[] pending = {categories.size()};   // counter

            categories.forEach(cat ->
                    expenseVM.getTotalExpensesForCategory(userId, cat.id, currentMonth)
                            .observe(this, spent -> {
                                pending[0]--;
                                double amount = (spent != null) ? spent : 0.0;
                                if (amount > 0.01) {
                                    entries.add(new PieEntry((float) amount, cat.name));
                                    colors.add(getColorFor(cat.id));
                                }
                                if (pending[0] == 0) drawPie(entries, colors);
                            })
            );
        });
    }

    private void drawPie(List<PieEntry> entries, List<Integer> colors) {
        // If no data, show empty state
        if (entries.isEmpty()) {
            pieChart.setData(null);
            pieChart.invalidate();
            pieChart.setCenterText("No expenses");
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setDrawEntryLabels(false);
        PieData data = new PieData(dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        pieChart.setData(data);
        pieChart.invalidate();       // refresh
    }

    /** Simple color palette */
    private int getColorFor(int id) {
        String[] palette = {
                "#FF7043", "#FFA726", "#FFEE58", "#66BB6A",
                "#26C6DA", "#42A5F5", "#7E57C2", "#EC407A",
                "#8D6E63", "#78909C"
        };
        return Color.parseColor(palette[id % palette.length]);
    }
}
