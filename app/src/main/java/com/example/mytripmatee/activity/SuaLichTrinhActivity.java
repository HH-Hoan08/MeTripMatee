package com.example.mytripmatee.activity;

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

import com.example.mytripmatee.R;
import com.example.mytripmatee.dao.LichTrinhDAO;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.databinding.ActivitySuaLichTrinhBinding;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.model.LichTrinh;
import com.example.mytripmatee.util.TienIchNgay;

import java.util.Calendar;
import java.util.Locale;

public class SuaLichTrinhActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, SuaLichTrinhActivity.class);
    }

    private ActivitySuaLichTrinhBinding binding;
    private LichTrinhDAO lichTrinhDAO;
    private ChuyenDiDAO chuyenDiDAO;
    private LichTrinh existingItem;

    private int maChuyenDi = 1;
    private int maHoatDong = -1;

    private String selectedType = "DI CHUYỂN";
    private String selectedDateDb = "2026-12-01";
    private String selectedStartTime = "09:00";
    private String selectedEndTime = "10:30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());

        binding = ActivitySuaLichTrinhBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        lichTrinhDAO = new LichTrinhDAO(this);
        chuyenDiDAO = new ChuyenDiDAO(this);

        if (getIntent() != null) {
            maChuyenDi = getIntent().getIntExtra("maChuyenDi", 1);
            maHoatDong = getIntent().getIntExtra("maHoatDong", -1);
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupListeners();

        if (maHoatDong > 0) {
            loadExistingData();
        } else {
            selectCategoryType(binding.chipTypeMove, "DI CHUYỂN");
            initNewActivityDate();
        }
    }

    private void initNewActivityDate() {
        ChuyenDi cd = chuyenDiDAO.layChuyenDiTheoId(maChuyenDi);
        if (cd != null && cd.getNgayDi() != null && !cd.getNgayDi().isEmpty()) {
            selectedDateDb = cd.getNgayDi();
            String displayDate = TienIchNgay.dinhDangHienThi(selectedDateDb);
            binding.tvSelectedDate.setText("Ngày diễn ra: " + displayDate);
            binding.tvSelectedDate.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }
    }

    private void loadExistingData() {
        existingItem = lichTrinhDAO.layHoatDongTheoId(maHoatDong);
        if (existingItem == null) {
            selectCategoryType(binding.chipTypeMove, "DI CHUYỂN");
            return;
        }

        binding.tvHeaderTitle.setText("Chỉnh sửa hoạt động");
        binding.btnDelete.setVisibility(View.VISIBLE);

        if (existingItem.getTieuDe() != null) {
            binding.etTitle.setText(existingItem.getTieuDe());
        }
        if (existingItem.getDiaDiem() != null) {
            binding.etLocation.setText(existingItem.getDiaDiem());
        }
        if (existingItem.getLinkBanDo() != null) {
            binding.etMapUrl.setText(existingItem.getLinkBanDo());
        }
        if (existingItem.getGhiChu() != null) {
            binding.etNotes.setText(existingItem.getGhiChu());
        }

        if (existingItem.getNgayHoatDong() != null && !existingItem.getNgayHoatDong().isEmpty()) {
            selectedDateDb = existingItem.getNgayHoatDong();
            try {
                String[] parts = selectedDateDb.split("-");
                if (parts.length == 3) {
                    String displayDate = String.format(Locale.getDefault(), "%s/%s/%s", parts[2], parts[1], parts[0]);
                    binding.tvSelectedDate.setText("Ngày diễn ra: " + displayDate);
                    binding.tvSelectedDate.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                }
            } catch (Exception e) {
                binding.tvSelectedDate.setText("Ngày diễn ra: " + selectedDateDb);
            }
        }

        if (existingItem.getGioBatDau() != null && !existingItem.getGioBatDau().isEmpty()) {
            selectedStartTime = existingItem.getGioBatDau();
            binding.tvStartTime.setText(selectedStartTime);
        }

        LichTrinh.LoaiHoatDong loai = existingItem.getLoaiHoatDong();
        if (loai == LichTrinh.LoaiHoatDong.AN_UONG) {
            selectCategoryType(binding.chipTypeFood, "ĂN UỐNG");
        } else if (loai == LichTrinh.LoaiHoatDong.LUU_TRU) {
            selectCategoryType(binding.chipTypeStay, "LƯU TRÚ");
        } else if (loai == LichTrinh.LoaiHoatDong.THAM_QUAN) {
            selectCategoryType(binding.chipTypeSightseeing, "CẢNH ĐẸP");
        } else {
            selectCategoryType(binding.chipTypeMove, "DI CHUYỂN");
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.chipTypeMove.setOnClickListener(v -> selectCategoryType(binding.chipTypeMove, "DI CHUYỂN"));
        binding.chipTypeFood.setOnClickListener(v -> selectCategoryType(binding.chipTypeFood, "ĂN UỐNG"));
        binding.chipTypeStay.setOnClickListener(v -> selectCategoryType(binding.chipTypeStay, "LƯU TRÚ"));
        binding.chipTypeSightseeing.setOnClickListener(v -> selectCategoryType(binding.chipTypeSightseeing, "CẢNH ĐẸP"));

        binding.cvDatePicker.setOnClickListener(v -> showDatePicker());

        binding.cvStartTime.setOnClickListener(v -> showTimePicker(true));
        binding.cvEndTime.setOnClickListener(v -> showTimePicker(false));

        View.OnClickListener saveListener = v -> validateAndSave();
        binding.btnSaveTop.setOnClickListener(saveListener);
        binding.btnSaveBottom.setOnClickListener(saveListener);

        binding.btnDelete.setOnClickListener(v -> confirmAndDeleteItem());
    }

    private void confirmAndDeleteItem() {
        if (maHoatDong <= 0) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa hoạt động")
                .setMessage("Bạn có chắc chắn muốn xóa hoạt động này khỏi lịch trình?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean success = lichTrinhDAO.xoaHoatDong(maHoatDong);
                    if (success) {
                        Toast.makeText(this, "Đã xóa hoạt động ✓", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Lỗi khi xóa hoạt động", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void selectCategoryType(TextView selectedChip, String typeName) {
        this.selectedType = typeName;

        resetChip(binding.chipTypeMove);
        resetChip(binding.chipTypeFood);
        resetChip(binding.chipTypeStay);
        resetChip(binding.chipTypeSightseeing);

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
            selectedDateDb = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
            String displayDate = String.format(Locale.getDefault(), "%02d/%02d/%d", d, m + 1, y);
            binding.tvSelectedDate.setText("Ngày diễn ra: " + displayDate);
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
        binding.tilTitle.setError(null);

        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";

        if (title.isEmpty()) {
            binding.tilTitle.setError("Vui lòng nhập tên hoạt động");
            binding.etTitle.requestFocus();
            return;
        }

        if (selectedDateDb == null || selectedDateDb.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày diễn ra cho hoạt động", Toast.LENGTH_SHORT).show();
            showDatePicker();
            return;
        }

        String location = binding.etLocation.getText() != null ? binding.etLocation.getText().toString().trim() : "";
        String mapUrl = binding.etMapUrl.getText() != null ? binding.etMapUrl.getText().toString().trim() : "";
        String notes = binding.etNotes.getText() != null ? binding.etNotes.getText().toString().trim() : "";

        LichTrinh.LoaiHoatDong loai;
        switch (selectedType) {
            case "ĂN UỐNG":
                loai = LichTrinh.LoaiHoatDong.AN_UONG;
                break;
            case "LƯU TRÚ":
                loai = LichTrinh.LoaiHoatDong.LUU_TRU;
                break;
            case "CẢNH ĐẸP":
                loai = LichTrinh.LoaiHoatDong.THAM_QUAN;
                break;
            case "DI CHUYỂN":
            default:
                loai = LichTrinh.LoaiHoatDong.DI_CHUYEN;
                break;
        }

        boolean daDi = (existingItem != null) ? existingItem.isDaDi() : false;
        LichTrinh item = new LichTrinh(maChuyenDi, selectedDateDb, selectedStartTime, title, location, mapUrl, loai, notes, daDi);

        if (maHoatDong > 0) {
            item.setMaHoatDong(maHoatDong);
            boolean success = lichTrinhDAO.capNhatHoatDong(item);
            if (success) {
                Toast.makeText(this, "Cập nhật hoạt động thành công ✓", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật (kiểm tra lại ngày nằm trong chuyến đi)", Toast.LENGTH_LONG).show();
            }
        } else {
            long newId = lichTrinhDAO.themHoatDong(item);
            if (newId > 0) {
                Toast.makeText(this, "Thêm hoạt động vào lịch trình thành công ✓", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi thêm hoạt động (ngày phải nằm trong ngày đi - ngày về của chuyến đi)", Toast.LENGTH_LONG).show();
            }
        }
    }
}


