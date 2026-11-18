package com.example.mytripmatee.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mytripmatee.model.NguoiDung;
import com.example.mytripmatee.util.TienIchMatKhau;

public class NguoiDungDAO {
    private static final String TAG = "NguoiDungDAO";
    private final DBHelper dbHelper;

    public NguoiDungDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public boolean kiemTraTrungTenDangNhap(String tenDangNhap) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty())
            return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM NguoiDung WHERE tenDangNhap = ? LIMIT 1", new String[]{tenDangNhap.trim()});
            return cursor != null && cursor.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi kiểm tra trùng tên đăng nhập", e);
            return false;
        }
    }

    public long dangKy(NguoiDung nguoiDung) {
        if (nguoiDung == null) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenDangNhap", nguoiDung.getTenDangNhap().trim());
        String hashedPassword = TienIchMatKhau.bamMatKhau(nguoiDung.getMatKhau());
        values.put("MatKhau", hashedPassword);
        values.put("HoTen", nguoiDung.getHoTen() != null ? nguoiDung.getHoTen().trim() : null);
        try {
            return db.insert("NguoiDung", null, values);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi đăng ký tài khoản", e);
            return -1;
        }
    }

    public NguoiDung dangNhap(String tenDangNhap, String matKhauTho) {
        if (tenDangNhap == null || matKhauTho == null) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        String hashedPassword = TienIchMatKhau.bamMatKhau(matKhauTho);
        try {
            cursor = db.rawQuery("SELECT MaNguoiDung, TenDangNhap, MatKhau, HoTen FROM NguoiDung " +
                            "WHERE TenDangNhap = ? AND MatKhau = ? LIMIT 1",
                    new String[]{tenDangNhap.trim(), hashedPassword});
            if (cursor != null && cursor.moveToFirst()) {
                NguoiDung user = new NguoiDung();
                user.setMaNguoiDung(cursor.getInt(cursor.getColumnIndexOrThrow("MaNguoiDung")));
                user.setTenDangNhap(cursor.getString(cursor.getColumnIndexOrThrow("TenDangNhap")));
                user.setMatKhau(cursor.getString(cursor.getColumnIndexOrThrow("MatKhau")));
                user.setHoTen(cursor.getString(cursor.getColumnIndexOrThrow("HoTen")));
                return user;
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi đăng nhập", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public NguoiDung layTheoId(int maNguoiDung) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT MaNguoiDung, TenDangNhap, MatKhau, HoTen FROM NguoiDung WHERE MaNguoiDung = ?",
                    new String[]{String.valueOf(maNguoiDung)});
            if (cursor != null && cursor.moveToFirst()) {
                NguoiDung user = new NguoiDung();
                user.setMaNguoiDung(cursor.getInt(cursor.getColumnIndexOrThrow("MaNguoiDung")));
                user.setTenDangNhap(cursor.getString(cursor.getColumnIndexOrThrow("TenDangNhap")));
                user.setMatKhau(cursor.getString(cursor.getColumnIndexOrThrow("MatKhau")));
                user.setHoTen(cursor.getString(cursor.getColumnIndexOrThrow("HoTen")));
                return user;
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public boolean capNhatHoTen(int maNguoiDung, String hoTenMoi) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("HoTen", hoTenMoi != null ? hoTenMoi.trim() : null);
        int rows = db.update("NguoiDung", values, "MaNguoiDung = ?", new String[]{String.valueOf(maNguoiDung)});
        return rows > 0;
    }

    public boolean doiMatKhau(int maNguoiDung, String matKhauMoiTho) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MatKhau", TienIchMatKhau.bamMatKhau(matKhauMoiTho));
        int rows = db.update("NguoiDung", values, "MaNguoiDung = ?", new String[]{String.valueOf(maNguoiDung)});
        return rows > 0;
    }
}
