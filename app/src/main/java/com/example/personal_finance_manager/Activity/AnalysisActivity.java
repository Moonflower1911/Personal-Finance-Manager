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
import com.example.personal_finance_manager.ViewModel.IncomeViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AnalysisActivity extends BaseActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private ExpenseViewModel expenseVM;
    private CategoryViewModel categoryVM;
    private IncomeViewModel incomeVM;
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

        // Initialize ViewModels
        expenseVM = new ViewModelProvider(this).get(ExpenseViewModel.class);
        categoryVM = new ViewModelProvider(this).get(CategoryViewModel.class);
        incomeVM = new ViewModelProvider(this).get(IncomeViewModel.class);

        userId = getIntent().getStringExtra("userId");
        setupBottomNavBar(userId);

        // Set the Analysis icon to active
        ImageView analysisIcon = findViewById(R.id.iconAnalysis);
        if (analysisIcon != null) analysisIcon.setAlpha(1f);

        // Initialize month navigation
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        btnPrevMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        // Initialize charts
        pieChart = findViewById(R.id.pieChart);
        configurePieChart();

        barChart = findViewById(R.id.barChart);
        configureBarChart();

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
        loadBarChartData();
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

    /** Configure bar chart appearance */
    private void configureBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);

        // X-axis configuration
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);

        // Y-axis configuration
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setSpaceTop(30f); // Give some space at the top for the values

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Legend configuration
        Legend barLegend = barChart.getLegend();
        barLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        barLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        barLegend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        barLegend.setDrawInside(false);
        barLegend.setForm(Legend.LegendForm.SQUARE);
        barLegend.setFormSize(9f);
        barLegend.setTextSize(11f);
        barLegend.setXEntrySpace(4f);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadBarChartData() {
        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        YearMonth current = YearMonth.now();

        // We need to track when all async operations are complete
        final AtomicInteger pendingOperations = new AtomicInteger(12); // 6 months * 2 data points per month

        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = current.minusMonths(i);
            String formattedMonth = targetMonth.toString(); // yyyy-MM
            String label = targetMonth.getMonth().toString().substring(0, 3); // JAN, FEB...
            labels.add(label);

            final int index = 5 - i;

            // Load expense data
            expenseVM.getTotalExpensesForUserMonth(userId, formattedMonth).observe(this, total -> {
                float amount = (total != null) ? total.floatValue() : 0f;
                expenseEntries.add(new BarEntry(index, amount));

                if (pendingOperations.decrementAndGet() == 0) {
                    drawBarChart(incomeEntries, expenseEntries, labels);
                }
            });

            // Load income data
            incomeVM.getIncome(userId, formattedMonth).observe(this, income -> {
                float amount = (income != null) ? (float) income.incomeAmount : 0f;
                incomeEntries.add(new BarEntry(index, amount));

                if (pendingOperations.decrementAndGet() == 0) {
                    drawBarChart(incomeEntries, expenseEntries, labels);
                }
            });
        }
    }

    private void drawBarChart(List<BarEntry> incomeEntries, List<BarEntry> expenseEntries, List<String> labels) {
        // Sort entries by X position (month index)
        incomeEntries.sort((e1, e2) -> Float.compare(e1.getX(), e2.getX()));
        expenseEntries.sort((e1, e2) -> Float.compare(e1.getX(), e2.getX()));

        // Create income dataset
        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Income");
        incomeDataSet.setColor(Color.parseColor("#66BB6A"));
        incomeDataSet.setValueTextColor(Color.BLACK);
        incomeDataSet.setValueTextSize(10f);

        // Create expense dataset
        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Expenses");
        expenseDataSet.setColor(Color.parseColor("#831B19"));
        expenseDataSet.setValueTextColor(Color.BLACK);
        expenseDataSet.setValueTextSize(10f);

        // Combine datasets
        List<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(incomeDataSet);
        dataSets.add(expenseDataSet);

        // Create bar data
        BarData barData = new BarData(dataSets);
        barData.setBarWidth(0.3f); // Set bars width

        // Set X-axis labels
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        // Group bars
        float groupSpace = 0.4f;
        float barSpace = 0f;
        barData.setBarWidth(0.3f);
        barChart.setData(barData);
        barChart.groupBars(0f, groupSpace, barSpace);

        // Refresh chart
        barChart.invalidate();
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