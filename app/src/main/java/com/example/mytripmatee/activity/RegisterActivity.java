package com.example.mytripmatee.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mytripmatee.R;
import com.example.mytripmatee.database.NguoiDungDAO;
import com.example.mytripmatee.model.NguoiDung;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etFullName, etEmail, etPassword;
    private MaterialButton btnRegisterSubmit, btnNavLogin;
    private CheckBox cbTerms;
    private NguoiDungDAO nguoiDungDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nguoiDungDAO = new NguoiDungDAO(this);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        btnNavLogin = findViewById(R.id.btnNavLogin);

        btnRegisterSubmit.setOnClickListener(v -> handleRegister());
        btnNavLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String username = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // kiểm tra rỗng
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // kiểm tra định dạng username (theo CHECK constraint của SQLite)
        if (username.length() < 3 || username.length() > 30) {
            Toast.makeText(this, "Tên đăng nhập phải từ 3 đến 30 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!username.matches("^[a-zA-Z0-9_.]+$")) {
            Toast.makeText(this, "Tên đăng nhập chỉ gồm chữ cái, số, dấu gạch dưới (_) hoặc chấm (.)", Toast.LENGTH_LONG).show();
            return;
        }

        // kiểm tra độ dài mật khẩu
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        // kiểm tra check điều khoản
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Bạn cần đồng ý với điều khoản sử dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        // kiểm tra trùng lặp username
        if (nguoiDungDAO.kiemTraTrungTenDangNhap(username)) {
            Toast.makeText(this, "Tên đăng nhập '" + username + "' đã được sử dụng, vui lòng chọn tên khác", Toast.LENGTH_LONG).show();
            etEmail.requestFocus();
            return;
        }

        // thực hiện tạo tài khoản
        NguoiDung newUser = new NguoiDung(username, password, fullName);
        long newId = nguoiDungDAO.dangKy(newUser);

        if (newId > 0) {
            Toast.makeText(this, "Đăng ký thành công! Hãy đăng nhập ngay", Toast.LENGTH_SHORT).show();
            // Trả username về cho LoginActivity tự điền sẵn
            Intent resultIntent = new Intent();
            resultIntent.putExtra("registered_username", username);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Đăng ký thất bại, vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
        }
    }
}
