package com.example.mytripmatee.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mytripmatee.database.DBHelper;
import com.example.mytripmatee.model.ThongBao;

import java.util.ArrayList;
import java.util.List;

public class ThongBaoDAO {
    private static final String TAG = "ThongBaoDAO";
    private final DBHelper dbHelper;

    public ThongBaoDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public long themThongBao(ThongBao tb) {
        if (tb == null) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Avoid inserting duplicate active invitations for the same user and trip
        if ("MOI_THAM_GIA".equals(tb.getLoaiThongBao())) {
            Cursor cursor = db.rawQuery(
                    "SELECT MaThongBao FROM ThongBao WHERE MaNguoiDung = ? AND MaChuyenDi = ? AND LoaiThongBao = 'MOI_THAM_GIA' AND TrangThai = 0",
                    new String[]{String.valueOf(tb.getMaNguoiDung()), String.valueOf(tb.getMaChuyenDi())}
            );
            if (cursor != null) {
                boolean exists = cursor.moveToFirst();
                cursor.close();
                if (exists) return 0; // Already has pending invitation
            }
        }

        ContentValues values = new ContentValues();
        values.put("MaNguoiDung", tb.getMaNguoiDung());
        values.put("MaNguoiGui", tb.getMaNguoiGui() > 0 ? tb.getMaNguoiGui() : 1);
        values.put("MaChuyenDi", tb.getMaChuyenDi());
        values.put("TieuDe", tb.getTieuDe());
        values.put("NoiDung", tb.getNoiDung());
        values.put("LoaiThongBao", tb.getLoaiThongBao());
        values.put("TrangThai", tb.getTrangThai());
        values.put("NgayTao", tb.getNgayTao() != null ? tb.getNgayTao() : "");

        try {
            return db.insert("ThongBao", null, values);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi thêm thông báo", e);
            return -1;
        }
    }

    public List<ThongBao> layDanhSachThongBaoTheoNguoiDung(int maNguoiDung) {
        List<ThongBao> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT t.*, u.HoTen AS TenNguoiGui, c.TenChuyenDi " +
                "FROM ThongBao t " +
                "LEFT JOIN NguoiDung u ON t.MaNguoiGui = u.MaNguoiDung " +
                "LEFT JOIN ChuyenDi c ON t.MaChuyenDi = c.MaChuyenDi " +
                "WHERE t.MaNguoiDung = ? " +
                "ORDER BY t.MaThongBao DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maNguoiDung)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ThongBao tb = new ThongBao();
                    tb.setMaThongBao(cursor.getInt(cursor.getColumnIndexOrThrow("MaThongBao")));
                    tb.setMaNguoiDung(cursor.getInt(cursor.getColumnIndexOrThrow("MaNguoiDung")));
                    tb.setMaNguoiGui(cursor.getInt(cursor.getColumnIndexOrThrow("MaNguoiGui")));
                    tb.setMaChuyenDi(cursor.getInt(cursor.getColumnIndexOrThrow("MaChuyenDi")));
                    tb.setTieuDe(cursor.getString(cursor.getColumnIndexOrThrow("TieuDe")));
                    tb.setNoiDung(cursor.getString(cursor.getColumnIndexOrThrow("NoiDung")));
                    tb.setLoaiThongBao(cursor.getString(cursor.getColumnIndexOrThrow("LoaiThongBao")));
                    tb.setTrangThai(cursor.getInt(cursor.getColumnIndexOrThrow("TrangThai")));
                    tb.setNgayTao(cursor.getString(cursor.getColumnIndexOrThrow("NgayTao")));
                    tb.setTenNguoiGui(cursor.getString(cursor.getColumnIndexOrThrow("TenNguoiGui")));
                    tb.setTenChuyenDi(cursor.getString(cursor.getColumnIndexOrThrow("TenChuyenDi")));

                    list.add(tb);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi lấy danh sách thông báo", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public int laySoThongBaoChuaDoc(int maNguoiDung) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM ThongBao WHERE MaNguoiDung = ? AND TrangThai = 0";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maNguoiDung)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đếm thông báo chưa đọc", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    public boolean capNhatTrangThaiThongBao(int maThongBao, int trangThai) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TrangThai", trangThai);
        try {
            int rows = db.update("ThongBao", values, "MaThongBao = ?", new String[]{String.valueOf(maThongBao)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi cập nhật trạng thái thông báo", e);
            return false;
        }
    }

    public boolean xoaThongBao(int maThongBao) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rows = db.delete("ThongBao", "MaThongBao = ?", new String[]{String.valueOf(maThongBao)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi xóa thông báo", e);
            return false;
        }
    }

    private String layThoiGianHienTai() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    public void guiLoiMoiThamGia(int maNguoiGui, String tenNguoiGui, int maNguoiNhan, int maChuyenDi, String tenChuyenDi) {
        if (maNguoiNhan == maNguoiGui || maNguoiNhan <= 0) return;
        String sender = (tenNguoiGui != null && !tenNguoiGui.isEmpty()) ? tenNguoiGui : "Một thành viên";
        String tieuDe = "Lời mời tham gia chuyến đi";
        String noiDung = sender + " đã mời bạn tham gia chuyến đi '" + tenChuyenDi + "'.";
        ThongBao tb = new ThongBao(maNguoiNhan, maNguoiGui, maChuyenDi, tieuDe, noiDung, "MOI_THAM_GIA", layThoiGianHienTai());
        themThongBao(tb);
    }

    public void guiThongBaoCapNhatChuyenDi(int maNguoiGui, String tenNguoiGui, List<Integer> danhSachThanhVien, int maChuyenDi, String tenChuyenDi) {
        if (danhSachThanhVien == null || danhSachThanhVien.isEmpty()) return;
        String sender = (tenNguoiGui != null && !tenNguoiGui.isEmpty()) ? tenNguoiGui : "Thành viên nhóm";
        String tieuDe = "Cập nhật chuyến đi";
        String noiDung = sender + " đã cập nhật thông tin chuyến đi '" + tenChuyenDi + "'.";
        String now = layThoiGianHienTai();

        for (int mId : danhSachThanhVien) {
            if (mId != maNguoiGui && mId > 0) {
                ThongBao tb = new ThongBao(mId, maNguoiGui, maChuyenDi, tieuDe, noiDung, "CAP_NHAT_CHUYEN_DI", now);
                themThongBao(tb);
            }
        }
    }

    public void guiThongBaoCapNhatLichTrinh(int maNguoiGui, String tenNguoiGui, List<Integer> danhSachThanhVien, int maChuyenDi, String tenChuyenDi, String tenHoatDong, String hanhDong) {
        if (danhSachThanhVien == null || danhSachThanhVien.isEmpty()) return;
        String sender = (tenNguoiGui != null && !tenNguoiGui.isEmpty()) ? tenNguoiGui : "Thành viên nhóm";
        String tieuDe = "Thay đổi lịch trình chuyến đi";
        String noiDung = sender + " đã " + hanhDong + " hoạt động '" + tenHoatDong + "' trong chuyến đi '" + tenChuyenDi + "'.";
        String now = layThoiGianHienTai();

        for (int mId : danhSachThanhVien) {
            if (mId != maNguoiGui && mId > 0) {
                ThongBao tb = new ThongBao(mId, maNguoiGui, maChuyenDi, tieuDe, noiDung, "CAP_NHAT_LICH_TRINH", now);
                themThongBao(tb);
            }
        }
    }
}
