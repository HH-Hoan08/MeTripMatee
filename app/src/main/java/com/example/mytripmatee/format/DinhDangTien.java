package com.example.mytripmatee.format;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DinhDangTien {

    private static final DecimalFormat formatter;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        formatter = new DecimalFormat("#,##0", symbols);
    }

    // Định dạng kiểu số hoặc chuỗi thành dạng "1.000.000 VNĐ"
    public static String dinhDangVND(Object soTien) {
        long val = chuyenChuoiThanhSo(soTien);
        if (val <= 0) return "0 VNĐ";
        synchronized (formatter) {
            return formatter.format(val) + " VNĐ";
        }
    }

    // Định dạng số thành dạng "1.000.000 đ"
    public static String dinhDangDong(long amount) {
        if (amount <= 0) return "0 đ";
        synchronized (formatter) {
            return formatter.format(amount) + " đ";
        }
    }

    // Định dạng số dạng ngắn: "600K", "1.5M", "0 đ"
    public static String dinhDangNgan(long amount) {
        if (amount <= 0) return "0 đ";
        if (amount >= 1_000_000) {
            double m = amount / 1_000_000.0;
            if (m == (long) m) {
                return String.format(Locale.getDefault(), "%dM", (long) m);
            } else {
                return String.format(Locale.getDefault(), "%.1fM", m).replace(".0", "");
            }
        } else if (amount >= 1_000) {
            return (amount / 1000) + "K";
        }
        return amount + " đ";
    }

    // Chuyển đổi mọi đối tượng / chuỗi nhập vào thành số long
    public static long chuyenChuoiThanhSo(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        String str = obj.toString().replaceAll("[^0-9]", "");
        if (str.isEmpty()) return 0;
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return 0;
        }
    }

    // Tự động định dạng lại số tiền dạng "1.000.000" ngay khi người dùng gõ vào EditText
    public static void themDinhDangTuDong(EditText editText) {
        if (editText == null) return;

        editText.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                isUpdating = true;

                String original = s.toString();
                long val = chuyenChuoiThanhSo(original);

                if (val > 0) {
                    String formatted;
                    synchronized (formatter) {
                        formatted = formatter.format(val);
                    }
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());
                }

                isUpdating = false;
            }
        });
    }
}
