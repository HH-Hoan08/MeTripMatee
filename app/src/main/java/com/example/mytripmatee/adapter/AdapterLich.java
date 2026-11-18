package com.example.mytripmatee.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;

import java.util.List;

public class AdapterLich extends RecyclerView.Adapter<AdapterLich.ViewHolderNgay> {

    public static class ItemNgayLich {
        public final int soNgay;
        public final boolean laThangHienTai;

        public ItemNgayLich(int soNgay, boolean laThangHienTai) {
            this.soNgay = soNgay;
            this.laThangHienTai = laThangHienTai;
        }
    }

    public interface SuKienChonNgayListener {
        void onChonNgay(int soNgay);
    }

    private final List<ItemNgayLich> danhSachNgay;
    private final SuKienChonNgayListener listener;
    private Integer ngayBatDau = 12;
    private Integer ngayKetThuc = 15;

    public AdapterLich(List<ItemNgayLich> danhSachNgay, SuKienChonNgayListener listener) {
        this.danhSachNgay = danhSachNgay;
        this.listener = listener;
    }

    public void setKhoangNgayChon(Integer ngayBatDau, Integer ngayKetThuc) {
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
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
        holder.tvDayNumber.setText(String.valueOf(item.soNgay));

        if (!item.laThangHienTai) {
            holder.tvDayNumber.setTextColor(Color.parseColor("#CCCCCC"));
            holder.tvDayNumber.setTypeface(null, Typeface.NORMAL);
            holder.viewCircleHighlight.setVisibility(View.GONE);
            holder.viewRangeHighlight.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            return;
        }

        boolean isStart = ngayBatDau != null && item.soNgay == ngayBatDau.intValue();
        boolean isEnd = ngayKetThuc != null && item.soNgay == ngayKetThuc.intValue();
        boolean inRange = ngayBatDau != null && ngayKetThuc != null
                && item.soNgay > ngayBatDau && item.soNgay < ngayKetThuc;

        if (isStart || isEnd) {
            holder.viewCircleHighlight.setVisibility(View.VISIBLE);
            holder.viewRangeHighlight.setVisibility(ngayKetThuc != null ? View.VISIBLE : View.GONE);
            holder.tvDayNumber.setTextColor(Color.WHITE);
            holder.tvDayNumber.setTypeface(null, Typeface.BOLD);
        } else if (inRange) {
            holder.viewCircleHighlight.setVisibility(View.GONE);
            holder.viewRangeHighlight.setVisibility(View.VISIBLE);
            holder.tvDayNumber.setTextColor(Color.parseColor("#00896A"));
            holder.tvDayNumber.setTypeface(null, Typeface.BOLD);
        } else {
            holder.viewCircleHighlight.setVisibility(View.GONE);
            holder.viewRangeHighlight.setVisibility(View.GONE);
            holder.tvDayNumber.setTextColor(Color.parseColor("#1A1A1A"));
            holder.tvDayNumber.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChonNgay(item.soNgay);
            }
        });
    }

    @Override
    public int getItemCount() {
        return danhSachNgay.size();
    }

    static class ViewHolderNgay extends RecyclerView.ViewHolder {
        final View viewRangeHighlight;
        final View viewCircleHighlight;
        final TextView tvDayNumber;

        ViewHolderNgay(@NonNull View itemView) {
            super(itemView);
            viewRangeHighlight = itemView.findViewById(R.id.viewRangeHighlight);
            viewCircleHighlight = itemView.findViewById(R.id.viewCircleHighlight);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
        }
    }
}
