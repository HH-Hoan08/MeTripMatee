package com.example.mytripmatee.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "mytripmatee_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    public SessionManager(Context context) {
        this.pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
    }
    // lưu phiên khi đăng nhập thành công
    public void saveUserSession(int userId, String username, String fullName, boolean rememberMe) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULL_NAME, fullName != null ? fullName : "");
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    public boolean isRememberMe() {
        return pref.getBoolean(KEY_REMEMBER_ME, false);
    }
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }
    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }
    public String getFullName() {
        return pref.getString(KEY_FULL_NAME, "");
    }
    // Đăng xuất: Xóa phiên làm việc
    public void logout() {
        boolean remember = isRememberMe();
        String savedUsername = getUsername();
        editor.clear();
        // nếu người dùng từng chọn ghi nhớ, ta vẫn lưu lại username để lần sau tự điền vào ô đăng nhập
        if (remember) {
            editor.putString(KEY_USERNAME, savedUsername);
            editor.putBoolean(KEY_REMEMBER_ME, true);
        }
        editor.apply();
    }
}
