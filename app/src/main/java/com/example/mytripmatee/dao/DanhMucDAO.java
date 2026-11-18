package com.example.mytripmatee.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mytripmatee.database.DBHelper;
import com.example.mytripmatee.model.DanhMuc;

import java.util.ArrayList;
import java.util.List;

public class DanhMucDAO {
    private static final String TAG = "DanhMucDAO";
    private final DBHelper dbHelper;

    public DanhMucDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    // Lấy danh sách tất cả danh mục chi tiêu từ SQLite
    public List<DanhMuc> layDanhSachDanhMuc() {
        List<DanhMuc> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM DanhMuc ORDER BY MaDanhMuc ASC", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    DanhMuc cat = new DanhMuc();
                    cat.setMaDanhMuc(cursor.getInt(cursor.getColumnIndexOrThrow("MaDanhMuc")));
                    cat.setTenDanhMuc(cursor.getString(cursor.getColumnIndexOrThrow("TenDanhMuc")));
                    cat.setMauSac(cursor.getString(cursor.getColumnIndexOrThrow("MauSac")));
                    list.add(cat);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lấy danh sách danh mục", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }
}