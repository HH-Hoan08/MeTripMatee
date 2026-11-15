package com.example.mytripmatee.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mytripmatee.R;
import com.example.mytripmatee.database.DBHelper;
import com.example.mytripmatee.database.NguoiDungDAO;
import com.example.mytripmatee.model.NguoiDung;
import com.example.mytripmatee.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnNavRegister;
    private CheckBox cbRemember;

    private SessionManager sessionManager;
    private NguoiDungDAO nguoiDungDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Đảm bảo SQLite đã được khởi tạo
        DBHelper.getInstance(this).getWritableDatabase();

        sessionManager = new SessionManager(this);
        nguoiDungDAO = new NguoiDungDAO(this);

        // kiểm tra Tự động đăng nhập (Auto-login)
        if (sessionManager.isLoggedIn() && sessionManager.isRememberMe()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnNavRegister = findViewById(R.id.btnNavRegister);
        cbRemember = findViewById(R.id.cbRemember);

        // điền sẵn username nếu từng chọn ghi nhớ trước đây
        if (sessionManager.isRememberMe()) {
            etEmail.setText(sessionManager.getUsername());
            cbRemember.setChecked(true);
        }

        btnLogin.setOnClickListener(v -> handleLogin());

        if (btnNavRegister != null) {
            btnNavRegister.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
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

        // Gọi DAO để xác thực
        NguoiDung user = nguoiDungDAO.dangNhap(username, password);

        if (user != null) {
            boolean isRemember = cbRemember.isChecked();
            // Lưu phiên đăng nhập
            sessionManager.saveUserSession(user.getMaNguoiDung(), user.getTenDangNhap(), user.getHoTen(), isRemember);

            Toast.makeText(this, "Chào mừng " + (user.getHoTen() != null ? user.getHoTen() : user.getTenDangNhap()) + "!", Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}