package com.example.mytripmatee.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mytripmatee.database.DBHelper;
import com.example.mytripmatee.model.ItemAnhThuVien;

import java.util.ArrayList;
import java.util.List;

public class AnhDAO {

    private final DBHelper dbHelper;

    public AnhDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public List<ItemAnhThuVien> layDanhSachAnhTheoChuyenDi(int maChuyenDi) {
        List<ItemAnhThuVien> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT MaAnh, DuongDanAnh, ChuThich FROM Anh WHERE MaChuyenDi = ? ORDER BY MaAnh DESC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(maChuyenDi)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int maAnh = cursor.getInt(cursor.getColumnIndexOrThrow("MaAnh"));
                String duongDan = cursor.getString(cursor.getColumnIndexOrThrow("DuongDanAnh"));
                String chuThich = cursor.getString(cursor.getColumnIndexOrThrow("ChuThich"));

                String category = "Cảnh đẹp";
                String title = "Hình ảnh chuyến đi";

                if (chuThich != null && !chuThich.trim().isEmpty()) {
                    if (chuThich.contains("|")) {
                        String[] parts = chuThich.split("\\|", 2);
                        category = parts[0].trim();
                        title = parts[1].trim();
                    } else {
                        title = chuThich.trim();
                    }
                }

                ItemAnhThuVien item = new ItemAnhThuVien(
                        String.valueOf(maAnh),
                        title,
                        category,
                        0,
                        category
                );
                item.setDuongDanUri(duongDan);
                list.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return list;
    }

    public String layAnhCanhDepRandomTheoChuyenDi(int maChuyenDi) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT DuongDanAnh, ChuThich FROM Anh WHERE MaChuyenDi = ? ORDER BY RANDOM()";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(maChuyenDi)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String duongDan = cursor.getString(cursor.getColumnIndexOrThrow("DuongDanAnh"));
                String chuThich = cursor.getString(cursor.getColumnIndexOrThrow("ChuThich"));

                String category = "Cảnh đẹp";
                if (chuThich != null && chuThich.contains("|")) {
                    category = chuThich.split("\\|", 2)[0].trim();
                }

                if ("Cảnh đẹp".equalsIgnoreCase(category)) {
                    cursor.close();
                    return duongDan;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return null;
    }

    public List<String> layTopDanhSachAnhCanhDepRandom(int maChuyenDi, int limit) {
        List<String> result = new ArrayList<>();
        if (maChuyenDi <= 0) return result;

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Lấy ngẫu nhiên các ảnh có tag "Cảnh đẹp" thuộc đúng chuyến đi này
        String sql = "SELECT DuongDanAnh, ChuThich FROM Anh WHERE MaChuyenDi = ? ORDER BY RANDOM()";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(maChuyenDi)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String duongDan = cursor.getString(cursor.getColumnIndexOrThrow("DuongDanAnh"));
                String chuThich = cursor.getString(cursor.getColumnIndexOrThrow("ChuThich"));

                String category = "Cảnh đẹp";
                if (chuThich != null && chuThich.contains("|")) {
                    category = chuThich.split("\\|", 2)[0].trim();
                }

                if ("Cảnh đẹp".equalsIgnoreCase(category) && !result.contains(duongDan)) {
                    result.add(duongDan);
                    if (result.size() >= limit) {
                        break;
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        return result;
    }

    public long themAnh(int maChuyenDi, String duongDanAnh, String chuThich, String danhMuc) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MaChuyenDi", maChuyenDi);
        values.put("DuongDanAnh", duongDanAnh);

        String cat = (danhMuc != null && !danhMuc.trim().isEmpty()) ? danhMuc.trim() : "Cảnh đẹp";
        String note = (chuThich != null && !chuThich.trim().isEmpty()) ? chuThich.trim() : "Kỷ niệm chuyến đi";

        values.put("ChuThich", cat + "|" + note);
        return db.insert("Anh", null, values);
    }

    public boolean xoaAnh(int maAnh) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete("Anh", "MaAnh = ?", new String[]{String.valueOf(maAnh)});
        return rows > 0;
    }
}
