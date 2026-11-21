package com.example.mytripmatee.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mytripmatee.database.DBHelper;
import com.example.mytripmatee.model.ChiPhi;
import com.example.mytripmatee.model.DanhMucThongKe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChiPhiDAO {
    private static final String TAG = "ChiPhiDAO";
    private final DBHelper dbHelper;

    public ChiPhiDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    // ==========================================
    //  CRUD Chi phí (Số tiền > 0)
    // ==========================================

    // 1. Thêm khoản chi mới (Bắt buộc số tiền > 0)
    public long themChiPhi(ChiPhi chiPhi) {
        if (chiPhi == null || chiPhi.getSoTien() <= 0) {
            Log.e(TAG, "Số tiền chi phí phải lớn hơn 0");
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MaChuyenDi", chiPhi.getMaChuyenDi());
        values.put("MaNguoiDung", chiPhi.getMaNguoiDung() > 0 ? chiPhi.getMaNguoiDung() : 1);
        values.put("MaDanhMuc", chiPhi.getMaDanhMuc());
        values.put("SoTien", chiPhi.getSoTien());
        values.put("NgayChi", chiPhi.getNgayChi());
        values.put("GhiChu", chiPhi.getGhiChu());
        values.put("LaCaNhan", chiPhi.getLaCaNhan());

        try {
            return db.insert("ChiPhi", null, values);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi thêm chi phí", e);
            return -1;
        }
    }

    // 2. Cập nhật khoản chi (Bắt buộc số tiền > 0)
    public boolean capNhatChiPhi(ChiPhi chiPhi) {
        if (chiPhi == null || chiPhi.getMaChiPhi() <= 0 || chiPhi.getSoTien() <= 0) {
            Log.e(TAG, "Số tiền không hợp lệ hoặc dữ liệu sai");
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MaNguoiDung", chiPhi.getMaNguoiDung() > 0 ? chiPhi.getMaNguoiDung() : 1);
        values.put("MaDanhMuc", chiPhi.getMaDanhMuc());
        values.put("SoTien", chiPhi.getSoTien());
        values.put("NgayChi", chiPhi.getNgayChi());
        values.put("GhiChu", chiPhi.getGhiChu());
        values.put("LaCaNhan", chiPhi.getLaCaNhan());

        try {
            int rows = db.update("ChiPhi", values, "MaChiPhi = ?",
                    new String[]{String.valueOf(chiPhi.getMaChiPhi())});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi cập nhật chi phí", e);
            return false;
        }
    }

    // 3. Xóa khoản chi (Xóa chi phí cập nhật ngay)
    public boolean xoaChiPhi(int maChiPhi) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rows = db.delete("ChiPhi", "MaChiPhi = ?",
                    new String[]{String.valueOf(maChiPhi)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xóa chi phí", e);
            return false;
        }
    }

    // 4. Lấy danh sách chi phí theo Chuyến đi & Người dùng
    // (Chi phí cá nhân chỉ hiện với đúng người tạo, chi phí nhóm hiện cho tất cả)
    public List<ChiPhi> layDanhSachChiPhiTheoChuyenDi(int maChuyenDi, int maNguoiDung) {
        List<ChiPhi> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT p.*, d.TenDanhMuc, d.MauSac " +
                "FROM ChiPhi p " +
                "JOIN DanhMuc d ON p.MaDanhMuc = d.MaDanhMuc " +
                "WHERE p.MaChuyenDi = ? " +
                "  AND (p.LaCaNhan = 0 OR p.MaNguoiDung = ?) " +
                "ORDER BY p.NgayChi DESC, p.MaChiPhi DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maChuyenDi), String.valueOf(maNguoiDung)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ChiPhi item = new ChiPhi();
                    item.setMaChiPhi(cursor.getInt(cursor.getColumnIndexOrThrow("MaChiPhi")));
                    item.setMaChuyenDi(cursor.getInt(cursor.getColumnIndexOrThrow("MaChuyenDi")));
                    try {
                        item.setMaNguoiDung(cursor.getInt(cursor.getColumnIndexOrThrow("MaNguoiDung")));
                    } catch (Exception ignored) {}
                    item.setMaDanhMuc(cursor.getInt(cursor.getColumnIndexOrThrow("MaDanhMuc")));
                    item.setSoTien(cursor.getLong(cursor.getColumnIndexOrThrow("SoTien")));
                    item.setNgayChi(cursor.getString(cursor.getColumnIndexOrThrow("NgayChi")));
                    item.setGhiChu(cursor.getString(cursor.getColumnIndexOrThrow("GhiChu")));
                    try {
                        item.setLaCaNhan(cursor.getInt(cursor.getColumnIndexOrThrow("LaCaNhan")));
                    } catch (Exception ignored) {}
                    item.setTenDanhMuc(cursor.getString(cursor.getColumnIndexOrThrow("TenDanhMuc")));
                    item.setMauSac(cursor.getString(cursor.getColumnIndexOrThrow("MauSac")));
                    list.add(item);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public List<ChiPhi> layDanhSachChiPhiTheoChuyenDi(int maChuyenDi) {
        return layDanhSachChiPhiTheoChuyenDi(maChuyenDi, 1);
    }

    // 4b. Lấy danh sách chi phí NHÓM / LỊCH TRÌNH (LaCaNhan = 0) của một chuyến đi
    public List<ChiPhi> layDanhSachChiPhiNhomTheoChuyenDi(int maChuyenDi) {
        List<ChiPhi> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT p.*, d.TenDanhMuc, d.MauSac " +
                "FROM ChiPhi p " +
                "JOIN DanhMuc d ON p.MaDanhMuc = d.MaDanhMuc " +
                "WHERE p.MaChuyenDi = ? AND p.LaCaNhan = 0 " +
                "ORDER BY p.NgayChi DESC, p.MaChiPhi DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maChuyenDi)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ChiPhi item = new ChiPhi();
                    item.setMaChiPhi(cursor.getInt(cursor.getColumnIndexOrThrow("MaChiPhi")));
                    item.setMaChuyenDi(cursor.getInt(cursor.getColumnIndexOrThrow("MaChuyenDi")));
                    try {
                        item.setMaNguoiDung(cursor.getInt(cursor.getColumnIndexOrThrow("MaNguoiDung")));
                    } catch (Exception ignored) {}
                    item.setMaDanhMuc(cursor.getInt(cursor.getColumnIndexOrThrow("MaDanhMuc")));
                    item.setSoTien(cursor.getLong(cursor.getColumnIndexOrThrow("SoTien")));
                    item.setNgayChi(cursor.getString(cursor.getColumnIndexOrThrow("NgayChi")));
                    item.setGhiChu(cursor.getString(cursor.getColumnIndexOrThrow("GhiChu")));
                    item.setLaCaNhan(0);
                    item.setTenDanhMuc(cursor.getString(cursor.getColumnIndexOrThrow("TenDanhMuc")));
                    item.setMauSac(cursor.getString(cursor.getColumnIndexOrThrow("MauSac")));
                    list.add(item);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // 5. Tính tổng chi tiêu của một chuyến đi
    public long tinhTongChiTheoChuyenDi(int maChuyenDi) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT COALESCE(SUM(SoTien), 0) FROM ChiPhi WHERE MaChuyenDi = ?";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maChuyenDi)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    // 5b. Tính tổng chi tiêu NHÓM / LỊCH TRÌNH (LaCaNhan = 0) của một chuyến đi
    public long tinhTongChiNhomTheoChuyenDi(int maChuyenDi) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT COALESCE(SUM(SoTien), 0) FROM ChiPhi WHERE MaChuyenDi = ? AND LaCaNhan = 0";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maChuyenDi)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    // ==============================================================
    //  Truy vấn thống kê SQLite (SUM, GROUP BY)
    // ==============================================================

    // Thống kê theo Danh mục (SUM, GROUP BY danh mục)
    public List<DanhMucThongKe> thongKeChiTieuTheoDanhMuc(int maChuyenDi) {
        List<DanhMucThongKe> list = new ArrayList<>();
        long tongChiTatCa = tinhTongChiTheoChuyenDi(maChuyenDi);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT d.TenDanhMuc, d.MauSac, SUM(p.SoTien) AS TongTien " +
                "FROM ChiPhi p " +
                "JOIN DanhMuc d ON p.MaDanhMuc = d.MaDanhMuc " +
                "WHERE p.MaChuyenDi = ? " +
                "GROUP BY d.MaDanhMuc, d.TenDanhMuc, d.MauSac " +
                "ORDER BY TongTien DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maChuyenDi)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String tenDM = cursor.getString(cursor.getColumnIndexOrThrow("TenDanhMuc"));
                    String mauSac = cursor.getString(cursor.getColumnIndexOrThrow("MauSac"));
                    long tongTienDM = cursor.getLong(cursor.getColumnIndexOrThrow("TongTien"));
                    double tyLe = (tongChiTatCa > 0) ? (tongTienDM * 100.0 / tongChiTatCa) : 0;

                    list.add(new DanhMucThongKe(tenDM, mauSac, tongTienDM, tyLe));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // Thống kê theo Ngày (SUM, GROUP BY theo ngày và lọc động)
    public Map<String, Long> thongKeChiTieuTheoNgay(int maChuyenDi, String tuNgay, String denNgay) {
        Map<String, Long> mapKetQua = new LinkedHashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT NgayChi, SUM(SoTien) AS TongTien " +
                "FROM ChiPhi " +
                "WHERE MaChuyenDi = ? AND NgayChi BETWEEN ? AND ? " +
                "GROUP BY NgayChi " +
                "ORDER BY NgayChi ASC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maChuyenDi), tuNgay, denNgay});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String ngay = cursor.getString(cursor.getColumnIndexOrThrow("NgayChi"));
                    long tongTien = cursor.getLong(cursor.getColumnIndexOrThrow("TongTien"));
                    mapKetQua.put(ngay, tongTien);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return mapKetQua;
    }
}