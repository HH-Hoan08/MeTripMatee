package com.example.mytripmatee.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class TienIchBanDo {

    private static final String TAG = "TienIchBanDo";

    /**
     * Mở vị trí địa điểm / đường dẫn Google Maps bằng Intent ngầm định ACTION_VIEW.
     * Tự động thử mở qua ứng dụng Google Maps chính thức, nếu không có sẽ tự chuyển sang trình duyệt web.
     * Xử lý ngoại lệ ActivityNotFoundException an toàn tuyệt đối, không làm crash ứng dụng.
     *
     * @param context  Context gọi Intent
     * @param mapUrl   Đường dẫn Google Maps (http/https/geo)
     * @param diaDiem  Tên địa điểm / địa chỉ
     * @param tieuDe   Tên tiêu đề hoạt động
     */
    public static void moGoogleMaps(Context context, String mapUrl, String diaDiem, String tieuDe) {
        if (context == null) return;

        String targetUrl = null;

        // 1. Kiểm tra nếu có mapUrl hợp lệ
        if (mapUrl != null && !mapUrl.trim().isEmpty()) {
            targetUrl = mapUrl.trim();
            if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://") && !targetUrl.startsWith("geo:")) {
                targetUrl = "https://" + targetUrl;
            }
        }
        // 2. Nếu không có mapUrl nhưng có diaDiem, tự tạo Google Maps Search URL
        else if (diaDiem != null && !diaDiem.trim().isEmpty()) {
            targetUrl = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(diaDiem.trim());
        }
        // 3. Nếu không có cả hai nhưng có tieuDe
        else if (tieuDe != null && !tieuDe.trim().isEmpty()) {
            targetUrl = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(tieuDe.trim());
        }

        if (targetUrl == null) {
            Toast.makeText(context, "Chưa có liên kết bản đồ hoặc địa điểm cho hoạt động này", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.parse(targetUrl);

        // Thử 1: Mở ưu tiên qua ứng dụng Google Maps chính thức bằng Intent ngầm định ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            context.startActivity(mapIntent);
            return;
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Ứng dụng Google Maps chưa được cài đặt, đang chuyển hướng sang trình duyệt web", e);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi mở ứng dụng Google Maps", e);
        }

        // Thử 2: Chuẩn hóa URI nếu là geo scheme để mở trên trình duyệt web
        if (targetUrl.startsWith("geo:")) {
            String query = targetUrl.contains("q=") ? targetUrl.substring(targetUrl.indexOf("q=") + 2) : targetUrl.replace("geo:", "");
            targetUrl = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query);
            uri = Uri.parse(targetUrl);
        }

        // Thử 3: Mở Intent ngầm định ACTION_VIEW không giới hạn package (mở trình duyệt Chrome/mặc định)
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Không tìm thấy ứng dụng bản đồ hoặc trình duyệt web trên thiết bị", e);
            Toast.makeText(context, "Không thể tìm thấy ứng dụng bản đồ hoặc trình duyệt web trên thiết bị!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khởi chạy Intent ngầm định ACTION_VIEW", e);
            Toast.makeText(context, "Lỗi khi mở bản đồ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
