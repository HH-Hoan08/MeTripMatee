package com.example.mytripmatee.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;
import com.example.mytripmatee.adapter.AdapterLich;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChonNgayActivity extends AppCompatActivity {

    public static final String EXTRA_RESULT_DATE_RANGE = "extra_result_date_range";
    public static final String EXTRA_RESULT_DURATION = "extra_result_duration";

    private RecyclerView rvCalendarDays;
    private TextView tvMonthYear, tvSelectedRange, tvPreset3N2D, tvPresetWeekend;
    private View btnApplyDate, btnReset;
    private MaterialCardView btnPreset3N2D, btnPresetWeekend;
    private ImageButton btnBack, btnPrevMonth, btnNextMonth;
    private AdapterLich adapter;
    // Các biến điều khiển lịch và ngày chọn
    private Calendar ngayHomNay;             // mốc thời gian thực tế hiện tại để cấm ngày quá khứ
    private Calendar lichDangXem;            // tháng/năm hiện tại đang được hiển thị trên lịch
    private String ngayBatDauChonStr = null;
    private String ngayKetThucChonStr = null;
    private final List<AdapterLich.ItemNgayLich> danhSachNgayLich = new ArrayList<>();
    private final SimpleDateFormat sdfChuan = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chon_ngay);

        // khởi tạo ngày hôm nay làm mốc chặn
        ngayHomNay = Calendar.getInstance();
        ngayHomNay.set(Calendar.HOUR_OF_DAY, 0);
        ngayHomNay.set(Calendar.MINUTE, 0);
        ngayHomNay.set(Calendar.SECOND, 0);
        ngayHomNay.set(Calendar.MILLISECOND, 0);

        // mặc định lịch đang mở tháng hiện tại
        lichDangXem = (Calendar) ngayHomNay.clone();

        // nhận ngày đi và ngày về truyền từ màn hình sửa
        if (getIntent() != null) {
            String ngayDiTruyenVao = getIntent().getStringExtra("ngayDi");
            String ngayVeTruyenVao = getIntent().getStringExtra("ngayVe");
            if (ngayDiTruyenVao != null && ngayDiTruyenVao.length() == 10) {
                ngayBatDauChonStr = ngayDiTruyenVao;
                if (ngayVeTruyenVao != null && ngayVeTruyenVao.length() == 10
                        && ngayVeTruyenVao.compareTo(ngayDiTruyenVao) >= 0) {
                    ngayKetThucChonStr = ngayVeTruyenVao;
                }
                try {
                    Date ngayDiDate = sdfChuan.parse(ngayDiTruyenVao);
                    // nếu chuyến đi ở tháng/năm sau, tự động mở ngay lịch tương ứng
                    if (ngayDiDate != null && ngayDiDate.after(ngayHomNay.getTime())) {
                        lichDangXem.setTime(ngayDiDate);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        initViews();
        caiDatRecyclerViewLich();
        capNhatNhanPreset();
        caiDatSuKienNutBam();
        capNhatLichHienThi();
        capNhatChuTomTat();
        capNhatTrangThaiPreset(0);
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
        tvPreset3N2D = findViewById(R.id.tvPreset3N2D);
        tvPresetWeekend = findViewById(R.id.tvPresetWeekend);
    }

    private void caiDatRecyclerViewLich() {
        rvCalendarDays.setLayoutManager(new GridLayoutManager(this, 7));
        adapter = new AdapterLich(danhSachNgayLich, this::xuLyChonNgay);
        rvCalendarDays.setAdapter(adapter);
    }

    private Calendar[] tinhKhoang3N2D() {
        Calendar batDau = (Calendar) ngayHomNay.clone();
        Calendar ketThuc = (Calendar) ngayHomNay.clone();
        ketThuc.add(Calendar.DAY_OF_MONTH, 2);
        return new Calendar[]{batDau, ketThuc};
    }

    private Calendar[] tinhKhoangCuoiTuan() {
        Calendar batDau = (Calendar) ngayHomNay.clone();
        Calendar ketThuc = (Calendar) ngayHomNay.clone();
        int thu = ngayHomNay.get(Calendar.DAY_OF_WEEK);
        if (thu == Calendar.SATURDAY) {
            // Hôm nay là Thứ 7 -> Cuối tuần là Thứ 7 - Chủ nhật
            ketThuc.add(Calendar.DAY_OF_MONTH, 1);
        } else if (thu == Calendar.SUNDAY) {
            // Hôm nay là Chủ nhật -> Cuối tuần tiếp theo là Thứ 6 - Chủ nhật tuần tới
            batDau.add(Calendar.DAY_OF_MONTH, 5);
            ketThuc.add(Calendar.DAY_OF_MONTH, 7);
        } else {
            // Thứ 2..Thứ 6 -> Thứ 6 đến Chủ nhật tuần này
            int soNgayDenThuSau = Calendar.FRIDAY - thu;
            batDau.add(Calendar.DAY_OF_MONTH, soNgayDenThuSau);
            ketThuc.add(Calendar.DAY_OF_MONTH, soNgayDenThuSau + 2);
        }
        return new Calendar[]{batDau, ketThuc};
    }

    private String dinhDangNhanKhoang(Calendar batDau, Calendar ketThuc) {
        int d1 = batDau.get(Calendar.DAY_OF_MONTH);
        int m1 = batDau.get(Calendar.MONTH) + 1;
        int d2 = ketThuc.get(Calendar.DAY_OF_MONTH);
        int m2 = ketThuc.get(Calendar.MONTH) + 1;
        if (m1 == m2 && batDau.get(Calendar.YEAR) == ketThuc.get(Calendar.YEAR)) {
            return String.format(Locale.getDefault(), "%d-%d/%d", d1, d2, m1);
        }
        return String.format(Locale.getDefault(), "%d/%d-%d/%d", d1, m1, d2, m2);
    }

    private void capNhatNhanPreset() {
        if (tvPreset3N2D != null) {
            Calendar[] khoang3N2D = tinhKhoang3N2D();
            tvPreset3N2D.setText("⚡ 3N2Đ (" + dinhDangNhanKhoang(khoang3N2D[0], khoang3N2D[1]) + ")");
        }
        if (tvPresetWeekend != null) {
            Calendar[] khoangCuoiTuan = tinhKhoangCuoiTuan();
            tvPresetWeekend.setText("🌅 Cuối tuần (" + dinhDangNhanKhoang(khoangCuoiTuan[0], khoangCuoiTuan[1]) + ")");
        }
    }

    private void apDungPreset(Calendar[] khoang, int presetIndex) {
        ngayBatDauChonStr = sdfChuan.format(khoang[0].getTime());
        ngayKetThucChonStr = sdfChuan.format(khoang[1].getTime());

        // Chuyển lịch hiển thị tới tháng của ngày bắt đầu để thấy vùng chọn
        lichDangXem.set(Calendar.YEAR, khoang[0].get(Calendar.YEAR));
        lichDangXem.set(Calendar.MONTH, khoang[0].get(Calendar.MONTH));

        capNhatLichHienThi();
        capNhatChuTomTat();
        capNhatTrangThaiPreset(presetIndex);
    }

    private void capNhatTrangThaiPreset(int activeIndex) {
        int activeColor = ContextCompat.getColor(this, R.color.teal_primary);
        int inactiveStroke = ContextCompat.getColor(this, R.color.border_color);
        int inactiveText = ContextCompat.getColor(this, R.color.text_primary);

        if (btnPreset3N2D != null && tvPreset3N2D != null) {
            boolean active = (activeIndex == 1);
            btnPreset3N2D.setStrokeColor(active ? activeColor : inactiveStroke);
            tvPreset3N2D.setTextColor(active ? activeColor : inactiveText);
            tvPreset3N2D.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        }
        if (btnPresetWeekend != null && tvPresetWeekend != null) {
            boolean active = (activeIndex == 2);
            btnPresetWeekend.setStrokeColor(active ? activeColor : inactiveStroke);
            tvPresetWeekend.setTextColor(active ? activeColor : inactiveText);
            tvPresetWeekend.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void capNhatLichHienThi() {
        danhSachNgayLich.clear();
        int thangHienThi = lichDangXem.get(Calendar.MONTH); // 0 - 11
        int namHienThi = lichDangXem.get(Calendar.YEAR);
        tvMonthYear.setText(String.format(Locale.getDefault(), "Tháng %d, %d", thangHienThi + 1, namHienThi));

        // nếu tháng/năm đang xem <= tháng/năm hiện tại thì KHÓA nút mũi tên lùi (<)
        boolean laThangHienTaiHoacQuaKhu = (namHienThi < ngayHomNay.get(Calendar.YEAR)) ||
                (namHienThi == ngayHomNay.get(Calendar.YEAR) && thangHienThi <= ngayHomNay.get(Calendar.MONTH));
        if (btnPrevMonth != null) {
            btnPrevMonth.setEnabled(!laThangHienTaiHoacQuaKhu);
            btnPrevMonth.setAlpha(laThangHienTaiHoacQuaKhu ? 0.25f : 1.0f);
        }

        // tính toán thứ của ngày mùng 1
        Calendar lichTam = (Calendar) lichDangXem.clone();
        lichTam.set(Calendar.DAY_OF_MONTH, 1);
        int thuTrongTuan = lichTam.get(Calendar.DAY_OF_WEEK);
        // tính số ngày đệm phía trước (theo chuẩn Thứ 2 đầu tuần)
        int soNgayDemPhiaTruoc = (thuTrongTuan == Calendar.SUNDAY) ? 6 : (thuTrongTuan - Calendar.MONDAY);
        // thêm các ngày đệm của tháng trước (làm mờ)
        Calendar lichThangTruoc = (Calendar) lichDangXem.clone();
        lichThangTruoc.add(Calendar.MONTH, -1);
        int soNgayThangTruoc = lichThangTruoc.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = soNgayDemPhiaTruoc - 1; i >= 0; i--) {
            danhSachNgayLich.add(new AdapterLich.ItemNgayLich(
                    soNgayThangTruoc - i,
                    lichThangTruoc.get(Calendar.MONTH),
                    lichThangTruoc.get(Calendar.YEAR),
                    false, true
            ));
        }
        // thêm các ngày của tháng hiện tại
        int soNgayTrongThang = lichDangXem.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int ngay = 1; ngay <= soNgayTrongThang; ngay++) {
            Calendar lichKiemTra = (Calendar) lichDangXem.clone();
            lichKiemTra.set(Calendar.DAY_OF_MONTH, ngay);
            lichKiemTra.set(Calendar.HOUR_OF_DAY, 0);
            lichKiemTra.set(Calendar.MINUTE, 0);
            lichKiemTra.set(Calendar.SECOND, 0);
            lichKiemTra.set(Calendar.MILLISECOND, 0);
            // đánh dấu là đã qua nếu ngày này < ngayHomNay
            boolean daQua = lichKiemTra.before(ngayHomNay);
            danhSachNgayLich.add(new AdapterLich.ItemNgayLich(
                    ngay, thangHienThi, namHienThi, true, daQua
            ));
        }
        if (adapter != null) {
            adapter.setKhoangNgayChon(ngayBatDauChonStr, ngayKetThucChonStr);
            adapter.capNhatDanhSach(danhSachNgayLich);
        }
    }

    private void caiDatSuKienNutBam() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnPreset3N2D != null) {
            btnPreset3N2D.setOnClickListener(v -> apDungPreset(tinhKhoang3N2D(), 1));
        }

        if (btnPresetWeekend != null) {
            btnPresetWeekend.setOnClickListener(v -> apDungPreset(tinhKhoangCuoiTuan(), 2));
        }

        // nút sang tháng sau (>) -> tự do sang Tháng 12/2026, 2027, 2028...
        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                lichDangXem.add(Calendar.MONTH, 1);
                capNhatLichHienThi();
            });
        }

        // nút lùi về tháng trước (<) -> bị cấm nếu đang ở tháng hiện tại
        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                lichDangXem.add(Calendar.MONTH, -1);
                capNhatLichHienThi();
            });
        }

        // bấm "Tháng 11, 2026" để nhảy nhanh sang năm 2027
        if (tvMonthYear != null) {
            tvMonthYear.setOnClickListener(v -> hienThiChonNhanhNamThang());
        }

        // nút Đặt lại
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                ngayBatDauChonStr = null;
                ngayKetThucChonStr = null;
                if (adapter != null) adapter.setKhoangNgayChon(null, null);
                capNhatChuTomTat();
                capNhatTrangThaiPreset(0);
            });
        }

        // nút Áp dụng ✓
        if (btnApplyDate != null) {
            btnApplyDate.setOnClickListener(v -> xacNhanVaDong());
        }
    }

    // hộp thoại chọn nhanh năm 2026, 2027, 2028
    private void hienThiChonNhanhNamThang() {
        String[] danhSachNam = {"2026", "2027", "2028"};
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Chọn năm khởi hành")
                .setItems(danhSachNam, (dialog, vitri) -> {
                    int namDaChon = Integer.parseInt(danhSachNam[vitri]);
                    lichDangXem.set(Calendar.YEAR, namDaChon);
                    // nếu chọn năm hiện tại thì không được để tháng nhỏ hơn tháng hiện tại
                    if (namDaChon == ngayHomNay.get(Calendar.YEAR) && lichDangXem.get(Calendar.MONTH) < ngayHomNay.get(Calendar.MONTH)) {
                        lichDangXem.set(Calendar.MONTH, ngayHomNay.get(Calendar.MONTH));
                    }
                    capNhatLichHienThi();
                })
                .show();
    }

    private void xuLyChonNgay(AdapterLich.ItemNgayLich itemNgay) {
        if (itemNgay.daQua || !itemNgay.laThangHienTai) return;

        String chuoiNgayChon = itemNgay.layChuoiNgayChuan();

        if (ngayBatDauChonStr == null) {
            ngayBatDauChonStr = chuoiNgayChon;
            ngayKetThucChonStr = null;
        } else if (ngayKetThucChonStr == null) {
            if (chuoiNgayChon.compareTo(ngayBatDauChonStr) >= 0) {
                ngayKetThucChonStr = chuoiNgayChon;
            } else {
                // nếu bấm ngày trước ngày bắt đầu thì lấy ngày này làm ngày bắt đầu mới
                ngayBatDauChonStr = chuoiNgayChon;
                ngayKetThucChonStr = null;
            }
        } else {
            // nếu đã chọn cả 2 ngày rồi thì bấm lại để chọn đợt mới
            ngayBatDauChonStr = chuoiNgayChon;
            ngayKetThucChonStr = null;
        }

        if (adapter != null) {
            adapter.setKhoangNgayChon(ngayBatDauChonStr, ngayKetThucChonStr);
        }
        capNhatChuTomTat();
        capNhatTrangThaiPreset(0);
    }

    private void capNhatChuTomTat() {
        if (tvSelectedRange == null) return;

        if (ngayBatDauChonStr != null && ngayKetThucChonStr != null) {
            tvSelectedRange.setText("Thời gian đã chọn:\n" + ngayBatDauChonStr + " → " + ngayKetThucChonStr);
        } else if (ngayBatDauChonStr != null) {
            tvSelectedRange.setText("Đang chọn ngày về (từ ngày " + ngayBatDauChonStr + ")...");
        } else {
            tvSelectedRange.setText("Chưa chọn ngày");
        }
    }

    private void xacNhanVaDong() {
        if (ngayBatDauChonStr == null) {
            Toast.makeText(this, "Vui lòng chọn ngày khởi hành", Toast.LENGTH_SHORT).show();
            return;
        }

        String ngayDi = ngayBatDauChonStr;
        String ngayVe = (ngayKetThucChonStr != null) ? ngayKetThucChonStr : ngayBatDauChonStr;

        Intent ketQuaIntent = new Intent();
        ketQuaIntent.putExtra("extra_ngay_di", ngayDi); // yyyy-MM-dd
        ketQuaIntent.putExtra("extra_ngay_ve", ngayVe); // yyyy-MM-dd
        ketQuaIntent.putExtra(EXTRA_RESULT_DATE_RANGE, ngayDi + " – " + ngayVe);

        setResult(RESULT_OK, ketQuaIntent);
        finish();
    }
}
