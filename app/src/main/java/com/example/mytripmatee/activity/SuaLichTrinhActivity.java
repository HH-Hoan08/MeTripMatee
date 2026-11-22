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
import com.example.mytripmatee.dao.ChiPhiDAO;
import com.example.mytripmatee.dao.LichTrinhDAO;
import com.example.mytripmatee.dao.ThongBaoDAO;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.database.NguoiDungDAO;
import com.example.mytripmatee.databinding.ActivitySuaLichTrinhBinding;
import com.example.mytripmatee.model.ChiPhi;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.model.LichTrinh;
import com.example.mytripmatee.model.NguoiDung;
import com.example.mytripmatee.util.QuanLyPhien;
import com.example.mytripmatee.util.TienIchNgay;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SuaLichTrinhActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, SuaLichTrinhActivity.class);
    }

    private ActivitySuaLichTrinhBinding binding;
    private LichTrinhDAO lichTrinhDAO;
    private ChuyenDiDAO chuyenDiDAO;
    private ChiPhiDAO chiPhiDAO;
    private LichTrinh existingItem;
    private ChiPhi existingChiPhi;

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
        chiPhiDAO = new ChiPhiDAO(this);

        if (getIntent() != null) {
            maChuyenDi = getIntent().getIntExtra("maChuyenDi", 1);
            maHoatDong = getIntent().getIntExtra("maHoatDong", -1);
        }

        com.example.mytripmatee.util.QuanLyPhien quanLyPhien = new com.example.mytripmatee.util.QuanLyPhien(this);
        int currentUserId = quanLyPhien.getMaNguoiDung();
        if (currentUserId <= 0) currentUserId = 1;

        ChuyenDi cd = chuyenDiDAO.layChuyenDiTheoId(maChuyenDi);
        if (cd != null && cd.getMaNguoiDung() != currentUserId) {
            Toast.makeText(this, "Bạn là thành viên được mời. Chỉ Trưởng nhóm (người tạo chuyến đi) mới có quyền thêm/chỉnh sửa/xóa lịch trình!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(imeInsets.left, imeInsets.top, imeInsets.right, imeInsets.bottom);
            return insets;
        });

        setupListeners();
        setupAutoScrollOnFocus(binding.svForm, binding.etTitle, binding.etLocation, binding.etCost, binding.etNotes);

        if (maHoatDong > 0) {
            loadExistingData();
        } else {
            selectCategoryType(binding.chipTypeMove, "DI CHUYỂN");
            initNewActivityDate();
        }
    }

    private void setupAutoScrollOnFocus(androidx.core.widget.NestedScrollView scrollView, View... views) {
        if (scrollView == null) return;
        for (View v : views) {
            if (v != null) {
                v.setOnFocusChangeListener((view, hasFocus) -> {
                    if (hasFocus) {
                        scrollView.postDelayed(() -> {
                            int[] loc = new int[2];
                            view.getLocationOnScreen(loc);
                            scrollView.smoothScrollBy(0, loc[1] - 250);
                        }, 200);
                    }
                });
            }
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

        List<ChiPhi> listCp = chiPhiDAO.layDanhSachChiPhiTheoChuyenDi(existingItem.getMaChuyenDi());
        if (listCp != null) {
            for (ChiPhi cp : listCp) {
                if (existingItem.getNgayHoatDong() != null && existingItem.getNgayHoatDong().equals(cp.getNgayChi()) &&
                        cp.getGhiChu() != null &&
                        (cp.getGhiChu().equalsIgnoreCase(existingItem.getTieuDe()) || cp.getGhiChu().contains(existingItem.getTieuDe()))) {
                    existingChiPhi = cp;
                    break;
                }
            }
        }
        if (existingChiPhi != null && existingChiPhi.getSoTien() > 0) {
            binding.etCost.setText(String.valueOf(existingChiPhi.getSoTien()));
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
                        if (existingChiPhi != null) {
                            chiPhiDAO.xoaChiPhi(existingChiPhi.getMaChiPhi());
                        }
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

        String costStr = binding.etCost.getText() != null ? binding.etCost.getText().toString().trim() : "";
        long costAmount = 0;
        if (!costStr.isEmpty()) {
            try {
                costAmount = Long.parseLong(costStr.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ignored) {}
        }

        if (!mapUrl.isEmpty() && !mapUrl.startsWith("http://") && !mapUrl.startsWith("https://") && !mapUrl.contains("maps")) {
            Toast.makeText(this, "Cảnh báo: Link Google Maps chưa đúng định dạng URL", Toast.LENGTH_SHORT).show();
        }

        LichTrinh.LoaiHoatDong loai;
        int maDanhMuc;
        switch (selectedType) {
            case "ĂN UỐNG":
                loai = LichTrinh.LoaiHoatDong.AN_UONG;
                maDanhMuc = 1;
                break;
            case "LƯU TRÚ":
                loai = LichTrinh.LoaiHoatDong.LUU_TRU;
                maDanhMuc = 3;
                break;
            case "CẢNH ĐẸP":
                loai = LichTrinh.LoaiHoatDong.THAM_QUAN;
                maDanhMuc = 4;
                break;
            case "DI CHUYỂN":
            default:
                loai = LichTrinh.LoaiHoatDong.DI_CHUYEN;
                maDanhMuc = 2;
                break;
        }

        boolean daDi = (existingItem != null) ? existingItem.isDaDi() : false;
        LichTrinh item = new LichTrinh(maChuyenDi, selectedDateDb, selectedStartTime, title, location, mapUrl, loai, notes, daDi);

        boolean isSuccess;
        if (maHoatDong > 0) {
            item.setMaHoatDong(maHoatDong);
            isSuccess = lichTrinhDAO.capNhatHoatDong(item);
        } else {
            long newId = lichTrinhDAO.themHoatDong(item);
            isSuccess = (newId > 0);
        }

        if (isSuccess) {
            if (costAmount > 0) {
                if (existingChiPhi != null) {
                    existingChiPhi.setMaDanhMuc(maDanhMuc);
                    existingChiPhi.setSoTien(costAmount);
                    existingChiPhi.setNgayChi(selectedDateDb);
                    existingChiPhi.setGhiChu(title);
                    existingChiPhi.setLaCaNhan(0);
                    chiPhiDAO.capNhatChiPhi(existingChiPhi);
                } else {
                    ChiPhi newCp = new ChiPhi(maChuyenDi, maDanhMuc, costAmount, selectedDateDb, title, 0);
                    chiPhiDAO.themChiPhi(newCp);
                }
            } else if (existingChiPhi != null) {
                chiPhiDAO.xoaChiPhi(existingChiPhi.getMaChiPhi());
            }

            // Send notification to trip members
            try {
                QuanLyPhien qlp = new QuanLyPhien(this);
                int curUserId = qlp.getMaNguoiDung();
                NguoiDungDAO ndDao = new NguoiDungDAO(this);
                NguoiDung curU = ndDao.layTheoId(curUserId);
                String senderName = curU != null ? (curU.getHoTen() != null && !curU.getHoTen().isEmpty() ? curU.getHoTen() : curU.getTenDangNhap()) : "Một thành viên";

                ChuyenDi cd = chuyenDiDAO.layChuyenDiTheoId(maChuyenDi);
                String tripName = cd != null ? cd.getTenChuyenDi() : "Chuyến đi";

                List<Integer> memberIds = chuyenDiDAO.layDanhSachIdThanhVien(maChuyenDi);
                if (cd != null && !memberIds.contains(cd.getMaNguoiDung())) {
                    memberIds.add(cd.getMaNguoiDung());
                }

                ThongBaoDAO tbDao = new ThongBaoDAO(this);
                String hanhDong = maHoatDong > 0 ? "cập nhật" : "thêm";
                tbDao.guiThongBaoCapNhatLichTrinh(curUserId, senderName, memberIds, maChuyenDi, tripName, title, hanhDong);
            } catch (Exception ignored) {}

            Toast.makeText(this, (maHoatDong > 0 ? "Cập nhật" : "Thêm") + " hoạt động vào lịch trình thành công ✓", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi lưu (ngày phải nằm trong khoảng ngày đi - ngày về của chuyến đi)", Toast.LENGTH_LONG).show();
        }
    }
}
