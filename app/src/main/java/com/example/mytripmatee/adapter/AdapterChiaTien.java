package com.example.mytripmatee.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;
import com.example.mytripmatee.model.ItemChiaTien;

import java.util.ArrayList;
import java.util.List;

public class AdapterChiaTien extends RecyclerView.Adapter<AdapterChiaTien.ViewHolder> {

    private List<ItemChiaTien> danhSach = new ArrayList<>();

    public void setDanhSach(List<ItemChiaTien> list) {
        this.danhSach = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chia_tien, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(danhSach.get(position));
    }

    @Override
    public int getItemCount() {
        return danhSach.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAvatar;
        private final TextView tvDescription;
        private final TextView tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }

        public void bind(ItemChiaTien item) {
            tvAvatar.setText(item.getAvatarText());
            tvDescription.setText(item.getDescription());
            tvAmount.setText(item.getDinhDangSoTien());

            if (item.isPositive()) {
                tvAvatar.setBackgroundResource(R.drawable.bg_circle_avatar_green);
                tvAmount.setTextColor(Color.parseColor("#10B981"));
            } else {
                tvAvatar.setBackgroundResource(R.drawable.bg_circle_avatar_red);
                tvAmount.setTextColor(Color.parseColor("#EF4444"));
            }
        }
    }
}