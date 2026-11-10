package com.example.mytripmatee;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CustomDatePickerActivity extends AppCompatActivity {
    private RecyclerView rvCalendarDays;
    private TextView tvMonthYear, tvSelectedRange;
    private CalendarAdapter adapter;
    private Integer selectedStartDate = 12;
    private Integer selectedEndDate = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_picker);

        initViews();
        setupCalendar();
        setupListeners();
        updateSummaryText();
    }

    private void initViews() {
        rvCalendarDays = findViewById(R.id.rvCalendarDays);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvSelectedRange = findViewById(R.id.tvSelectedRange);
    }

    private void setupCalendar() {
        rvCalendarDays.setLayoutManager(new GridLayoutManager(this, 7));

        List<CalendarAdapter.DayItem> daysList = new ArrayList<>();
        // Ngày cuối tháng trước (26 - 31 Tháng 10)
        for (int d = 26; d <= 31; d++) {
            daysList.add(new CalendarAdapter.DayItem(d, false));
        }
        // Ngày trong tháng 11 (1 - 30)
        for (int d = 1; d <= 30; d++) {
            daysList.add(new CalendarAdapter.DayItem(d, true));
        }

        adapter = new CalendarAdapter(daysList, this::onDayClick);
        adapter.setSelectedRange(selectedStartDate, selectedEndDate);
        rvCalendarDays.setAdapter(adapter);
    }

    private void setupListeners() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        TextView btnReset = findViewById(R.id.btnReset);
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                selectedStartDate = null;
                selectedEndDate = null;
                if (adapter != null) {
                    adapter.setSelectedRange(null, null);
                }
                updateSummaryText();
            });
        }
    }

    private void onDayClick(int dayClicked) {
        if (selectedStartDate == null) {
            selectedStartDate = dayClicked;
            selectedEndDate = null;
        } else if (selectedEndDate == null && dayClicked > selectedStartDate) {
            selectedEndDate = dayClicked;
        } else {
            selectedStartDate = dayClicked;
            selectedEndDate = null;
        }

        if (adapter != null) {
            adapter.setSelectedRange(selectedStartDate, selectedEndDate);
        }
        updateSummaryText();
    }

    private void updateSummaryText() {
        if (tvSelectedRange == null) return;
        if (selectedStartDate != null && selectedEndDate != null) {
            int duration = (selectedEndDate - selectedStartDate) + 1;
            int nights = duration - 1;
            tvSelectedRange.setText("Thời gian đã chọn:\n" + selectedStartDate + " Th11 → " + selectedEndDate + " Th11, 2026 (" + duration + "N" + nights + "Đ)");
        } else if (selectedStartDate != null) {
            tvSelectedRange.setText("Đang chọn ngày về (từ ngày " + selectedStartDate + " Th11)...");
        } else {
            tvSelectedRange.setText("Chưa chọn ngày");
        }
    }
}
