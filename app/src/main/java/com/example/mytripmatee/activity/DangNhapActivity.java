package com.example.mytripmatee.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mytripmatee.R;
import com.example.mytripmatee.database.DBHelper;
import com.example.mytripmatee.database.NguoiDungDAO;
import com.example.mytripmatee.model.NguoiDung;
import com.example.mytripmatee.util.QuanLyPhien;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class DangNhapActivity extends AppCompatActivity {
    private static final String SUPPORT_EMAIL = "support@mytripmatee.com";

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnNavRegister;
    private TextView tvForgotPassword;
    private CheckBox cbRemember;
    private ScrollView svLogin;

    private QuanLyPhien quanLyPhien;
    private NguoiDungDAO nguoiDungDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());

        DBHelper.getInstance(this).getWritableDatabase();

        quanLyPhien = new QuanLyPhien(this);
        nguoiDungDAO = new NguoiDungDAO(this);

        if (quanLyPhien.daDangNhap() && quanLyPhien.coGhiNhoDangNhap()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_dang_nhap);

        svLogin = findViewById(R.id.svLogin);
        View rootView = findViewById(R.id.rootLogin);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
                v.setPadding(imeInsets.left, imeInsets.top, imeInsets.right, imeInsets.bottom);
                return insets;
            });
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnNavRegister = findViewById(R.id.btnNavRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        cbRemember = findViewById(R.id.cbRemember);

        setupAutoScrollOnFocus(svLogin, etEmail, etPassword);

        if (quanLyPhien.coGhiNhoDangNhap()) {
            etEmail.setText(quanLyPhien.getTenDangNhap());
            cbRemember.setChecked(true);
        }

        btnLogin.setOnClickListener(v -> handleLogin());

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> openSupportEmail());
        }

        if (btnNavRegister != null) {
            btnNavRegister.setOnClickListener(v -> {
                Intent intent = new Intent(DangNhapActivity.this, DangKiActivity.class);
                startActivity(intent);
            });
        }
    }

    private void openSupportEmail() {
        String username = (etEmail != null && etEmail.getText() != null)
                ? etEmail.getText().toString().trim()
                : "";
        String body = username.isEmpty()
                ? "Xin chào đội ngũ hỗ trợ MyTripMate,\n\nTôi cần hỗ trợ đặt lại mật khẩu cho tài khoản của mình.\nTên đăng nhập: "
                : "Xin chào đội ngũ hỗ trợ MyTripMate,\n\nTôi cần hỗ trợ đặt lại mật khẩu cho tài khoản: " + username;

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + SUPPORT_EMAIL));
        intent.putExtra(Intent.EXTRA_SUBJECT, "[MyTripMate] Hỗ trợ quên mật khẩu");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        try {
            startActivity(intent);
        } catch (Exception e) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Liên hệ hỗ trợ qua Email")
                    .setMessage("Không tìm thấy ứng dụng Email trên thiết bị.\nVui lòng gửi yêu cầu khôi phục mật khẩu tới:\n\n" + SUPPORT_EMAIL)
                    .setPositiveButton("Sao chép Email", (dialog, which) -> {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(ClipData.newPlainText("Support Email", SUPPORT_EMAIL));
                            Toast.makeText(this, "Đã sao chép " + SUPPORT_EMAIL, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Đóng", null)
                    .show();
        }
    }

    private void setupAutoScrollOnFocus(ScrollView scrollView, View... views) {
        if (scrollView == null) return;
        for (View v : views) {
            if (v != null) {
                v.setOnFocusChangeListener((view, hasFocus) -> {
                    if (hasFocus) {
                        scrollView.postDelayed(() -> {
                            int[] loc = new int[2];
                            view.getLocationOnScreen(loc);
                            scrollView.smoothScrollBy(0, loc[1] - 250);
                        }, 200);
                    }
                });
            }
        }
    }

    private void handleLogin() {
        String username = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        NguoiDung user = nguoiDungDAO.dangNhap(username, password);

        if (user != null) {
            boolean isRemember = cbRemember.isChecked();
            quanLyPhien.luuPhienNguoiDung(user.getMaNguoiDung(), user.getTenDangNhap(), user.getHoTen(), isRemember);

            Toast.makeText(this, "Chào mừng " + (user.getHoTen() != null ? user.getHoTen() : user.getTenDangNhap()) + "!", Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(DangNhapActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
