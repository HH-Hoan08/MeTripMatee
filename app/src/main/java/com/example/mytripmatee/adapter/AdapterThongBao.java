package com.example.mytripmatee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;
import com.example.mytripmatee.model.ThongBao;

import java.util.ArrayList;
import java.util.List;

public class AdapterThongBao extends RecyclerView.Adapter<AdapterThongBao.ThongBaoViewHolder> {

    public interface OnThongBaoActionListener {
        void onAccept(ThongBao thongBao, int position);
        void onDecline(ThongBao thongBao, int position);
    }

    private final List<ThongBao> danhSachThongBao = new ArrayList<>();
    private final OnThongBaoActionListener actionListener;

    public AdapterThongBao(OnThongBaoActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setDanhSachThongBao(List<ThongBao> list) {
        danhSachThongBao.clear();
        if (list != null) {
            danhSachThongBao.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ThongBaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ThongBaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThongBaoViewHolder holder, int position) {
        ThongBao tb = danhSachThongBao.get(position);
        holder.bind(tb, actionListener, position);
    }

    @Override
    public int getItemCount() {
        return danhSachThongBao.size();
    }

    static class ThongBaoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvTime;
        private final TextView tvContent;
        private final TextView tvStatus;
        private final LinearLayout layoutActions;
        private final Button btnAccept;
        private final Button btnDecline;

        public ThongBaoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
            tvContent = itemView.findViewById(R.id.tvNotifContent);
            tvStatus = itemView.findViewById(R.id.tvNotifStatus);
            layoutActions = itemView.findViewById(R.id.layoutNotifActions);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }

        public void bind(ThongBao tb, OnThongBaoActionListener listener, int position) {
            tvTitle.setText(tb.getTieuDe() != null ? tb.getTieuDe() : "Thông báo");
            tvTime.setText(tb.getNgayTao() != null ? tb.getNgayTao() : "Mới");
            tvContent.setText(tb.getNoiDung() != null ? tb.getNoiDung() : "");

            if ("MOI_THAM_GIA".equals(tb.getLoaiThongBao())) {
                if (tb.getTrangThai() == 0) { // Pending invitation
                    layoutActions.setVisibility(View.VISIBLE);
                    tvStatus.setVisibility(View.GONE);

                    btnAccept.setOnClickListener(v -> {
                        if (listener != null) listener.onAccept(tb, position);
                    });
                    btnDecline.setOnClickListener(v -> {
                        if (listener != null) listener.onDecline(tb, position);
                    });
                } else if (tb.getTrangThai() == 1) { // Accepted
                    layoutActions.setVisibility(View.GONE);
                    tvStatus.setVisibility(View.VISIBLE);
                    tvStatus.setText("✓ Đã chấp nhận");
                    tvStatus.setBackgroundResource(R.drawable.bg_pill_teal);
                } else { // Declined (2)
                    layoutActions.setVisibility(View.GONE);
                    tvStatus.setVisibility(View.VISIBLE);
                    tvStatus.setText("✕ Đã từ chối");
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_orange);
                }
            } else {
                // Non-invitation notifications
                layoutActions.setVisibility(View.GONE);
                tvStatus.setVisibility(View.GONE);
            }
        }
    }
}
