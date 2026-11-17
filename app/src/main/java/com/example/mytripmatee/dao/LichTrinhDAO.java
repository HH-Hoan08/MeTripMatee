package com.example.mytripmatee.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mytripmatee.database.DBHelper;
import com.example.mytripmatee.model.LichTrinh;

import java.util.ArrayList;
import java.util.List;

public class LichTrinhDAO {
    private static final String TAG = "LichTrinhDAO";
    private final DBHelper dbHelper;

    public LichTrinhDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public LichTrinhDAO(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    //Thêm hoạt động mới cho chuyến đi
    public long themHoatDong(LichTrinh item) {
        if (item == null || item.getMaChuyenDi() <= 0 || item.getTieuDe() == null || item.getTieuDe().trim().isEmpty()) {
            Log.e(TAG, "Dữ liệu hoạt động không hợp lệ");
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MaChuyenDi", item.getMaChuyenDi());
        values.put("NgayHoatDong", item.getNgayHoatDong());
        values.put("GioBatDau", item.getGioBatDau());
        values.put("TieuDe", item.getTieuDe().trim());
        values.put("DiaDiem", item.getDiaDiem() != null ? item.getDiaDiem().trim() : null);
        values.put("LinkBanDo", item.getLinkBanDo() != null ? item.getLinkBanDo().trim() : null);
        values.put("LoaiHoatDong", item.getLoaiHoatDong() != null ? item.getLoaiHoatDong().getCode() : "THAM_QUAN");
        values.put("GhiChu", item.getGhiChu() != null ? item.getGhiChu().trim() : null);
        values.put("DaDi", item.isDaDi() ? 1 : 0);

        try {
            return db.insert("LichTrinh", null, values);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi thêm hoạt động (có thể vi phạm trigger kiểm tra ngày chuyến đi)", e);
            return -1;
        }
    }

    //Cập nhật thông tin hoạt động
    public boolean capNhatHoatDong(LichTrinh item) {
        if (item == null || item.getMaHoatDong() <= 0) return false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NgayHoatDong", item.getNgayHoatDong());
        values.put("GioBatDau", item.getGioBatDau());
        values.put("TieuDe", item.getTieuDe().trim());
        values.put("DiaDiem", item.getDiaDiem() != null ? item.getDiaDiem().trim() : null);
        values.put("LinkBanDo", item.getLinkBanDo() != null ? item.getLinkBanDo().trim() : null);
        values.put("LoaiHoatDong", item.getLoaiHoatDong() != null ? item.getLoaiHoatDong().getCode() : "THAM_QUAN");
        values.put("GhiChu", item.getGhiChu() != null ? item.getGhiChu().trim() : null);
        values.put("DaDi", item.isDaDi() ? 1 : 0);

        try {
            int rows = db.update("LichTrinh", values, "MaHoatDong = ?",
                    new String[]{String.valueOf(item.getMaHoatDong())});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi cập nhật hoạt động", e);
            return false;
        }
    }

    //Cập nhật trạng thái Đã đi / Chưa đi
    public boolean capNhatTrangThaiDaDi(int maHoatDong, boolean daDi) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("DaDi", daDi ? 1 : 0);
        try {
            int rows = db.update("LichTrinh", values, "MaHoatDong = ?",
                    new String[]{String.valueOf(maHoatDong)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi cập nhật trạng thái đã đi", e);
            return false;
        }
    }

    //Xóa hoạt động an toàn
    public boolean xoaHoatDong(int maHoatDong) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rows = db.delete("LichTrinh", "MaHoatDong = ?",
                    new String[]{String.valueOf(maHoatDong)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xóa hoạt động", e);
            return false;
        }
    }

    //Lấy danh sách tất cả hoạt động của chuyến đi, tự động sắp xếp Ngày/Giờ tăng dần
    public List<LichTrinh> layDanhSachHoatDongTheoChuyenDi(int maChuyenDi) {
        List<LichTrinh> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM LichTrinh WHERE MaChuyenDi = ? " +
                "ORDER BY NgayHoatDong ASC, GioBatDau ASC, MaHoatDong ASC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maChuyenDi)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(parseLichTrinhFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lấy danh sách hoạt động theo chuyến đi", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    //Lấy danh sách hoạt động theo từng Ngày cụ thể
    public List<LichTrinh> layDanhSachHoatDongTheoNgay(int maChuyenDi, String ngayHoatDong) {
        List<LichTrinh> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM LichTrinh WHERE MaChuyenDi = ? AND NgayHoatDong = ? " +
                "ORDER BY GioBatDau ASC, MaHoatDong ASC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maChuyenDi), ngayHoatDong});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(parseLichTrinhFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lấy lịch trình theo ngày", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // Helper map Cursor -> Object LichTrinh
    private LichTrinh parseLichTrinhFromCursor(Cursor cursor) {
        LichTrinh item = new LichTrinh();
        item.setMaHoatDong(cursor.getInt(cursor.getColumnIndexOrThrow("MaHoatDong")));
        item.setMaChuyenDi(cursor.getInt(cursor.getColumnIndexOrThrow("MaChuyenDi")));
        item.setNgayHoatDong(cursor.getString(cursor.getColumnIndexOrThrow("NgayHoatDong")));
        item.setGioBatDau(cursor.getString(cursor.getColumnIndexOrThrow("GioBatDau")));
        item.setTieuDe(cursor.getString(cursor.getColumnIndexOrThrow("TieuDe")));
        item.setDiaDiem(cursor.getString(cursor.getColumnIndexOrThrow("DiaDiem")));
        item.setLinkBanDo(cursor.getString(cursor.getColumnIndexOrThrow("LinkBanDo")));
        item.setLoaiHoatDong(LichTrinh.LoaiHoatDong.fromCode(
                cursor.getString(cursor.getColumnIndexOrThrow("LoaiHoatDong"))));
        item.setGhiChu(cursor.getString(cursor.getColumnIndexOrThrow("GhiChu")));
        item.setDaDi(cursor.getInt(cursor.getColumnIndexOrThrow("DaDi")) == 1);
        return item;
    }
}