package com.example.mytripmatee;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Định dạng Date hoặc Timestamp (Long) sang chuỗi "dd/MM/yyyy"
    public static String formatDate(Object input) {
        try {
            if (input instanceof Date) return sdf.format((Date) input);
            if (input instanceof Long) return sdf.format(new Date((Long) input));
        } catch (Exception e) { /* Có xử lý ngoại lệ */ }
        return "";
    }

    // Parse chuỗi "dd/MM/yyyy" thành đối tượng Date
    public static Date parseDate(String dateStr) {
        try {
            return (dateStr != null && !dateStr.trim().isEmpty()) ? sdf.parse(dateStr.trim()) : null;
        } catch (Exception e) {
            return null; // Bắt lỗi ParseException nếu ngày sai
        }
    }
}