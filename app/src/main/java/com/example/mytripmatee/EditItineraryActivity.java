package com.example.mytripmatee;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mytripmatee.databinding.ActivityItineraryEditBinding;

import java.util.Calendar;
import java.util.Locale;

public class EditItineraryActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, EditItineraryActivity.class);
    }

    private ActivityItineraryEditBinding binding;
    private String selectedType = "DI CHUYỂN";
    private String selectedDate = "";
    private String selectedStartTime = "09:00";
    private String selectedEndTime = "10:30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());

        binding = ActivityItineraryEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupListeners();
        selectCategoryType(binding.chipTypeMove, "DI CHUYỂN");
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        // Category Type Chips
        binding.chipTypeMove.setOnClickListener(v -> selectCategoryType(binding.chipTypeMove, "DI CHUYỂN"));
        binding.chipTypeFood.setOnClickListener(v -> selectCategoryType(binding.chipTypeFood, "ĂN UỐNG"));
        binding.chipTypeStay.setOnClickListener(v -> selectCategoryType(binding.chipTypeStay, "LƯU TRÚ"));
        binding.chipTypeSightseeing.setOnClickListener(v -> selectCategoryType(binding.chipTypeSightseeing, "CẢNH ĐẸP"));

        // Date Picker
        binding.cvDatePicker.setOnClickListener(v -> showDatePicker());

        // Time Pickers
        binding.cvStartTime.setOnClickListener(v -> showTimePicker(true));
        binding.cvEndTime.setOnClickListener(v -> showTimePicker(false));

        // Save buttons
        View.OnClickListener saveListener = v -> validateAndSave();
        binding.btnSaveTop.setOnClickListener(saveListener);
        binding.btnSaveBottom.setOnClickListener(saveListener);
    }

    private void selectCategoryType(TextView selectedChip, String typeName) {
        this.selectedType = typeName;

        // Reset chip styles
        resetChip(binding.chipTypeMove);
        resetChip(binding.chipTypeFood);
        resetChip(binding.chipTypeStay);
        resetChip(binding.chipTypeSightseeing);

        // Highlight selected chip
        selectedChip.setBackgroundResource(R.drawable.bg_pill_teal);
        selectedChip.setTextColor(Color.WHITE);
    }

    private void resetChip(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_pill_white);
        chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", d, m + 1, y);
            binding.tvSelectedDate.setText("Ngày diễn ra: " + selectedDate);
            binding.tvSelectedDate.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }, year, month, day);

        dialog.show();
    }

    private void showTimePicker(boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        int hour = isStart ? 9 : 10;
        int minute = isStart ? 0 : 30;

        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
            if (isStart) {
                selectedStartTime = formattedTime;
                binding.tvStartTime.setText(selectedStartTime);
            } else {
                selectedEndTime = formattedTime;
                binding.tvEndTime.setText(selectedEndTime);
            }
        }, hour, minute, true);

        dialog.show();
    }

    private void validateAndSave() {
        // Clear previous errors
        binding.tilTitle.setError(null);

        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";

        // Bắt lỗi form - 1: Tên hoạt động bắt buộc
        if (title.isEmpty()) {
            binding.tilTitle.setError("Vui lòng nhập tên hoạt động");
            binding.etTitle.requestFocus();
            return;
        }

        // Bắt lỗi form - 2: Kiểm tra ngày diễn ra
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày diễn ra cho hoạt động", Toast.LENGTH_SHORT).show();
            showDatePicker();
            return;
        }

        // Bắt lỗi form - 3: Kiểm tra link Google Maps (nếu có nhập)
        String mapUrl = binding.etMapUrl.getText() != null ? binding.etMapUrl.getText().toString().trim() : "";
        if (!mapUrl.isEmpty() && !mapUrl.startsWith("http://") && !mapUrl.startsWith("https://") && !mapUrl.contains("maps")) {
            Toast.makeText(this, "Cảnh báo: Link Google Maps chưa đúng định dạng URL", Toast.LENGTH_SHORT).show();
        }

        // Form hợp lệ -> Lưu thành công
        Toast.makeText(this, "Lưu hoạt động vào lịch trình thành công ✓", Toast.LENGTH_SHORT).show();
        finish();
    }
}
