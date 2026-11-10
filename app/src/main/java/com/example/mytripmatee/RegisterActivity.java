package com.example.mytripmatee;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etFullName, etEmail, etPassword;
    private MaterialButton btnRegisterSubmit, btnNavLogin;
    private CheckBox cbTerms;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // ánh xạ view
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        btnNavLogin = findViewById(R.id.btnNavLogin);

        // xử lý nút tạo tài khoản và bắt đầu
        btnRegisterSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = etFullName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // kiểm tra bỏ trống
                if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }
                // kiểm tra độ dài của mật khẩu
                if (password.length() < 8) {
                    Toast.makeText(RegisterActivity.this, "Mật khẩu phải có ít nhất 8 ký tự", Toast.LENGTH_SHORT).show();
                    return;
                }
                // kiểm tra checkbox của điều khoản
                if (!cbTerms.isChecked()) {
                    Toast.makeText(RegisterActivity.this, "Bạn cần đồng ý điều khoản sử dụng", Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: gọi api tạo tài khoản hoặc insert database vào
                Toast.makeText(RegisterActivity.this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // xử lý chuyển về lại tab đăng nhập
        btnNavLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
