package com.example.mytripmatee.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mytripmatee.R;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.util.QuanLyPhien;
import com.example.mytripmatee.util.TienIchNgay;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.example.mytripmatee.database.NguoiDungDAO;
import com.example.mytripmatee.model.NguoiDung;
import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SuaChuyenDiActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private MaterialButton btnSaveTop, btnSaveBottom, btnDelete;
    private TextInputEditText etTripName, etDestination;
    private EditText etBudget;
    private TextView tvHeaderTitle, tvHeaderSub;
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
    // quản lý thành viên tham gia
    private MaterialCardView cvMembers;
    private TextView tvMembersSubtitle;
    private NguoiDungDAO nguoiDungDAO;
    private List<NguoiDung> danhSachNguoiDungDb = new ArrayList<>();
    private List<Integer> danhSachIdThanhVienChon = new ArrayList<>();

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
        WindowCompat.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_sua_chuyen_di);

        View rootView = findViewById(R.id.rootLayout);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        chuyenDiDAO = new ChuyenDiDAO(this);
        quanLyPhien = new QuanLyPhien(this);
        nguoiDungDAO = new NguoiDungDAO(this);

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
        tvHeaderSub = findViewById(R.id.tvHeaderSub);
        btnTabEdit = findViewById(R.id.btnTabEdit);
        btnTabCreate = findViewById(R.id.btnTabCreate);

        etTripName = findViewById(R.id.etTripName);
        etDestination = findViewById(R.id.etDestination);
        etBudget = findViewById(R.id.etBudget);

        cvTime = findViewById(R.id.cvTime);
        tvDurationBadgeTime = findViewById(R.id.tvDurationBadgeTime);
        tvDateRangeTime = findViewById(R.id.tvDateRangeTime);
        tvCountdownTime = findViewById(R.id.tvCountdownTime);

        cvMembers = findViewById(R.id.cvMembers);
        tvMembersSubtitle = findViewById(R.id.tvMembersSubtitle);
    }

    private void setupCreateMode() {
        isCreateMode = true;
        if (tvHeaderTitle != null) tvHeaderTitle.setText("Thêm chuyến đi mới");
        if (tvHeaderSub != null) tvHeaderSub.setText("Tạo hành trình mới cho riêng bạn");
        if (btnDelete != null) btnDelete.setVisibility(View.GONE);
        if (btnSaveBottom != null) btnSaveBottom.setText("Lưu & Tạo chuyến đi ✓");

        int activeColor = ContextCompat.getColor(this, R.color.teal_primary);
        int inactiveColor = ContextCompat.getColor(this, R.color.text_secondary);

        if (btnTabCreate != null) {
            btnTabCreate.setBackgroundResource(R.drawable.bg_pill_white);
            btnTabCreate.setTextColor(activeColor);
            btnTabCreate.setTypeface(null, Typeface.BOLD);
        }
        if (btnTabEdit != null) {
            btnTabEdit.setBackgroundColor(Color.TRANSPARENT);
            btnTabEdit.setTextColor(inactiveColor);
            btnTabEdit.setTypeface(null, Typeface.NORMAL);
            if (tripId <= 0) {
                btnTabEdit.setText("Chỉnh sửa");
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        selectedNgayDi = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 3);
        selectedNgayVe = sdf.format(cal.getTime());

        if (etTripName != null) etTripName.setText("");
        if (etDestination != null) etDestination.setText("");
        if (etBudget != null) etBudget.setText("3000000");

        updateDateDisplay();

        int currentUserId = (quanLyPhien != null) ? quanLyPhien.getMaNguoiDung() : 1;
        danhSachIdThanhVienChon.clear();
        danhSachIdThanhVienChon.add(currentUserId);
        capNhatHienThiThanhVien();
    }

    private void setupEditMode(int id) {
        isCreateMode = false;
        currentTrip = chuyenDiDAO.layChuyenDiTheoId(id);
        if (currentTrip == null) {
            Toast.makeText(this, "Không tìm thấy chuyến đi!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int currentUserId = (quanLyPhien != null) ? quanLyPhien.getMaNguoiDung() : 1;
        if (currentTrip.getMaNguoiDung() != currentUserId) {
            Toast.makeText(this, "Chỉ Trưởng nhóm (người tạo chuyến đi) mới có quyền chỉnh sửa hoặc xóa chuyến đi này!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (tvHeaderTitle != null) tvHeaderTitle.setText("Chỉnh sửa chuyến đi");
        if (tvHeaderSub != null) tvHeaderSub.setText("Quản lý thông tin & lịch trình");
        if (btnDelete != null) btnDelete.setVisibility(View.VISIBLE);
        if (btnSaveBottom != null) btnSaveBottom.setText("Lưu & Cập nhật chuyến đi ✓");

        int activeColor = ContextCompat.getColor(this, R.color.teal_primary);
        int inactiveColor = ContextCompat.getColor(this, R.color.text_secondary);

        if (btnTabEdit != null) {
            btnTabEdit.setBackgroundResource(R.drawable.bg_pill_white);
            btnTabEdit.setTextColor(activeColor);
            btnTabEdit.setTypeface(null, Typeface.BOLD);
            String shortName = (currentTrip.getDiemDen() != null && !currentTrip.getDiemDen().isEmpty())
                    ? currentTrip.getDiemDen().split(",")[0].trim()
                    : currentTrip.getTenChuyenDi();
            btnTabEdit.setText(shortName + " (Chỉnh sửa)");
        }
        if (btnTabCreate != null) {
            btnTabCreate.setBackgroundColor(Color.TRANSPARENT);
            btnTabCreate.setTextColor(inactiveColor);
            btnTabCreate.setTypeface(null, Typeface.NORMAL);
        }

        selectedNgayDi = currentTrip.getNgayDi();
        selectedNgayVe = currentTrip.getNgayVe();

        if (etTripName != null) etTripName.setText(currentTrip.getTenChuyenDi());
        if (etDestination != null) etDestination.setText(currentTrip.getDiemDen());
        if (etBudget != null) etBudget.setText(String.valueOf(currentTrip.getNganSach()));
        updateDateDisplay();

        currentUserId = (currentTrip != null) ? currentTrip.getMaNguoiDung() : 1;
        List<Integer> listThanhVien = chuyenDiDAO.layDanhSachIdThanhVien(id);
        danhSachIdThanhVienChon.clear();
        if (listThanhVien != null && !listThanhVien.isEmpty()) {
            danhSachIdThanhVienChon.addAll(listThanhVien);
        } else {
            danhSachIdThanhVienChon.add(currentUserId);
        }
        if (!danhSachIdThanhVienChon.contains(currentUserId)) {
            danhSachIdThanhVienChon.add(currentUserId);
        }
        capNhatHienThiThanhVien();
    }

    private void updateDateDisplay() {
        String displayNgayDi = TienIchNgay.dinhDangHienThi(selectedNgayDi);
        String displayNgayVe = TienIchNgay.dinhDangHienThi(selectedNgayVe);

        if (tvDateRangeTime != null) {
            tvDateRangeTime.setText("Ngày đi: " + displayNgayDi + "\nNgày về: " + displayNgayVe);
        }

        long diffDays = 1;
        long daysUntilStart = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d1 = sdf.parse(selectedNgayDi);
            Date d2 = sdf.parse(selectedNgayVe);
            Date today = sdf.parse(sdf.format(new Date()));

            if (d1 != null && d2 != null) {
                long diffMs = d2.getTime() - d1.getTime();
                diffDays = Math.max(1, (diffMs / (24 * 60 * 60 * 1000)) + 1);
            }

            if (d1 != null && today != null) {
                long diffMsToday = d1.getTime() - today.getTime();
                daysUntilStart = diffMsToday / (24 * 60 * 60 * 1000);
            }
        } catch (Exception ignored) {}

        String durationStr;
        if (diffDays <= 1) {
            durationStr = "1 Ngày (Trong ngày)";
        } else {
            durationStr = diffDays + " Ngày " + (diffDays - 1) + " Đêm";
        }

        if (tvDurationBadgeTime != null) {
            tvDurationBadgeTime.setText(durationStr);
        }

        if (tvCountdownTime != null) {
            if (daysUntilStart > 0) {
                tvCountdownTime.setText("Còn " + daysUntilStart + " ngày");
            } else if (daysUntilStart == 0) {
                tvCountdownTime.setText("Hôm nay");
            } else {
                tvCountdownTime.setText("Đã đi");
            }
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

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
                        ChuyenDi cd = chuyenDiDAO.layChuyenDiTheoId(tripId);
                        int currentUserId = (quanLyPhien != null) ? quanLyPhien.getMaNguoiDung() : 1;
                        if (cd != null && cd.getMaNguoiDung() != currentUserId) {
                            Toast.makeText(this, "Chỉ Trưởng nhóm (người tạo chuyến đi) mới có quyền chỉnh sửa!", Toast.LENGTH_SHORT).show();
                            return;
                        }
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

        if (cvMembers != null) {
            cvMembers.setOnClickListener(v -> hienThiDialogChonThanhVien());
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

        if (!danhSachIdThanhVienChon.contains(currentUserId)) {
            danhSachIdThanhVienChon.add(currentUserId);
        }

        if (isCreateMode) {
            ChuyenDi newTrip = new ChuyenDi(currentUserId, tripName, destination, selectedNgayDi, selectedNgayVe, budget);
            newTrip.setSoThanhVien(danhSachIdThanhVienChon.size());
            long id = chuyenDiDAO.themChuyenDi(newTrip, danhSachIdThanhVienChon);
            if (id > 0) {
                Toast.makeText(this, "Tạo chuyến đi thành công", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Tạo thất bại (vui lòng kiểm tra ngày yyyy-MM-dd)", Toast.LENGTH_LONG).show();
            }
        } else {
            if (currentTrip != null) {
                currentTrip.setTenChuyenDi(tripName);
                currentTrip.setDiemDen(destination);
                currentTrip.setNganSach(budget);
                currentTrip.setNgayDi(selectedNgayDi);
                currentTrip.setNgayVe(selectedNgayVe);
                currentTrip.setSoThanhVien(danhSachIdThanhVienChon.size());

                boolean success = chuyenDiDAO.capNhatChuyenDi(currentTrip, danhSachIdThanhVienChon);
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

    // hiển thị Dialog Multi-Choice danh sách thành viên từ Database
    private void hienThiDialogChonThanhVien() {
        danhSachNguoiDungDb = nguoiDungDAO.layTatCaNguoiDung();
        if (danhSachNguoiDungDb.isEmpty()) {
            Toast.makeText(this, "Chưa có danh sách người dùng trong hệ thống", Toast.LENGTH_SHORT).show();
            return;
        }

        int size = danhSachNguoiDungDb.size();
        String[] tenHienThi = new String[size];
        boolean[] daChon = new boolean[size];

        for (int i = 0; i < size; i++) {
            NguoiDung nd = danhSachNguoiDungDb.get(i);
            String hoTen = (nd.getHoTen() != null && !nd.getHoTen().isEmpty()) ? nd.getHoTen() : nd.getTenDangNhap();
            tenHienThi[i] = hoTen + " (@" + nd.getTenDangNhap() + ")";
            daChon[i] = danhSachIdThanhVienChon.contains(nd.getMaNguoiDung());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Chọn thành viên tham gia")
                .setMultiChoiceItems(tenHienThi, daChon, (dialog, which, isChecked) -> {
                    int idNguoiDung = danhSachNguoiDungDb.get(which).getMaNguoiDung();
                    if (isChecked) {
                        if (!danhSachIdThanhVienChon.contains(idNguoiDung)) {
                            danhSachIdThanhVienChon.add(idNguoiDung);
                        }
                    } else {
                        danhSachIdThanhVienChon.remove(Integer.valueOf(idNguoiDung));
                    }
                })
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    capNhatHienThiThanhVien();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // cập nhật chữ hiển thị trên thẻ thành viên
    private void capNhatHienThiThanhVien() {
        if (tvMembersSubtitle == null) return;

        if (danhSachIdThanhVienChon.isEmpty()) {
            tvMembersSubtitle.setText("Chưa chọn thành viên nào");
            return;
        }

        if (danhSachNguoiDungDb.isEmpty() && nguoiDungDAO != null) {
            danhSachNguoiDungDb = nguoiDungDAO.layTatCaNguoiDung();
        }

        List<String> tenCacThanhVien = new ArrayList<>();
        for (NguoiDung nd : danhSachNguoiDungDb) {
            if (danhSachIdThanhVienChon.contains(nd.getMaNguoiDung())) {
                String ten = (nd.getHoTen() != null && !nd.getHoTen().isEmpty()) ? nd.getHoTen() : nd.getTenDangNhap();
                tenCacThanhVien.add(ten);
            }
        }

        int soLuong = danhSachIdThanhVienChon.size();
        if (soLuong == 1) {
            String ten = !tenCacThanhVien.isEmpty() ? tenCacThanhVien.get(0) : "Bạn";
            tvMembersSubtitle.setText("Chuyến đi cá nhân (1 thành viên: " + ten + ")");
        } else {
            String danhSachTen = android.text.TextUtils.join(", ", tenCacThanhVien);
            tvMembersSubtitle.setText(soLuong + " thành viên: " + danhSachTen);
        }
    }
}
