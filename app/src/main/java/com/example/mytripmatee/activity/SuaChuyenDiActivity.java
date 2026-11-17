package com.example.mytripmatee.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mytripmatee.R;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.util.TienIchNgay;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SuaChuyenDiActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private MaterialButton btnSaveTop, btnSaveBottom, btnDelete;
    private TextInputEditText etTripName, etDestination;
    private TextView tvHeaderTitle;
    private TextView tvTripTitleCover, tvDurationBadgeCover, tvTripMetaCover;
    private TextView tvDurationBadgeTime, tvDateRangeTime, tvCountdownTime;
    private TextView btnTabEdit, btnTabCreate;
    private MaterialCardView cvTime;
    private ChuyenDiDAO chuyenDiDAO;

    private boolean isCreateMode = false;
    private int maChuyenDi = 1;
    private String ngayDiDb = "2026-11-12";
    private String ngayVeDb = "2026-11-15";

    private final ActivityResultLauncher<Intent> chonNgayLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String nDi = data.getStringExtra("extra_ngay_di");
                    String nVe = data.getStringExtra("extra_ngay_ve");
                    if (nDi != null && !nDi.isEmpty()) ngayDiDb = nDi;
                    if (nVe != null && !nVe.isEmpty()) ngayVeDb = nVe;
                    updateDateDisplay();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sua_chuyen_di);

        chuyenDiDAO = new ChuyenDiDAO(this);

        initViews();
        setupListeners();

        boolean startInCreateMode = getIntent().getBooleanExtra("mode_create", false);
        maChuyenDi = getIntent().getIntExtra("maChuyenDi", 1);
        switchMode(startInCreateMode);

        if (!startInCreateMode) {
            loadExistingTripData();
        }

        String incomingTitle = getIntent().getStringExtra("trip_title");
        if (incomingTitle != null && !incomingTitle.isEmpty()) {
            if (etTripName != null)
                etTripName.setText(incomingTitle);
            if (tvTripTitleCover != null)
                tvTripTitleCover.setText(incomingTitle);
            if (btnTabEdit != null)
                btnTabEdit.setText(incomingTitle + " (Chỉnh sửa)");
        }
    }

    private void loadExistingTripData() {
        ChuyenDi existing = chuyenDiDAO.layChuyenDiTheoId(maChuyenDi);
        if (existing != null) {
            if (etTripName != null && existing.getTenChuyenDi() != null) {
                etTripName.setText(existing.getTenChuyenDi());
            }
            if (etDestination != null && existing.getDiemDen() != null) {
                etDestination.setText(existing.getDiemDen());
            }
            if (existing.getNgayDi() != null) {
                ngayDiDb = existing.getNgayDi();
            }
            if (existing.getNgayVe() != null) {
                ngayVeDb = existing.getNgayVe();
            }
            updateDateDisplay();
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
        tvTripTitleCover = findViewById(R.id.tvTripTitleCover);
        tvDurationBadgeCover = findViewById(R.id.tvDurationBadgeCover);
        tvTripMetaCover = findViewById(R.id.tvTripMetaCover);

        cvTime = findViewById(R.id.cvTime);
        tvDurationBadgeTime = findViewById(R.id.tvDurationBadgeTime);
        tvDateRangeTime = findViewById(R.id.tvDateRangeTime);
        tvCountdownTime = findViewById(R.id.tvCountdownTime);
    }

    private void switchMode(boolean createMode) {
        this.isCreateMode = createMode;

        if (createMode) {
            if (btnTabCreate != null) {
                btnTabCreate.setBackgroundResource(R.drawable.bg_pill_white);
                btnTabCreate.setTextColor(Color.parseColor("#00896A"));
                btnTabCreate.setTypeface(null, Typeface.BOLD);
            }
            if (btnTabEdit != null) {
                btnTabEdit.setBackgroundColor(Color.TRANSPARENT);
                btnTabEdit.setTextColor(Color.parseColor("#64748B"));
                btnTabEdit.setTypeface(null, Typeface.NORMAL);
            }

            if (tvHeaderTitle != null)
                tvHeaderTitle.setText("Thêm chuyến đi mới");
            if (etTripName != null)
                etTripName.setText("Hành trình mới cùng bạn bè");
            if (etDestination != null)
                etDestination.setText("Nha Trang, Khánh Hòa");
            if (btnDelete != null)
                btnDelete.setVisibility(View.GONE);
            if (btnSaveBottom != null)
                btnSaveBottom.setText("Lưu & Tạo chuyến đi ✓");
            updateDateDisplay();
        } else {
            if (btnTabEdit != null) {
                btnTabEdit.setBackgroundResource(R.drawable.bg_pill_white);
                btnTabEdit.setTextColor(Color.parseColor("#00896A"));
                btnTabEdit.setTypeface(null, Typeface.BOLD);
            }
            if (btnTabCreate != null) {
                btnTabCreate.setBackgroundColor(Color.TRANSPARENT);
                btnTabCreate.setTextColor(Color.parseColor("#64748B"));
                btnTabCreate.setTypeface(null, Typeface.NORMAL);
            }

            if (tvHeaderTitle != null)
                tvHeaderTitle.setText("Chỉnh sửa chuyến đi");
            if (btnDelete != null)
                btnDelete.setVisibility(View.VISIBLE);
            if (btnSaveBottom != null)
                btnSaveBottom.setText("Lưu & Cập nhật lịch trình ✓");
            updateDateDisplay();
        }
    }

    private void showDateRangePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialogDi = new DatePickerDialog(this, (view, y1, m1, d1) -> {
            ngayDiDb = String.format(Locale.getDefault(), "%04d-%02d-%02d", y1, m1 + 1, d1);

            DatePickerDialog dialogVe = new DatePickerDialog(SuaChuyenDiActivity.this, (view2, y2, m2, d2) -> {
                ngayVeDb = String.format(Locale.getDefault(), "%04d-%02d-%02d", y2, m2 + 1, d2);
                updateDateDisplay();
            }, y1, m1, d1);
            dialogVe.setTitle("Chọn ngày về (kết thúc)");
            dialogVe.show();

        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialogDi.setTitle("Chọn ngày đi (bắt đầu)");
        dialogDi.show();
    }

    private void updateDateDisplay() {
        String displayNgayDi = TienIchNgay.dinhDangHienThi(ngayDiDb);
        String displayNgayVe = TienIchNgay.dinhDangHienThi(ngayVeDb);

        if (tvDateRangeTime != null) {
            tvDateRangeTime.setText("Ngày đi: " + displayNgayDi + "\nNgày về: " + displayNgayVe);
        }

        long diffDays = 1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d1 = sdf.parse(ngayDiDb);
            Date d2 = sdf.parse(ngayVeDb);
            if (d1 != null && d2 != null) {
                long diffMs = d2.getTime() - d1.getTime();
                diffDays = Math.max(1, (diffMs / (24 * 60 * 60 * 1000)) + 1);
            }
        } catch (Exception ignored) {}

        String durationStr = diffDays + " Ngày " + Math.max(0, diffDays - 1) + " Đêm";
        if (tvDurationBadgeTime != null) tvDurationBadgeTime.setText(durationStr);
        if (tvDurationBadgeCover != null) tvDurationBadgeCover.setText(durationStr);
        if (tvTripMetaCover != null) tvTripMetaCover.setText("📅 " + displayNgayDi + " – " + displayNgayVe + " • 👥 3 thành viên");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        if (btnTabEdit != null) {
            btnTabEdit.setOnClickListener(v -> switchMode(false));
        }
        if (btnTabCreate != null) {
            btnTabCreate.setOnClickListener(v -> switchMode(true));
        }

        if (etTripName != null && tvTripTitleCover != null) {
            etTripName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String currentText = s.toString().trim();
                    if (currentText.isEmpty()) {
                        tvTripTitleCover.setText("Tên chuyến đi...");
                    } else {
                        tvTripTitleCover.setText(currentText);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        View.OnClickListener saveListener = v -> saveTrip();
        if (btnSaveTop != null) btnSaveTop.setOnClickListener(saveListener);
        if (btnSaveBottom != null) btnSaveBottom.setOnClickListener(saveListener);

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                if (!isCreateMode && maChuyenDi > 0) {
                    chuyenDiDAO.xoaChuyenDi(maChuyenDi);
                }
                Toast.makeText(SuaChuyenDiActivity.this, "Đã xóa chuyến đi", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        }

        if (cvTime != null) {
            cvTime.setOnClickListener(v -> {
                Intent intent = new Intent(SuaChuyenDiActivity.this, ChonNgayActivity.class);
                intent.putExtra("ngayDi", ngayDiDb);
                intent.putExtra("ngayVe", ngayVeDb);
                chonNgayLauncher.launch(intent);
            });
        }
    }

    private void saveTrip() {
        String tripName = etTripName.getText() != null ? etTripName.getText().toString().trim() : "";
        String destination = etDestination.getText() != null ? etDestination.getText().toString().trim() : "";

        if (tripName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên chuyến đi", Toast.LENGTH_SHORT).show();
            etTripName.requestFocus();
            return;
        }

        ChuyenDi cd = new ChuyenDi(1, tripName, destination, ngayDiDb, ngayVeDb, 5000000);
        if (!isCreateMode && maChuyenDi > 0) {
            cd.setMaChuyenDi(maChuyenDi);
            chuyenDiDAO.capNhatChuyenDi(cd);
        } else {
            chuyenDiDAO.themChuyenDi(cd);
        }

        String msg = isCreateMode ? "Tạo chuyến đi mới thành công ✓" : "Lưu chuyến đi thành công ✓";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
