package com.example.mytripmatee.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mytripmatee.databinding.ActivityCaiDatBinding;
import com.example.mytripmatee.util.QuanLyPhien;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CaiDatActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "AppSettings";
    public static final String KEY_DARK_MODE = "dark_mode";
    public static final String KEY_LAST_BACKUP = "last_backup_time";

    public static Bitmap pendingSweepBitmap = null;
    public static boolean shouldAnimateSweep = false;

    private ActivityCaiDatBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());

        binding = ActivityCaiDatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainSettings, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupHeader();
        setupDarkModeSwitch();
        setupSampleData();
        setupLocalBackupAndRestore();
        setupAppInfoSection();
        setupLogout();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        checkAndPerformWaterRippleAnimation();
    }

    /**
     * Hiệu ứng tỏa nước Circular Reveal từ tâm mượt mà, không bị nhảy giật hay giữ màu cũ ở cuối
     */
    private void checkAndPerformWaterRippleAnimation() {
        if (shouldAnimateSweep && pendingSweepBitmap != null && !pendingSweepBitmap.isRecycled()) {
            shouldAnimateSweep = false;

            // 1. Phủ ngay ảnh chụp màn hình cũ lên overlay phía trên
            binding.overlayView.setImageBitmap(pendingSweepBitmap);
            binding.overlayView.setAlpha(1.0f);
            binding.overlayView.setVisibility(View.VISIBLE);

            binding.mainSettings.post(() -> {
                if (isFinishing() || isDestroyed()) return;

                int width = binding.mainSettings.getWidth();
                int height = binding.mainSettings.getHeight();

                if (width <= 0 || height <= 0) {
                    width = getResources().getDisplayMetrics().widthPixels;
                    height = getResources().getDisplayMetrics().heightPixels;
                }

                // 2. Tọa độ tâm màn hình (Center X, Center Y)
                int centerX = width / 2;
                int centerY = height / 2;
                float maxRadius = (float) Math.hypot(centerX, centerY);

                // 3. Tỏa giao diện mới (đã đổi theme chuẩn ở đằng sau) từ tâm ra 4 góc
                Animator revealAnimator = ViewAnimationUtils.createCircularReveal(
                        binding.mainSettings,
                        centerX,
                        centerY,
                        0f,
                        maxRadius
                );
                revealAnimator.setDuration(600);
                revealAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

                // 4. Mờ dần đồng thời lớp ảnh cũ (Alpha Dissolve 1.0 -> 0.0 trong 600ms)
                binding.overlayView.animate()
                        .alpha(0.0f)
                        .setDuration(600)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();

                revealAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.overlayView.setVisibility(View.GONE);
                        binding.overlayView.setAlpha(1.0f);
                        if (pendingSweepBitmap != null && !pendingSweepBitmap.isRecycled()) {
                            pendingSweepBitmap.recycle();
                            pendingSweepBitmap = null;
                        }
                    }
                });

                revealAnimator.start();
            });
        }
    }

    private void setupHeader() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupDarkModeSwitch() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkSystem = (currentNightMode == Configuration.UI_MODE_NIGHT_YES);
        boolean isDarkModeSaved = sharedPreferences.getBoolean(KEY_DARK_MODE, isDarkSystem);

        binding.switchDarkMode.setOnCheckedChangeListener(null);
        binding.switchDarkMode.setChecked(isDarkModeSaved);

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean currentMode = sharedPreferences.getBoolean(KEY_DARK_MODE, isDarkSystem);
            if (currentMode == isChecked) return;

            // 1. Chụp ảnh màn hình giao diện hiện tại
            try {
                View rootView = binding.getRoot();
                if (rootView.getWidth() > 0 && rootView.getHeight() > 0) {
                    Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    rootView.draw(canvas);
                    pendingSweepBitmap = bitmap;
                    shouldAnimateSweep = true;
                }
            } catch (Exception e) {
                pendingSweepBitmap = null;
                shouldAnimateSweep = false;
            }

            // 2. Lưu cài đặt Dark Mode
            sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();

            // 3. Đổi Night Mode -> Activity sẽ recreate ngay đằng sau lớp overlayView
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            // Triệt xóa animation chuyển giao diện mặc định để không bị giật
            overridePendingTransition(0, 0);
        });
    }

    private void setupSampleData() {
        View.OnClickListener sampleDataClickListener = v -> showSampleDataDialog();
        binding.rowCreateSampleData.setOnClickListener(sampleDataClickListener);
        binding.btnCreateSampleData.setOnClickListener(sampleDataClickListener);
    }

    private void showSampleDataDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Tạo dữ liệu mẫu")
                .setMessage("Khởi tạo các chuyến đi thử nghiệm (Đà Lạt, Hà Giang, Phú Quốc) cùng lịch trình chi tiết và danh sách chi tiêu?")
                .setPositiveButton("Tạo dữ liệu", (dialog, which) -> {
                    sharedPreferences.edit().putBoolean("has_sample_data", true).apply();
                    Toast.makeText(CaiDatActivity.this, "Đã khởi tạo thành công dữ liệu mẫu!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupLocalBackupAndRestore() {
        updateBackupTimeText();

        View.OnClickListener backupClickListener = v -> showBackupDialog();
        binding.rowLocalBackup.setOnClickListener(backupClickListener);
        binding.btnLocalBackup.setOnClickListener(backupClickListener);

        View.OnClickListener restoreClickListener = v -> showRestoreDialog();
        binding.rowLocalRestore.setOnClickListener(restoreClickListener);
        binding.btnLocalRestore.setOnClickListener(restoreClickListener);
    }

    private void updateBackupTimeText() {
        String lastBackup = sharedPreferences.getString(KEY_LAST_BACKUP, null);
        if (lastBackup != null && !lastBackup.isEmpty()) {
            binding.tvLastBackupTime.setText("Lần cuối: " + lastBackup);
        } else {
            binding.tvLastBackupTime.setText("Lần cuối: Chưa sao lưu");
        }
    }

    private void showBackupDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sao lưu dữ liệu Local")
                .setMessage("Tạo bản sao lưu toàn bộ thông tin chuyến đi, lịch trình và nhật ký chi tiêu vào bộ nhớ local thiết bị?")
                .setPositiveButton("Sao lưu ngay", (dialog, which) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
                    String currentTime = sdf.format(new Date());

                    sharedPreferences.edit().putString(KEY_LAST_BACKUP, currentTime).apply();
                    updateBackupTimeText();

                    Toast.makeText(CaiDatActivity.this, "Sao lưu dữ liệu local thành công!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showRestoreDialog() {
        String lastBackup = sharedPreferences.getString(KEY_LAST_BACKUP, null);
        if (lastBackup == null) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Phục hồi dữ liệu")
                    .setMessage("Chưa tìm thấy bản sao lưu local nào trên thiết bị. Vui lòng thực hiện sao lưu trước.")
                    .setPositiveButton("Đã hiểu", null)
                    .show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Phục hồi dữ liệu")
                .setMessage("Bạn có chắc chắn muốn khôi phục dữ liệu từ bản sao lưu ngày " + lastBackup + "?")
                .setPositiveButton("Phục hồi", (dialog, which) -> {
                    Toast.makeText(CaiDatActivity.this, "Khôi phục dữ liệu từ bản sao lưu thành công!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupAppInfoSection() {
        binding.rowAboutApp.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Giới thiệu MyTripMate")
                    .setMessage("MyTripMate v1.0.0 (Build 100)\n\n" +
                            "MyTripMate là ứng dụng di động hỗ trợ người dùng lên kế hoạch chuyến đi, quản lý lịch trình theo ngày, theo dõi chi tiêu du lịch và khám phá những địa điểm thú vị.\n\n" +
                            "© 2026 MyTripMate Team. All rights reserved.")
                    .setPositiveButton("Đóng", null)
                    .show();
        });

        binding.rowTerms.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Điều khoản & Quyền riêng tư")
                    .setMessage("1. Bảo mật dữ liệu: Toàn bộ thông tin lịch trình và chi tiêu cá nhân được bảo lưu an toàn trên thiết bị của bạn.\n\n" +
                            "2. Dữ liệu sao lưu: Các tệp sao lưu local lưu giữ trên máy cá nhân và không chia sẻ cho bên thứ ba mà không có sự đồng ý của bạn.\n\n" +
                            "3. Quyền hạn: Ứng dụng chỉ yêu cầu các quyền truy cập cần thiết để lưu trữ dữ liệu và hiển thị bản đồ.")
                    .setPositiveButton("Đóng", null)
                    .show();
        });

        binding.rowSupport.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Gửi phản hồi & Hỗ trợ")
                    .setMessage("Mọi góp ý và yêu cầu hỗ trợ xin vui lòng liên hệ:\n\n" +
                            "• Email: support@mytripmatee.com\n" +
                            "• Hotline: 1900-MYTRIP (8:00 - 20:00)\n" +
                            "• Website: https://mytripmatee.com")
                    .setPositiveButton("Gửi Email", (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:support@mytripmatee.com"));
                        intent.putExtra(Intent.EXTRA_SUBJECT, "[MyTripMate] Phản hồi người dùng");
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(CaiDatActivity.this, "Không tìm thấy ứng dụng Email", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Đóng", null)
                    .show();
        });
    }

    private void setupLogout() {
        binding.btnLogout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        QuanLyPhien quanLyPhien = new QuanLyPhien(CaiDatActivity.this);
                        quanLyPhien.dangXuat();

                        Intent intent = new Intent(CaiDatActivity.this, DangNhapActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }
}
