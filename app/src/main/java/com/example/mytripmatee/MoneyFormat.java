package com.example.mytripmatee;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MoneyFormat {

    // Định dạng mọi kiểu số (double, long, int) hoặc Chuỗi số thành dạng: 1.000.000 VNĐ
    public static String formatVND(Object amount) {
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
            symbols.setGroupingSeparator('.');
            DecimalFormat formatter = new DecimalFormat("#,##0", symbols);

            if (amount instanceof Number) {
                return formatter.format(amount) + " VNĐ";
            }
            if (amount instanceof String) {
                double val = Double.parseDouble(((String) amount).replaceAll("[^0-9]", ""));
                return formatter.format(val) + " VNĐ";
            }
        } catch (Exception e) { /* Có xử lý ngoại lệ */ }
        return "0 VNĐ";
    }
}