package com.example.mytripmatee.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TienIchNgay {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Định dạng Date hoặc Timestamp (Long) sang chuỗi "dd/MM/yyyy"
    public static String dinhDangNgay(Object dauVao) {
        try {
            if (dauVao instanceof Date) return sdf.format((Date) dauVao);
            if (dauVao instanceof Long) return sdf.format(new Date((Long) dauVao));
        } catch (Exception e) { /* Có xử lý ngoại lệ */ }
        return "";
    }

    // Parse chuỗi "dd/MM/yyyy" thành đối tượng Date
    public static Date chuyenChuoiThanhNgay(String chuoiNgay) {
        try {
            return (chuoiNgay != null && !chuoiNgay.trim().isEmpty()) ? sdf.parse(chuoiNgay.trim()) : null;
        } catch (Exception e) {
            return null; // Bắt lỗi ParseException nếu ngày sai
        }
    }
}
