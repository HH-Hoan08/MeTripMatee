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

    // Format YYYY-MM-DD -> "12 Th11"
    public static String dinhDangNgayPill(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.trim().isEmpty()) return "";
        try {
            if (yyyyMMdd.contains("-")) {
                String[] parts = yyyyMMdd.split("-");
                if (parts.length == 3) {
                    int day = Integer.parseInt(parts[2]);
                    int month = Integer.parseInt(parts[1]);
                    return day + " Th" + month;
                }
            } else if (yyyyMMdd.contains("/")) {
                String[] parts = yyyyMMdd.split("/");
                if (parts.length >= 2) {
                    int day = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    return day + " Th" + month;
                }
            }
        } catch (Exception ignored) {}
        return yyyyMMdd;
    }

    // Convert YYYY-MM-DD to DD/MM/YYYY
    public static String dinhDangHienThi(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.isEmpty()) return "";
        try {
            String[] parts = yyyyMMdd.split("-");
            if (parts.length == 3) {
                return String.format(Locale.getDefault(), "%s/%s/%s", parts[2], parts[1], parts[0]);
            }
        } catch (Exception ignored) {}
        return yyyyMMdd;
    }
}
