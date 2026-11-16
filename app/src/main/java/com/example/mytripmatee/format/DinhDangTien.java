package com.example.mytripmatee.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DinhDangTien {

    // Định dạng mọi kiểu số (double, long, int) hoặc Chuỗi số thành dạng: 1.000.000 VNĐ
    public static String dinhDangVND(Object soTien) {
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
            symbols.setGroupingSeparator('.');
            DecimalFormat formatter = new DecimalFormat("#,##0", symbols);

            if (soTien instanceof Number) {
                return formatter.format(soTien) + " VNĐ";
            }
            if (soTien instanceof String) {
                double val = Double.parseDouble(((String) soTien).replaceAll("[^0-9]", ""));
                return formatter.format(val) + " VNĐ";
            }
        } catch (Exception e) { /* Có xử lý ngoại lệ */ }
        return "0 VNĐ";
    }
}
