package com.example.mytripmatee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;
import com.example.mytripmatee.model.NguoiDung;

import java.util.ArrayList;
import java.util.List;

public class AdapterChonThanhVien extends RecyclerView.Adapter<AdapterChonThanhVien.ChonThanhVienViewHolder> {

    private final List<NguoiDung> danhSachHienThi = new ArrayList<>();
    private final List<Integer> selectedUserIds;

    public AdapterChonThanhVien(List<Integer> selectedUserIds) {
        this.selectedUserIds = selectedUserIds;
    }

    public void setDanhSachNguoiDung(List<NguoiDung> list) {
        this.danhSachHienThi.clear();
        if (list != null) {
            this.danhSachHienThi.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChonThanhVienViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_member, parent, false);
        return new ChonThanhVienViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChonThanhVienViewHolder holder, int position) {
        NguoiDung nd = danhSachHienThi.get(position);
        holder.bind(nd, selectedUserIds.contains(nd.getMaNguoiDung()), () -> {
            int userId = nd.getMaNguoiDung();
            if (selectedUserIds.contains(userId)) {
                selectedUserIds.remove(Integer.valueOf(userId));
            } else {
                selectedUserIds.add(userId);
            }
            notifyItemChanged(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return danhSachHienThi.size();
    }

    static class ChonThanhVienViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbSelect;
        private final TextView tvAvatar;
        private final TextView tvFullName;
        private final TextView tvUsername;

        public ChonThanhVienViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cbSelectMember);
            tvAvatar = itemView.findViewById(R.id.tvMemberAvatar);
            tvFullName = itemView.findViewById(R.id.tvMemberFullName);
            tvUsername = itemView.findViewById(R.id.tvMemberUsername);
        }

        public void bind(NguoiDung nd, boolean isSelected, Runnable onToggle) {
            cbSelect.setChecked(isSelected);

            String fullName = (nd.getHoTen() != null && !nd.getHoTen().isEmpty()) ? nd.getHoTen() : nd.getTenDangNhap();
            tvFullName.setText(fullName);
            tvUsername.setText("@" + (nd.getTenDangNhap() != null ? nd.getTenDangNhap() : "user"));

            String initial = !fullName.isEmpty() ? String.valueOf(fullName.charAt(0)).toUpperCase() : "U";
            tvAvatar.setText(initial);

            View.OnClickListener clickListener = v -> {
                if (onToggle != null) onToggle.run();
            };

            itemView.setOnClickListener(clickListener);
            cbSelect.setOnClickListener(clickListener);
        }
    }
}
