package com.example.mytripmatee.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;

import java.util.List;
import java.util.Locale;

public class AdapterLich extends RecyclerView.Adapter<AdapterLich.ViewHolderNgay> {

    // Model đại diện cho 1 ô ngày trên lịch (Tên biến thuần Việt)
    public static class ItemNgayLich {
        public final int ngay;
        public final int thang; // 0 - 11
        public final int nam;
        public final boolean laThangHienTai;
        public final boolean daQua; // true nếu là ngày trong quá khứ

        public ItemNgayLich(int ngay, int thang, int nam, boolean laThangHienTai, boolean daQua) {
            this.ngay = ngay;
            this.thang = thang;
            this.nam = nam;
            this.laThangHienTai = laThangHienTai;
            this.daQua = daQua;
        }

        // Chuyển thành chuỗi yyyy-MM-dd chuẩn SQLite
        public String layChuoiNgayChuan() {
            return String.format(Locale.getDefault(), "%04d-%02d-%02d", nam, thang + 1, ngay);
        }
    }

    public interface SuKienChonNgayListener {
        void onChonNgay(ItemNgayLich itemNgay);
    }

    private final List<ItemNgayLich> danhSachNgay;
    private final SuKienChonNgayListener listener;
    private String ngayBatDauStr = null; // Định dạng yyyy-MM-dd
    private String ngayKetThucStr = null;

    // tạo một danh sách độc lập cho Adapter (không dùng chung vùng nhớ)
    public AdapterLich(List<ItemNgayLich> danhSachNgayBanDau, SuKienChonNgayListener listener) {
        this.danhSachNgay = new java.util.ArrayList<>();
        if (danhSachNgayBanDau != null) {
            this.danhSachNgay.addAll(danhSachNgayBanDau);
        }
        this.listener = listener;
    }

    // Cập nhật khoảng ngày đang được chọn để tô màu
    public void setKhoangNgayChon(String ngayBatDau, String ngayKetThuc) {
        this.ngayBatDauStr = ngayBatDau;
        this.ngayKetThucStr = ngayKetThuc;
        notifyDataSetChanged();
    }

    // Cập nhật danh sách ngày khi đổi tháng/năm
    public void capNhatDanhSach(List<ItemNgayLich> danhSachMoi) {
        // kiểm tra tránh tự xóa
        if (this.danhSachNgay != danhSachMoi) {
            this.danhSachNgay.clear();
            if (danhSachMoi != null) {
                this.danhSachNgay.addAll(danhSachMoi);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolderNgay onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ngay_lich, parent, false);
        return new ViewHolderNgay(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderNgay holder, int position) {
        ItemNgayLich item = danhSachNgay.get(position);
        holder.tvDayNumber.setText(String.valueOf(item.ngay));

        int colorNormal = ContextCompat.getColor(holder.itemView.getContext(), R.color.calendar_text_dark);
        int colorPrimary = ContextCompat.getColor(holder.itemView.getContext(), R.color.calendar_primary);

        // 1. Ngày của tháng trước/sau hoặc ngày đã qua trong quá khứ -> Làm mờ, không cho bấm
        if (!item.laThangHienTai || item.daQua) {
            holder.tvDayNumber.setTextColor(colorNormal);
            holder.tvDayNumber.setAlpha(item.laThangHienTai ? 0.38f : 0.22f);
            holder.tvDayNumber.setTypeface(null, Typeface.NORMAL);
            holder.viewCircleHighlight.setVisibility(View.GONE);
            holder.viewRangeHighlight.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
            return;
        }

        // 2. Ngày hợp lệ trong hiện tại & tương lai
        holder.tvDayNumber.setAlpha(1.0f);
        String chuoiNgayHienTai = item.layChuoiNgayChuan();
        boolean laNgayBatDau = ngayBatDauStr != null && chuoiNgayHienTai.equals(ngayBatDauStr);
        boolean laNgayKetThuc = ngayKetThucStr != null && chuoiNgayHienTai.equals(ngayKetThucStr);
        boolean coKhoangNhieuNgay = ngayBatDauStr != null && ngayKetThucStr != null
                && !ngayBatDauStr.equals(ngayKetThucStr);
        boolean namTrongKhoang = coKhoangNhieuNgay
                && chuoiNgayHienTai.compareTo(ngayBatDauStr) > 0
                && chuoiNgayHienTai.compareTo(ngayKetThucStr) < 0;

        if (laNgayBatDau || laNgayKetThuc) {
            // Điểm đầu hoặc điểm cuối: Hình tròn xanh đậm, chữ trắng in đậm
            holder.viewCircleHighlight.setVisibility(View.VISIBLE);
            holder.viewRangeHighlight.setVisibility(coKhoangNhieuNgay ? View.VISIBLE : View.GONE);
            holder.tvDayNumber.setTextColor(Color.WHITE);
            holder.tvDayNumber.setTypeface(null, Typeface.BOLD);
        } else if (namTrongKhoang) {
            // Ngày ở giữa khoảng: Dải màu xanh nhạt, chữ xanh đậm
            holder.viewCircleHighlight.setVisibility(View.GONE);
            holder.viewRangeHighlight.setVisibility(View.VISIBLE);
            holder.tvDayNumber.setTextColor(colorPrimary);
            holder.tvDayNumber.setTypeface(null, Typeface.BOLD);
        } else {
            // Ngày bình thường
            holder.viewCircleHighlight.setVisibility(View.GONE);
            holder.viewRangeHighlight.setVisibility(View.GONE);
            holder.tvDayNumber.setTextColor(colorNormal);
            holder.tvDayNumber.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChonNgay(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return danhSachNgay.size();
    }

    public static class ViewHolderNgay extends RecyclerView.ViewHolder {
        TextView tvDayNumber;
        View viewRangeHighlight, viewCircleHighlight;

        public ViewHolderNgay(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            viewRangeHighlight = itemView.findViewById(R.id.viewRangeHighlight);
            viewCircleHighlight = itemView.findViewById(R.id.viewCircleHighlight);
        }
    }
}