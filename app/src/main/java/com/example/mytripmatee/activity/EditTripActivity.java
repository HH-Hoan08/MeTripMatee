package com.example.mytripmatee.activity;

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

import androidx.appcompat.app.AppCompatActivity;

import com.example.mytripmatee.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class EditTripActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private MaterialButton btnSaveTop, btnSaveBottom, btnDelete;
    private TextInputEditText etTripName, etDestination;
    private TextView tvHeaderTitle;
    private TextView tvTripTitleCover, tvDurationBadgeCover, tvTripMetaCover;
    private TextView tvDurationBadgeTime, tvDateRangeTime, tvCountdownTime;
    private TextView btnTabEdit, btnTabCreate;
    private MaterialCardView cvTime;
    private boolean isCreateMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_edit);
        initViews();
        setupListeners();

        boolean startInCreateMode = getIntent().getBooleanExtra("mode_create", false);
        switchMode(startInCreateMode);

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
            if (tvDurationBadgeCover != null)
                tvDurationBadgeCover.setText("4 Ngày 3 Đêm");
            if (tvTripTitleCover != null)
                tvTripTitleCover.setText("Hành trình mới cùng bạn bè");
            if (tvTripMetaCover != null)
                tvTripMetaCover.setText("📅 18/11 – 22/11/2026 • 👥 3 thành viên");
            if (etTripName != null)
                etTripName.setText("Hành trình mới cùng bạn bè");
            if (etDestination != null)
                etDestination.setText("Nha Trang, Khánh Hòa");
            if (tvDurationBadgeTime != null)
                tvDurationBadgeTime.setText("4 Ngày 3 Đêm");
            if (tvDateRangeTime != null)
                tvDateRangeTime.setText("18/11/2026 →\n22/11/2026");
            if (tvCountdownTime != null)
                tvCountdownTime.setText("Khởi hành sau\n3 ngày");
            if (btnDelete != null)
                btnDelete.setVisibility(View.GONE);
            if (btnSaveBottom != null)
                btnSaveBottom.setText("Lưu & Tạo chuyến đi ✓");
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
            if (tvDurationBadgeCover != null)
                tvDurationBadgeCover.setText("3 Ngày 2 Đêm");
            if (tvTripTitleCover != null)
                tvTripTitleCover.setText("Đà Lạt - Sương Mù & Thảnh Thơi");
            if (tvTripMetaCover != null)
                tvTripMetaCover.setText("📅 12/11 – 15/11/2026 • 👥 3 thành viên");
            if (etTripName != null)
                etTripName.setText("Đà Lạt - Sương Mù & Thảnh Thơi");
            if (etDestination != null)
                etDestination.setText("Đà Lạt, Lâm Đồng");
            if (tvDurationBadgeTime != null)
                tvDurationBadgeTime.setText("3 Ngày 2 Đêm");
            if (tvDateRangeTime != null)
                tvDateRangeTime.setText("12/11/2026 →\n15/11/2026");
            if (tvCountdownTime != null)
                tvCountdownTime.setText("Khởi hành sau\n3 ngày");
            if (btnDelete != null)
                btnDelete.setVisibility(View.VISIBLE);
            if (btnSaveBottom != null)
                btnSaveBottom.setText("Lưu & Cập nhật lịch trình ✓");
        }
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
                Toast.makeText(EditTripActivity.this, "Đã xóa chuyến đi", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        if (cvTime != null) {
            cvTime.setOnClickListener(v -> {
                Intent intent = new Intent(EditTripActivity.this, CustomDatePickerActivity.class);
                startActivity(intent);
            });
        }
    }

    private void saveTrip() {
        String tripName = etTripName.getText() != null ? etTripName.getText().toString().trim() : "";

        if (tripName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên chuyến đi", Toast.LENGTH_SHORT).show();
            etTripName.requestFocus();
            return;
        }

        String msg = isCreateMode ? "Tạo chuyến đi mới thành công" : "Lưu chuyến đi thành công";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }
}
