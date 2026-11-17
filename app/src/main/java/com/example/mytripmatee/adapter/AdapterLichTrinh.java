package com.example.mytripmatee.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;
import com.example.mytripmatee.databinding.ItemLichTrinhBinding;
import com.example.mytripmatee.model.ItemLichTrinh;

import java.util.ArrayList;
import java.util.List;

public class AdapterLichTrinh extends RecyclerView.Adapter<AdapterLichTrinh.ViewHolderLichTrinh> {

    public interface SuKienChonLichTrinhListener {
        void onChonMuc(ItemLichTrinh item);
        void onMoBanDo(ItemLichTrinh item);
        void onThayDoiCheck(ItemLichTrinh item, boolean isChecked);
    }

    private final List<ItemLichTrinh> danhSachLichTrinh = new ArrayList<>();
    private final SuKienChonLichTrinhListener listener;

    public AdapterLichTrinh(SuKienChonLichTrinhListener listener) {
        this.listener = listener;
    }

    public void setDanhSachLichTrinh(List<ItemLichTrinh> danhSachMoi) {
        this.danhSachLichTrinh.clear();
        if (danhSachMoi != null) {
            this.danhSachLichTrinh.addAll(danhSachMoi);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolderLichTrinh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLichTrinhBinding binding = ItemLichTrinhBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolderLichTrinh(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderLichTrinh holder, int position) {
        if (position >= 0 && position < danhSachLichTrinh.size()) {
            holder.bind(danhSachLichTrinh.get(position), position, danhSachLichTrinh.size(), listener);
        }
    }

    @Override
    public int getItemCount() {
        return danhSachLichTrinh.size();
    }

    public static class ViewHolderLichTrinh extends RecyclerView.ViewHolder {
        private final ItemLichTrinhBinding binding;

        public ViewHolderLichTrinh(@NonNull ItemLichTrinhBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ItemLichTrinh item, int position, int totalSize, SuKienChonLichTrinhListener listener) {
            if (item == null) return;

            Context context = binding.getRoot().getContext();

            binding.timelineLineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
            binding.timelineLineBottom.setVisibility(position == totalSize - 1 ? View.INVISIBLE : View.VISIBLE);

            switch (item.getLoaiHoatDong()) {
                case AN_UONG:
                    binding.tvBadgeType.setBackgroundResource(R.drawable.bg_badge_amber);
                    binding.tvBadgeType.setTextColor(ContextCompat.getColor(context, R.color.badge_amber_text));
                    binding.nodeCircle.setBackgroundResource(R.drawable.bg_node_circle_orange);
                    binding.imgNodeIcon.setImageResource(R.drawable.ic_restaurant);
                    break;
                case LUU_TRU:
                    binding.tvBadgeType.setBackgroundResource(R.drawable.bg_badge_stay);
                    binding.tvBadgeType.setTextColor(ContextCompat.getColor(context, R.color.badge_stay_text));
                    binding.nodeCircle.setBackgroundResource(R.drawable.bg_node_circle_purple);
                    binding.imgNodeIcon.setImageResource(R.drawable.ic_hotel);
                    break;
                case THAM_QUAN:
                    binding.tvBadgeType.setBackgroundResource(R.drawable.bg_badge_teal);
                    binding.tvBadgeType.setTextColor(ContextCompat.getColor(context, R.color.teal_primary));
                    binding.nodeCircle.setBackgroundResource(R.drawable.bg_node_circle_blue);
                    binding.imgNodeIcon.setImageResource(R.drawable.ic_explore);
                    break;
                case DI_CHUYEN:
                default:
                    binding.tvBadgeType.setBackgroundResource(R.drawable.bg_badge_move);
                    binding.tvBadgeType.setTextColor(ContextCompat.getColor(context, R.color.badge_move_text));
                    binding.nodeCircle.setBackgroundResource(R.drawable.bg_node_circle_blue);
                    binding.imgNodeIcon.setImageResource(R.drawable.ic_pin_flight);
                    break;
            }

            binding.tvBadgeType.setText(item.getNhanLoaiHoatDong() != null ? item.getNhanLoaiHoatDong() : "");
            binding.tvTimeRange.setText(item.getKhungGio() != null ? item.getKhungGio() : "");
            binding.tvTitle.setText(item.getTieuDe() != null ? item.getTieuDe() : "");
            binding.tvDescription.setText(item.getMoTa() != null ? item.getMoTa() : "");

            if (item.isCoBanDo()) {
                binding.btnOpenMaps.setVisibility(View.VISIBLE);
                binding.tvSubInfo.setVisibility(View.GONE);
                binding.btnOpenMaps.setOnClickListener(v -> {
                    if (listener != null) listener.onMoBanDo(item);
                });
            } else {
                binding.btnOpenMaps.setVisibility(View.GONE);
                binding.tvSubInfo.setVisibility(View.VISIBLE);
                binding.tvSubInfo.setText(item.getThongTinPhu() != null ? item.getThongTinPhu() : "");
            }

            binding.cbDone.setOnCheckedChangeListener(null);
            binding.cbDone.setText(item.getVanBanTrangThai() != null ? item.getVanBanTrangThai() : "");
            binding.cbDone.setChecked(item.isDaHoanThanh());
            binding.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setDaHoanThanh(isChecked);
                if (listener != null) {
                    listener.onThayDoiCheck(item, isChecked);
                }
            });

            if (item.getVanBanCanhBao() != null && !item.getVanBanCanhBao().isEmpty()) {
                binding.layoutAlert.setVisibility(View.VISIBLE);
                binding.tvAlertText.setText(item.getVanBanCanhBao());
            } else {
                binding.layoutAlert.setVisibility(View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChonMuc(item);
                }
            });
        }
    }
}
