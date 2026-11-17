package com.example.mytripmatee.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;
import com.example.mytripmatee.adapter.AdapterLich;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChonNgayActivity extends AppCompatActivity {

    public static final String EXTRA_RESULT_DATE_RANGE = "extra_result_date_range";
    public static final String EXTRA_RESULT_DURATION = "extra_result_duration";

    private RecyclerView rvCalendarDays;
    private TextView tvMonthYear, tvSelectedRange;
    private View btnApplyDate, btnReset, btnPreset3N2D, btnPresetWeekend;
    private ImageButton btnBack, btnPrevMonth, btnNextMonth;
    private AdapterLich adapter;
    private Integer selectedStartDate = 12;
    private Integer selectedEndDate = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chon_ngay);

        if (getIntent() != null) {
            String incomingDi = getIntent().getStringExtra("ngayDi");
            String incomingVe = getIntent().getStringExtra("ngayVe");
            if (incomingDi != null && incomingDi.contains("-")) {
                try {
                    String[] parts = incomingDi.split("-");
                    if (parts.length == 3) {
                        selectedStartDate = Integer.parseInt(parts[2]);
                    }
                } catch (Exception ignored) {}
            }
            if (incomingVe != null && incomingVe.contains("-")) {
                try {
                    String[] parts = incomingVe.split("-");
                    if (parts.length == 3) {
                        selectedEndDate = Integer.parseInt(parts[2]);
                    }
                } catch (Exception ignored) {}
            }
        }

        initViews();
        setupCalendar();
        setupListeners();
        updateSummaryText();
    }

    private void initViews() {
        rvCalendarDays = findViewById(R.id.rvCalendarDays);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvSelectedRange = findViewById(R.id.tvSelectedRange);
        btnApplyDate = findViewById(R.id.btnApplyDate);
        btnReset = findViewById(R.id.btnReset);
        btnBack = findViewById(R.id.btnBack);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnPreset3N2D = findViewById(R.id.btnPreset3N2D);
        btnPresetWeekend = findViewById(R.id.btnPresetWeekend);
    }

    private void setupCalendar() {
        rvCalendarDays.setLayoutManager(new GridLayoutManager(this, 7));

        List<AdapterLich.ItemNgayLich> daysList = new ArrayList<>();
        for (int d = 26; d <= 31; d++) {
            daysList.add(new AdapterLich.ItemNgayLich(d, false));
        }
        for (int d = 1; d <= 30; d++) {
            daysList.add(new AdapterLich.ItemNgayLich(d, true));
        }

        adapter = new AdapterLich(daysList, this::onDayClick);
        adapter.setKhoangNgayChon(selectedStartDate, selectedEndDate);
        rvCalendarDays.setAdapter(adapter);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                selectedStartDate = null;
                selectedEndDate = null;
                if (adapter != null) {
                    adapter.setKhoangNgayChon(null, null);
                }
                updateSummaryText();
            });
        }

        if (btnPreset3N2D != null) {
            btnPreset3N2D.setOnClickListener(v -> {
                selectedStartDate = 12;
                selectedEndDate = 15;
                if (adapter != null) {
                    adapter.setKhoangNgayChon(selectedStartDate, selectedEndDate);
                }
                updateSummaryText();
            });
        }

        if (btnPresetWeekend != null) {
            btnPresetWeekend.setOnClickListener(v -> {
                selectedStartDate = 20;
                selectedEndDate = 22;
                if (adapter != null) {
                    adapter.setKhoangNgayChon(selectedStartDate, selectedEndDate);
                }
                updateSummaryText();
            });
        }

        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v ->
                    Toast.makeText(this, "Xem lịch Tháng 10, 2026", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v ->
                    Toast.makeText(this, "Xem lịch Tháng 12, 2026", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnApplyDate != null) {
            btnApplyDate.setOnClickListener(v -> applyAndFinish());
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
            adapter.setKhoangNgayChon(selectedStartDate, selectedEndDate);
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

    private void applyAndFinish() {
        String dateRange;
        String durationText;
        String ngayDiDb;
        String ngayVeDb;

        int month = 11;
        int year = 2026;

        if (selectedStartDate != null && selectedEndDate != null) {
            int duration = (selectedEndDate - selectedStartDate) + 1;
            int nights = Math.max(0, duration - 1);
            dateRange = selectedStartDate + "/11/2026 – " + selectedEndDate + "/11/2026";
            durationText = duration + " Ngày " + nights + " Đêm";
            ngayDiDb = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, selectedStartDate);
            ngayVeDb = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, selectedEndDate);
        } else if (selectedStartDate != null) {
            dateRange = selectedStartDate + "/11/2026";
            durationText = "1 Ngày 0 Đêm";
            ngayDiDb = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, selectedStartDate);
            ngayVeDb = ngayDiDb;
        } else {
            dateRange = "12/11/2026 – 15/11/2026";
            durationText = "4 Ngày 3 Đêm";
            ngayDiDb = "2026-11-12";
            ngayVeDb = "2026-11-15";
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_RESULT_DATE_RANGE, dateRange);
        resultIntent.putExtra(EXTRA_RESULT_DURATION, durationText);
        resultIntent.putExtra("extra_ngay_di", ngayDiDb);
        resultIntent.putExtra("extra_ngay_ve", ngayVeDb);

        setResult(RESULT_OK, resultIntent);
        Toast.makeText(this, "Đã chọn: " + dateRange + " (" + durationText + ")", Toast.LENGTH_SHORT).show();
        finish();
    }
}
