package com.example.mytripmatee.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.Locale;

public class NganSach {

    // Hàm cập nhật ProgressBar, Badge % và hiển thị AlertDialog cảnh báo
    public static void xulyCanhBaoNganSach(Context context, long tongChi, long nganSach, ProgressBar pbBudget, TextView tvBadge) {
        if (context == null || nganSach <= 0) return;

        // 1. Tính phần trăm sử dụng ngân sách
        int percent = (int) ((tongChi * 100.0) / nganSach);

        // 2. Cập nhật giao diện ProgressBar & Badge
        if (pbBudget != null) {
            pbBudget.setProgress(Math.min(percent, 100));

            // Đổi màu cảnh báo: Cam khi >= 80%, Đỏ khi > 100%
            if (percent > 100) {
                pbBudget.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#DC2626"))); // Đỏ
            } else if (percent >= 80) {
                pbBudget.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#F59E0B"))); // Cam
            } else {
                pbBudget.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#10B981"))); // Xanh
            }
        }

        if (tvBadge != null) {
            tvBadge.setText(String.format(Locale.getDefault(), "Đã chi %d%%", percent));
        }

        // 3. Hiển thị AlertDialog cảnh báo
        if (percent > 100) {
            hienDialog(context, "⚠️ VƯỢT NGÂN SÁCH!",
                    "Bạn đã chi tiêu vượt quá 100% ngân sách chuyến đi!\n• Đã chi: " + formatMoney(tongChi) + "\n• Ngân sách: " + formatMoney(nganSach));
        } else if (percent >= 80) {
            hienDialog(context, "⚡ CẢNH BÁO NGÂN SÁCH",
                    "Bạn đã sử dụng " + percent + "% ngân sách chuyến đi. Hãy chú ý các khoản chi sắp tới!");
        }
    }

    private static void hienDialog(Context context, String tieuDe, String noiDung) {
        new AlertDialog.Builder(context)
                .setTitle(tieuDe)
                .setMessage(noiDung)
                .setPositiveButton("Đã hiểu", null)
                .show();
    }

    private static String formatMoney(long amount) {
        return String.format(Locale.getDefault(), "%,d đ", amount).replace(',', '.');
    }
}