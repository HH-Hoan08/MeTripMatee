package com.example.mytripmatee.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
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
import com.google.android.material.textfield.TextInputEditText;

public class DangNhapActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnNavRegister;
    private CheckBox cbRemember;

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

        View rootView = findViewById(R.id.rootLogin);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnNavRegister = findViewById(R.id.btnNavRegister);
        cbRemember = findViewById(R.id.cbRemember);

        if (quanLyPhien.coGhiNhoDangNhap()) {
            etEmail.setText(quanLyPhien.getTenDangNhap());
            cbRemember.setChecked(true);
        }

        btnLogin.setOnClickListener(v -> handleLogin());

        if (btnNavRegister != null) {
            btnNavRegister.setOnClickListener(v -> {
                Intent intent = new Intent(DangNhapActivity.this, DangKiActivity.class);
                startActivity(intent);
            });
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
