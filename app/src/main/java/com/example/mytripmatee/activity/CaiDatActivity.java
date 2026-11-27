package com.example.mytripmatee.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mytripmatee.R;
import com.example.mytripmatee.dao.AnhDAO;
import com.example.mytripmatee.dao.ChiPhiDAO;
import com.example.mytripmatee.dao.LichTrinhDAO;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.database.DBHelper;
import com.example.mytripmatee.databinding.ActivityCaiDatBinding;
import com.example.mytripmatee.model.ChiPhi;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.model.LichTrinh;
import com.example.mytripmatee.util.QuanLyPhien;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CaiDatActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "AppSettings";
    public static final String KEY_DARK_MODE = "dark_mode";
    public static final String KEY_LAST_BACKUP = "last_backup_time";

    public static Bitmap pendingSweepBitmap = null;
    public static boolean shouldAnimateSweep = false;
    public static boolean isAnimatingDarkMode = false;

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
     * Hiệu ứng tỏa nước Circular Reveal từ tâm mượt mà, chống spam công tắc khi animation đang chạy
     */
    private void checkAndPerformWaterRippleAnimation() {
        if (shouldAnimateSweep && pendingSweepBitmap != null && !pendingSweepBitmap.isRecycled()) {
            shouldAnimateSweep = false;
            isAnimatingDarkMode = true;

            if (binding.rowDarkMode != null) {
                binding.rowDarkMode.setEnabled(false);
            }

            // 1. Phủ ngay ảnh chụp màn hình cũ lên overlay phía trên
            binding.overlayView.setImageBitmap(pendingSweepBitmap);
            binding.overlayView.setAlpha(1.0f);
            binding.overlayView.setVisibility(View.VISIBLE);

            binding.mainSettings.post(() -> {
                if (isFinishing() || isDestroyed()) {
                    isAnimatingDarkMode = false;
                    if (binding.rowDarkMode != null) {
                        binding.rowDarkMode.setEnabled(true);
                    }
                    return;
                }

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

                // 3. Tỏa giao diện mới từ tâm ra 4 góc
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

                        // Mở khóa lại hàng chọn sau khi 100% hiệu ứng tỏa nước đã kết thúc
                        isAnimatingDarkMode = false;
                        if (binding.rowDarkMode != null) {
                            binding.rowDarkMode.setEnabled(true);
                        }

                        if (pendingSweepBitmap != null && !pendingSweepBitmap.isRecycled()) {
                            pendingSweepBitmap.recycle();
                            pendingSweepBitmap = null;
                        }
                    }
                });

                revealAnimator.start();
            });
        } else {
            isAnimatingDarkMode = false;
            if (binding.rowDarkMode != null) {
                binding.rowDarkMode.setEnabled(true);
            }
        }
    }

    private void setupHeader() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupDarkModeSwitch() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkSystem = (currentNightMode == Configuration.UI_MODE_NIGHT_YES);
        boolean isDarkModeSaved = sharedPreferences.getBoolean(KEY_DARK_MODE, isDarkSystem);

        updateDarkModeUI(isDarkModeSaved);

        View.OnClickListener toggleClickListener = v -> {
            if (isAnimatingDarkMode) return;

            boolean currentSavedMode = sharedPreferences.getBoolean(KEY_DARK_MODE, isDarkSystem);
            boolean newMode = !currentSavedMode;

            isAnimatingDarkMode = true;
            binding.rowDarkMode.setEnabled(false);

            // Animate Icon bên trái (iconDarkMode) xoay 360 và thu phóng lật mặt
            if (binding.iconDarkMode != null) {
                binding.iconDarkMode.animate()
                        .rotationBy(360f)
                        .scaleX(0.2f)
                        .scaleY(0.2f)
                        .setDuration(220)
                        .withEndAction(() -> {
                            updateDarkModeUI(newMode);
                            binding.iconDarkMode.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(220)
                                    .start();
                        })
                        .start();
            }

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
                isAnimatingDarkMode = false;
                binding.rowDarkMode.setEnabled(true);
            }

            // 2. Lưu cài đặt Dark Mode mới
            sharedPreferences.edit().putBoolean(KEY_DARK_MODE, newMode).apply();

            // 3. Đổi Night Mode -> Activity sẽ recreate ngay đằng sau lớp overlayView
            if (newMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            // Triệt xóa animation chuyển giao diện mặc định
            overridePendingTransition(0, 0);
        };

        binding.rowDarkMode.setOnClickListener(toggleClickListener);
    }

    private void updateDarkModeUI(boolean isDark) {
        if (binding.iconDarkMode != null) {
            if (isDark) {
                binding.iconDarkMode.setImageResource(R.drawable.ic_dark_mode);
                binding.iconDarkMode.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.teal_primary)
                ));
            } else {
                binding.iconDarkMode.setImageResource(R.drawable.ic_sun);
                binding.iconDarkMode.setImageTintList(ColorStateList.valueOf(
                        Color.parseColor("#F59E0B")
                ));
            }
        }
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
                    int created = taoDuLieuMauChoTaiKhoanHienTai();
                    if (created > 0) {
                        sharedPreferences.edit().putBoolean("has_sample_data", true).apply();
                        Toast.makeText(CaiDatActivity.this, "Đã khởi tạo " + created + " chuyến đi mẫu thành công!", Toast.LENGTH_SHORT).show();
                    } else if (created == 0) {
                        Toast.makeText(CaiDatActivity.this, "Dữ liệu mẫu đã có sẵn trong tài khoản của bạn!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private int taoDuLieuMauChoTaiKhoanHienTai() {
        QuanLyPhien phien = new QuanLyPhien(this);
        int userId = phien.getMaNguoiDung();
        if (userId <= 0) {
            Toast.makeText(this, "Vui lòng đăng nhập lại để tạo dữ liệu mẫu!", Toast.LENGTH_SHORT).show();
            return -1;
        }

        ChuyenDiDAO chuyenDiDAO = new ChuyenDiDAO(this);
        LichTrinhDAO lichTrinhDAO = new LichTrinhDAO(this);
        ChiPhiDAO chiPhiDAO = new ChiPhiDAO(this);
        AnhDAO anhDAO = new AnhDAO(this);

        Set<String> existingNames = new HashSet<>();
        List<ChuyenDi> existingTrips = chuyenDiDAO.layDanhSachChuyenDi(userId);
        if (existingTrips != null) {
            for (ChuyenDi cd : existingTrips) {
                if (cd.getTenChuyenDi() != null) {
                    existingNames.add(cd.getTenChuyenDi().trim());
                }
            }
        }

        int createdCount = 0;

        // 1. Đà Lạt (Sắp tới)
        String name1 = "Đà Lạt – Sương Mù & Thảnh Thơi";
        if (!existingNames.contains(name1)) {
            String d1 = ngayLech(5);
            String d2 = ngayLech(6);
            String d3 = ngayLech(7);
            ChuyenDi cd1 = new ChuyenDi(userId, name1, "Đà Lạt, Lâm Đồng", d1, d3, 3500000);
            cd1.setTrangThai("SAP_TOI");
            cd1.setAnhBia("img_dalat");
            long id1 = chuyenDiDAO.themChuyenDi(cd1);
            if (id1 > 0) {
                int ma = (int) id1;
                anhDAO.themAnh(ma, "img_dalat", "Hồ Xuân Hương Đà Lạt", "Cảnh đẹp");
                lichTrinhDAO.themHoatDong(new LichTrinh(ma, d1, "07:30", "Di chuyển lên Đà Lạt", "Bến xe Đà Lạt", null, LichTrinh.LoaiHoatDong.DI_CHUYEN, null, false));
                lichTrinhDAO.themHoatDong(new LichTrinh(ma, d1, "12:00", "Ăn trưa lẩu gà lá é", "Đà Lạt", null, LichTrinh.LoaiHoatDong.AN_UONG, "Đặt bàn trước", false));
                lichTrinhDAO.themHoatDong(new LichTrinh(ma, d2, "08:30", "Săn mây Đồi Chè Cầu Đất", "Cầu Đất, Đà Lạt", null, LichTrinh.LoaiHoatDong.THAM_QUAN, null, false));
                themChiPhiMau(chiPhiDAO, ma, userId, 2, 450000, d1, "Vé xe khứ hồi");
                themChiPhiMau(chiPhiDAO, ma, userId, 3, 1200000, d1, "Homestay 2 đêm");
                themChiPhiMau(chiPhiDAO, ma, userId, 1, 320000, d2, "Ăn uống & cà phê");
                createdCount++;
            }
        }

        // 2. Hà Giang (Đang diễn ra)
        String name2 = "Hà Giang – Chinh Phục Mã Pí Lèng";
        if (!existingNames.contains(name2)) {
            String d1 = ngayLech(0);
            String d2 = ngayLech(1);
            String d3 = ngayLech(2);
            String d4 = ngayLech(3);
            ChuyenDi cd2 = new ChuyenDi(userId, name2, "Đồng Văn, Hà Giang", d1, d4, 4500000);
            cd2.setTrangThai("DANG_DI");
            cd2.setAnhBia("img_hagiang");
            long id2 = chuyenDiDAO.themChuyenDi(cd2);
            if (id2 > 0) {
                int ma = (int) id2;
                anhDAO.themAnh(ma, "img_hagiang", "Đèo Mã Pí Lèng Hà Giang", "Cảnh đẹp");
                lichTrinhDAO.themHoatDong(new LichTrinh(ma, d1, "07:00", "Xuất phát đi TP. Hà Giang", "Cột mốc Km0 Hà Giang", null, LichTrinh.LoaiHoatDong.DI_CHUYEN, null, true));
                lichTrinhDAO.themHoatDong(new LichTrinh(ma, d2, "09:00", "Chinh phục đèo Mã Pí Lèng", "Đèo Mã Pí Lèng, Mèo Vạc", null, LichTrinh.LoaiHoatDong.THAM_QUAN, null, false));
                lichTrinhDAO.themHoatDong(new LichTrinh(ma, d3, "19:00", "Thưởng thức lẩu gà đen", "Phố cổ Đồng Văn", null, LichTrinh.LoaiHoatDong.AN_UONG, null, false));
                themChiPhiMau(chiPhiDAO, ma, userId, 2, 600000, d1, "Thuê xe máy & xăng xe");
                themChiPhiMau(chiPhiDAO, ma, userId, 3, 950000, d1, "Nhà nghỉ phố cổ Đồng Văn");
                themChiPhiMau(chiPhiDAO, ma, userId, 1, 420000, d2, "Ăn uống dọc đường");
                createdCount++;
            }
        }

        // 3. Phú Quốc (Đã hoàn thành)
        String name3 = "Phú Quốc – Kỳ Nghỉ Biển Ngọc";
        if (!existingNames.contains(name3)) {
            String d1 = ngayLech(-10);
            String d2 = ngayLech(-9);
            String d3 = ngayLech(-8);
            String d4 = ngayLech(-7);
            ChuyenDi cd3 = new ChuyenDi(userId, name3, "Phú Quốc, Kiên Giang", d1, d4, 5000000);
            cd3.setTrangThai("DA_XONG");
            cd3.setAnhBia("img_phuquoc");
            long id3 = chuyenDiDAO.themChuyenDi(cd3);
            if (id3 > 0) {
                int ma = (int) id3;
                anhDAO.themAnh(ma, "img_phuquoc", "Bãi Sao Phú Quốc", "Cảnh đẹp");
                lichTrinhDAO.themHoatDong(new LichTrinh(ma, d1, "09:00", "Nhận phòng resort ven biển", "Dương Đông, Phú Quốc", null, LichTrinh.LoaiHoatDong.LUU_TRU, null, true));
                lichTrinhDAO.themHoatDong(new LichTrinh(ma, d2, "10:00", "Lặn ngắm san hô Bãi Sao", "Bãi Sao, Phú Quốc", null, LichTrinh.LoaiHoatDong.THAM_QUAN, null, true));
                lichTrinhDAO.themHoatDong(new LichTrinh(ma, d3, "17:30", "Ngắm hoàng hôn Sunset Sanato", "Bãi Trường, Phú Quốc", null, LichTrinh.LoaiHoatDong.THAM_QUAN, null, true));
                themChiPhiMau(chiPhiDAO, ma, userId, 2, 1650000, d1, "Vé máy bay khứ hồi");
                themChiPhiMau(chiPhiDAO, ma, userId, 3, 1800000, d1, "Phòng resort 3 đêm");
                themChiPhiMau(chiPhiDAO, ma, userId, 4, 550000, d2, "Tour cano 4 đảo");
                createdCount++;
            }
        }

        return createdCount;
    }

    private void themChiPhiMau(ChiPhiDAO dao, int maChuyenDi, int userId, int maDanhMuc, long soTien, String ngayChi, String ghiChu) {
        ChiPhi cp = new ChiPhi(maChuyenDi, maDanhMuc, soTien, ngayChi, ghiChu);
        cp.setMaNguoiDung(userId);
        dao.themChiPhi(cp);
    }

    private String ngayLech(int offsetDays) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, offsetDays);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
    }

    private File getLocalBackupFile() {
        File dir = new File(getFilesDir(), "backups");
        return new File(dir, DBHelper.DATABASE_NAME);
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
        File backupFile = getLocalBackupFile();
        if (lastBackup != null && !lastBackup.isEmpty() && backupFile.exists() && backupFile.length() > 0) {
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
                    File backupFile = getLocalBackupFile();
                    if (DBHelper.backupDatabase(this, backupFile)) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
                        String currentTime = sdf.format(new Date());

                        sharedPreferences.edit().putString(KEY_LAST_BACKUP, currentTime).apply();
                        updateBackupTimeText();

                        Toast.makeText(CaiDatActivity.this, "Sao lưu dữ liệu local thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CaiDatActivity.this, "Sao lưu dữ liệu thất bại!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showRestoreDialog() {
        File backupFile = getLocalBackupFile();
        String lastBackup = sharedPreferences.getString(KEY_LAST_BACKUP, null);
        if (!backupFile.exists() || backupFile.length() == 0) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Phục hồi dữ liệu")
                    .setMessage("Chưa tìm thấy bản sao lưu local nào trên thiết bị. Vui lòng thực hiện sao lưu trước.")
                    .setPositiveButton("Đã hiểu", null)
                    .show();
            return;
        }

        String timeLabel = (lastBackup != null && !lastBackup.isEmpty()) ? (" ngày " + lastBackup) : "";
        new MaterialAlertDialogBuilder(this)
                .setTitle("Phục hồi dữ liệu")
                .setMessage("Bạn có chắc chắn muốn khôi phục dữ liệu từ bản sao lưu" + timeLabel + "? Dữ liệu hiện tại sẽ được thay thế bằng bản sao lưu này.")
                .setPositiveButton("Phục hồi", (dialog, which) -> {
                    if (DBHelper.restoreDatabase(this, backupFile)) {
                        Toast.makeText(CaiDatActivity.this, "Khôi phục dữ liệu từ bản sao lưu thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CaiDatActivity.this, "Khôi phục dữ liệu thất bại!", Toast.LENGTH_SHORT).show();
                    }
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
