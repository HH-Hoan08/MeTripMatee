package com.example.mytripmatee.util;

import android.content.Context;
import android.content.SharedPreferences;

public class QuanLyPhien {
    private static final String TEN_SHARED_PREF = "mytripmatee_session";
    private static final String KHOA_DA_DANG_NHAP = "is_logged_in";
    private static final String KHOA_MA_NGUOI_DUNG = "user_id";
    private static final String KHOA_TEN_DANG_NHAP = "username";
    private static final String KHOA_HO_TEN = "full_name";
    private static final String KHOA_GHI_NHO_DANG_NHAP = "remember_me";
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public QuanLyPhien(Context context) {
        this.pref = context.getSharedPreferences(TEN_SHARED_PREF, Context.MODE_PRIVATE);
        this.editor = pref.edit();
    }

    // lưu phiên khi đăng nhập thành công
    public void luuPhienNguoiDung(int maNguoiDung, String tenDangNhap, String hoTen, boolean ghiNho) {
        editor.putBoolean(KHOA_DA_DANG_NHAP, true);
        editor.putInt(KHOA_MA_NGUOI_DUNG, maNguoiDung);
        editor.putString(KHOA_TEN_DANG_NHAP, tenDangNhap);
        editor.putString(KHOA_HO_TEN, hoTen != null ? hoTen : "");
        editor.putBoolean(KHOA_GHI_NHO_DANG_NHAP, ghiNho);
        editor.apply();
    }

    public boolean daDangNhap() {
        return pref.getBoolean(KHOA_DA_DANG_NHAP, false);
    }

    public boolean coGhiNhoDangNhap() {
        return pref.getBoolean(KHOA_GHI_NHO_DANG_NHAP, false);
    }

    public int getMaNguoiDung() {
        return pref.getInt(KHOA_MA_NGUOI_DUNG, -1);
    }

    public String getTenDangNhap() {
        return pref.getString(KHOA_TEN_DANG_NHAP, "");
    }

    public String getHoTen() {
        return pref.getString(KHOA_HO_TEN, "");
    }

    // Đăng xuất: Xóa phiên làm việc
    public void dangXuat() {
        boolean remember = coGhiNhoDangNhap();
        String savedUsername = getTenDangNhap();
        editor.clear();
        if (remember) {
            editor.putString(KHOA_TEN_DANG_NHAP, savedUsername);
            editor.putBoolean(KHOA_GHI_NHO_DANG_NHAP, true);
        }
        editor.apply();
    }
}
