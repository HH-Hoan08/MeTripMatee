package com.example.mytripmatee.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mytripmatee.model.ChuyenDi;

import java.util.ArrayList;
import java.util.List;

public class ChuyenDiDAO {
    private static final String TAG = "ChuyenDiDAO";
    private final DBHelper dbHelper;

    public ChuyenDiDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }
    // thêm chuyến đi mới
    public long themChuyenDi(ChuyenDi chuyenDi) {
        if (chuyenDi == null) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MaNguoiDung", chuyenDi.getMaNguoiDung());
        values.put("TenChuyenDi", chuyenDi.getTenChuyenDi().trim());
        values.put("DiemDen", chuyenDi.getDiemDen() != null ? chuyenDi.getDiemDen().trim() : null);
        values.put("NgayDi", chuyenDi.getNgayDi());
        values.put("NgayVe", chuyenDi.getNgayVe());
        values.put("NganSach", chuyenDi.getNganSach());
        values.put("TrangThai", chuyenDi.getTrangThai() != null ? chuyenDi.getTrangThai() : "SAP_TOI");
        values.put("AnhBia", chuyenDi.getAnhBia());

        try {
            return db.insert("ChuyenDi", null, values);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi thêm chuyến đi", e);
            return -1;
        }
    }

    // lấy danh sách chuyến đi của người dùng kèm dữ liệu thống kê từ View v_TongChiChuyenDi (Read)

    public List<ChuyenDi> layDanhSachChuyenDi(int maNguoiDung) {
        List<ChuyenDi> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // kết hợp bảng ChuyenDi với View v_TongChiChuyenDi
        String sql = "SELECT c.*, " +
                "       COALESCE(v.TongChi, 0) AS TongChi, " +
                "       COALESCE(v.ConLai, c.NganSach) AS ConLai, " +
                "       COALESCE(v.TyLeSuDung, 0.0) AS TyLeSuDung, " +
                "       COALESCE(v.MucCanhBao, 'BINH_THUONG') AS MucCanhBao " +
                "FROM ChuyenDi c " +
                "LEFT JOIN v_TongChiChuyenDi v ON c.MaChuyenDi = v.MaChuyenDi " +
                "WHERE c.MaNguoiDung = ? " +
                "ORDER BY c.NgayDi DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maNguoiDung)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ChuyenDi cd = parseChuyenDiFromCursor(cursor);
                    list.add(cd);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lấy danh sách chuyến đi", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // lấy chi tiết một chuyến đi theo ID
    public ChuyenDi layChuyenDiTheoId(int maChuyenDi) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT c.*, " +
                "       COALESCE(v.TongChi, 0) AS TongChi, " +
                "       COALESCE(v.ConLai, c.NganSach) AS ConLai, " +
                "       COALESCE(v.TyLeSuDung, 0.0) AS TyLeSuDung, " +
                "       COALESCE(v.MucCanhBao, 'BINH_THUONG') AS MucCanhBao " +
                "FROM ChuyenDi c " +
                "LEFT JOIN v_TongChiChuyenDi v ON c.MaChuyenDi = v.MaChuyenDi " +
                "WHERE c.MaChuyenDi = ?";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maChuyenDi)});
            if (cursor != null && cursor.moveToFirst()) {
                return parseChuyenDiFromCursor(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    // cập nhật thông tin chuyến đi (Update)
    public boolean capNhatChuyenDi(ChuyenDi chuyenDi) {
        if (chuyenDi == null) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenChuyenDi", chuyenDi.getTenChuyenDi().trim());
        values.put("DiemDen", chuyenDi.getDiemDen() != null ? chuyenDi.getDiemDen().trim() : null);
        values.put("NgayDi", chuyenDi.getNgayDi());
        values.put("NgayVe", chuyenDi.getNgayVe());
        values.put("NganSach", chuyenDi.getNganSach());
        values.put("TrangThai", chuyenDi.getTrangThai());
        values.put("AnhBia", chuyenDi.getAnhBia());

        try {
            int rows = db.update("ChuyenDi", values, "MaChuyenDi = ?",
                    new String[]{String.valueOf(chuyenDi.getMaChuyenDi())});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi cập nhật chuyến đi (có thể do vi phạm ngày của Trigger)", e);
            return false;
        }
    }

    // xóa chuyến đi (Delete - tự động CASCADE xóa Lịch Trình, Chi Phí, Ảnh)
    public boolean xoaChuyenDi(int maChuyenDi) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rows = db.delete("ChuyenDi", "MaChuyenDi = ?", new String[]{String.valueOf(maChuyenDi)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xóa chuyến đi", e);
            return false;
        }
    }
    // tìm kiếm chuyến đi theo từ khóa
    public List<ChuyenDi> timKiemChuyenDi(int maNguoiDung, String keyword) {
        List<ChuyenDi> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT c.*, " +
                "       COALESCE(v.TongChi, 0) AS TongChi, " +
                "       COALESCE(v.ConLai, c.NganSach) AS ConLai, " +
                "       COALESCE(v.TyLeSuDung, 0.0) AS TyLeSuDung, " +
                "       COALESCE(v.MucCanhBao, 'BINH_THUONG') AS MucCanhBao " +
                "FROM ChuyenDi c " +
                "LEFT JOIN v_TongChiChuyenDi v ON c.MaChuyenDi = v.MaChuyenDi " +
                "WHERE c.MaNguoiDung = ? AND (c.TenChuyenDi LIKE ? OR c.DiemDen LIKE ?) " +
                "ORDER BY c.NgayDi DESC";

        String pattern = "%" + keyword.trim() + "%";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maNguoiDung), pattern, pattern});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(parseChuyenDiFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    private ChuyenDi parseChuyenDiFromCursor(Cursor cursor) {
        ChuyenDi cd = new ChuyenDi();
        cd.setMaChuyenDi(cursor.getInt(cursor.getColumnIndexOrThrow("MaChuyenDi")));
        cd.setMaNguoiDung(cursor.getInt(cursor.getColumnIndexOrThrow("MaNguoiDung")));
        cd.setTenChuyenDi(cursor.getString(cursor.getColumnIndexOrThrow("TenChuyenDi")));
        cd.setDiemDen(cursor.getString(cursor.getColumnIndexOrThrow("DiemDen")));
        cd.setNgayDi(cursor.getString(cursor.getColumnIndexOrThrow("NgayDi")));
        cd.setNgayVe(cursor.getString(cursor.getColumnIndexOrThrow("NgayVe")));
        cd.setNganSach(cursor.getLong(cursor.getColumnIndexOrThrow("NganSach")));
        cd.setTrangThai(cursor.getString(cursor.getColumnIndexOrThrow("TrangThai")));
        cd.setAnhBia(cursor.getString(cursor.getColumnIndexOrThrow("AnhBia")));

        // Các cột thống kê
        cd.setTongChi(cursor.getLong(cursor.getColumnIndexOrThrow("TongChi")));
        cd.setConLai(cursor.getLong(cursor.getColumnIndexOrThrow("ConLai")));
        cd.setTyLeSuDung(cursor.getDouble(cursor.getColumnIndexOrThrow("TyLeSuDung")));
        cd.setMucCanhBao(cursor.getString(cursor.getColumnIndexOrThrow("MucCanhBao")));
        return cd;
    }
}