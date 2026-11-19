package com.example.mytripmatee.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mytripmatee.R;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.util.QuanLyPhien;
import com.example.mytripmatee.util.TienIchNgay;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SuaChuyenDiActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private MaterialButton btnSaveTop, btnSaveBottom, btnDelete;
    private TextInputEditText etTripName, etDestination;
    private EditText etBudget;
    private TextView tvHeaderTitle;
    private TextView tvDurationBadgeTime, tvDateRangeTime, tvCountdownTime;
    private TextView btnTabEdit, btnTabCreate;
    private MaterialCardView cvTime;

    private boolean isCreateMode = false;
    private int tripId = -1;
    private ChuyenDi currentTrip;

    private ChuyenDiDAO chuyenDiDAO;
    private QuanLyPhien quanLyPhien;
    private String selectedNgayDi = "2026-11-12";
    private String selectedNgayVe = "2026-11-15";

    private final ActivityResultLauncher<Intent> chonNgayLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String nDi = data.getStringExtra("extra_ngay_di");
                    String nVe = data.getStringExtra("extra_ngay_ve");
                    if (nDi != null && !nDi.isEmpty()) selectedNgayDi = nDi;
                    if (nVe != null && !nVe.isEmpty()) selectedNgayVe = nVe;
                    updateDateDisplay();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sua_chuyen_di);

        chuyenDiDAO = new ChuyenDiDAO(this);
        quanLyPhien = new QuanLyPhien(this);

        initViews();
        setupListeners();

        isCreateMode = getIntent().getBooleanExtra("mode_create", false);
        tripId = getIntent().getIntExtra("trip_id", getIntent().getIntExtra("maChuyenDi", -1));

        if (isCreateMode || tripId <= 0) {
            setupCreateMode();
        } else {
            setupEditMode(tripId);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSaveTop = findViewById(R.id.btnSaveTop);
        btnSaveBottom = findViewById(R.id.btnSaveBottom);
        btnDelete = findViewById(R.id.btnDelete);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        btnTabEdit = findViewById(R.id.btnTabEdit);
        btnTabCreate = findViewById(R.id.btnTabCreate);

        etTripName = findViewById(R.id.etTripName);
        etDestination = findViewById(R.id.etDestination);
        etBudget = findViewById(R.id.etBudget);

        cvTime = findViewById(R.id.cvTime);
        tvDurationBadgeTime = findViewById(R.id.tvDurationBadgeTime);
        tvDateRangeTime = findViewById(R.id.tvDateRangeTime);
        tvCountdownTime = findViewById(R.id.tvCountdownTime);
    }

    private void setupCreateMode() {
        isCreateMode = true;
        if (tvHeaderTitle != null) tvHeaderTitle.setText("Thêm chuyến đi mới");
        if (btnDelete != null) btnDelete.setVisibility(View.GONE);
        if (btnSaveBottom != null) btnSaveBottom.setText("Lưu & Tạo chuyến đi ✓");

        // Cập nhật giao diện 2 tab: Tạo chuyến mới (active), Chỉnh sửa (inactive)
        if (btnTabCreate != null) {
            btnTabCreate.setBackgroundResource(R.drawable.bg_pill_white);
            btnTabCreate.setTextColor(Color.parseColor("#00896A"));
            btnTabCreate.setTypeface(null, Typeface.BOLD);
        }
        if (btnTabEdit != null) {
            btnTabEdit.setBackgroundColor(Color.TRANSPARENT);
            btnTabEdit.setTextColor(Color.parseColor("#64748B"));
            btnTabEdit.setTypeface(null, Typeface.NORMAL);
            if (tripId <= 0) {
                btnTabEdit.setText("Chỉnh sửa");
            }
        }

        // Ngày mặc định: hôm nay và 3 ngày sau
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        selectedNgayDi = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 3);
        selectedNgayVe = sdf.format(cal.getTime());

        if (etTripName != null) etTripName.setText("");
        if (etDestination != null) etDestination.setText("");
        if (etBudget != null) etBudget.setText("3000000");

        updateDateDisplay();
    }

    private void setupEditMode(int id) {
        isCreateMode = false;
        currentTrip = chuyenDiDAO.layChuyenDiTheoId(id);
        if (currentTrip == null) {
            Toast.makeText(this, "Không tìm thấy chuyến đi!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (tvHeaderTitle != null) tvHeaderTitle.setText("Chỉnh sửa chuyến đi");
        if (btnDelete != null) btnDelete.setVisibility(View.VISIBLE);
        if (btnSaveBottom != null) btnSaveBottom.setText("Lưu & Cập nhật chuyến đi ✓");

        // Cập nhật giao diện 2 tab: Chỉnh sửa (active), Tạo chuyến mới (inactive)
        if (btnTabEdit != null) {
            btnTabEdit.setBackgroundResource(R.drawable.bg_pill_white);
            btnTabEdit.setTextColor(Color.parseColor("#00896A"));
            btnTabEdit.setTypeface(null, Typeface.BOLD);
            String shortName = (currentTrip.getDiemDen() != null && !currentTrip.getDiemDen().isEmpty())
                    ? currentTrip.getDiemDen().split(",")[0].trim()
                    : currentTrip.getTenChuyenDi();
            btnTabEdit.setText(shortName + " (Chỉnh sửa)");
        }
        if (btnTabCreate != null) {
            btnTabCreate.setBackgroundColor(Color.TRANSPARENT);
            btnTabCreate.setTextColor(Color.parseColor("#64748B"));
            btnTabCreate.setTypeface(null, Typeface.NORMAL);
        }

        selectedNgayDi = currentTrip.getNgayDi();
        selectedNgayVe = currentTrip.getNgayVe();

        if (etTripName != null) etTripName.setText(currentTrip.getTenChuyenDi());
        if (etDestination != null) etDestination.setText(currentTrip.getDiemDen());
        if (etBudget != null) etBudget.setText(String.valueOf(currentTrip.getNganSach()));
        updateDateDisplay();
    }

    private void updateDateDisplay() {
        String displayNgayDi = TienIchNgay.dinhDangHienThi(selectedNgayDi);
        String displayNgayVe = TienIchNgay.dinhDangHienThi(selectedNgayVe);

        if (tvDateRangeTime != null) {
            tvDateRangeTime.setText("Ngày đi: " + displayNgayDi + "\nNgày về: " + displayNgayVe);
        }

        long diffDays = 1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d1 = sdf.parse(selectedNgayDi);
            Date d2 = sdf.parse(selectedNgayVe);
            if (d1 != null && d2 != null) {
                long diffMs = d2.getTime() - d1.getTime();
                diffDays = Math.max(1, (diffMs / (24 * 60 * 60 * 1000)) + 1);
            }
        } catch (Exception ignored) {}

        String durationStr = diffDays + " Ngày " + Math.max(0, diffDays - 1) + " Đêm";
        if (tvDurationBadgeTime != null) tvDurationBadgeTime.setText(durationStr);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Chuyển đổi giữa 2 tab: Tạo chuyến mới và Chỉnh sửa
        if (btnTabCreate != null) {
            btnTabCreate.setOnClickListener(v -> {
                if (!isCreateMode) {
                    setupCreateMode();
                }
            });
        }

        if (btnTabEdit != null) {
            btnTabEdit.setOnClickListener(v -> {
                if (isCreateMode) {
                    if (tripId > 0) {
                        setupEditMode(tripId);
                    } else {
                        Toast.makeText(this, "Chưa có thông tin chuyến đi để chỉnh sửa", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        View.OnClickListener saveListener = v -> saveTrip();
        if (btnSaveTop != null) btnSaveTop.setOnClickListener(saveListener);
        if (btnSaveBottom != null) btnSaveBottom.setOnClickListener(saveListener);

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> confirmDelete());
        }

        if (cvTime != null) {
            cvTime.setOnClickListener(v -> {
                Intent intent = new Intent(SuaChuyenDiActivity.this, ChonNgayActivity.class);
                intent.putExtra("ngayDi", selectedNgayDi);
                intent.putExtra("ngayVe", selectedNgayVe);
                chonNgayLauncher.launch(intent);
            });
        }
    }

    private void saveTrip() {
        String tripName = etTripName.getText() != null ? etTripName.getText().toString().trim() : "";
        String destination = etDestination.getText() != null ? etDestination.getText().toString().trim() : "";
        String budgetStr = etBudget != null && etBudget.getText() != null ? etBudget.getText().toString().trim() : "";

        if (tripName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên chuyến đi", Toast.LENGTH_SHORT).show();
            etTripName.requestFocus();
            return;
        }

        long budget = 0;
        try {
            budget = Long.parseLong(budgetStr.replaceAll("[^0-9]", ""));
        } catch (Exception ignored) {}

        if (budget <= 0) {
            Toast.makeText(this, "Vui lòng nhập ngân sách hợp lệ (> 0 VNĐ)", Toast.LENGTH_SHORT).show();
            return;
        }

        int currentUserId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
        if (currentUserId <= 0) currentUserId = 1;

        if (isCreateMode) {
            // Thêm mới (INSERT)
            ChuyenDi newTrip = new ChuyenDi(currentUserId, tripName, destination, selectedNgayDi, selectedNgayVe, budget);
            long id = chuyenDiDAO.themChuyenDi(newTrip);
            if (id > 0) {
                Toast.makeText(this, "Tạo chuyến đi thành công", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Tạo thất bại (vui lòng kiểm tra ngày yyyy-MM-dd)", Toast.LENGTH_LONG).show();
            }
        } else {
            // Cập nhật (UPDATE)
            if (currentTrip != null) {
                currentTrip.setTenChuyenDi(tripName);
                currentTrip.setDiemDen(destination);
                currentTrip.setNganSach(budget);
                currentTrip.setNgayDi(selectedNgayDi);
                currentTrip.setNgayVe(selectedNgayVe);

                boolean success = chuyenDiDAO.capNhatChuyenDi(currentTrip);
                if (success) {
                    Toast.makeText(this, "Cập nhật chuyến đi thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Cập nhật thất bại (vui lòng kiểm tra ngày của lịch trình/chi phí)", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa chuyến đi")
                .setMessage("Bạn có chắc chắn muốn xóa chuyến đi này không? Mọi lịch trình và chi phí đi kèm cũng sẽ bị xóa vĩnh viễn.")
                .setPositiveButton("Xóa vĩnh viễn", (dialog, which) -> {
                    if (tripId > 0) {
                        boolean deleted = chuyenDiDAO.xoaChuyenDi(tripId);
                        if (deleted) {
                            Toast.makeText(SuaChuyenDiActivity.this, "Đã xóa chuyến đi thành công", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(SuaChuyenDiActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
