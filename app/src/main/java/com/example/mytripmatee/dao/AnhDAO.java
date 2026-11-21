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

                String title = chuThich != null && !chuThich.trim().isEmpty() ? chuThich : "Hình ảnh chuyến đi";
                String sub = "Thư viện ảnh";

                ItemAnhThuVien item = new ItemAnhThuVien(
                        String.valueOf(maAnh),
                        title,
                        sub,
                        0,
                        "Kỷ niệm"
                );
                item.setDuongDanUri(duongDan);
                list.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return list;
    }

    public long themAnh(int maChuyenDi, String duongDanAnh, String chuThich) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MaChuyenDi", maChuyenDi);
        values.put("DuongDanAnh", duongDanAnh);
        values.put("ChuThich", chuThich);
        return db.insert("Anh", null, values);
    }

    public boolean xoaAnh(int maAnh) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete("Anh", "MaAnh = ?", new String[]{String.valueOf(maAnh)});
        return rows > 0;
    }
}
