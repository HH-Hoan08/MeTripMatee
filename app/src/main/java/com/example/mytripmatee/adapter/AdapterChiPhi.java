package com.example.mytripmatee.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;
import com.example.mytripmatee.databinding.ItemChiPhiBinding;
import com.example.mytripmatee.model.ItemChiPhi;

import java.util.ArrayList;
import java.util.List;

public class AdapterChiPhi extends RecyclerView.Adapter<AdapterChiPhi.ViewHolderChiPhi> {

    public interface SuKienChonChiPhiListener {
        void onChonChiPhi(ItemChiPhi item);
    }

    private List<ItemChiPhi> danhSachChiPhi = new ArrayList<>();
    private final SuKienChonChiPhiListener listener;

    public AdapterChiPhi(SuKienChonChiPhiListener listener) {
        this.listener = listener;
    }

    public void setDanhSachChiPhi(List<ItemChiPhi> danhSach) {
        this.danhSachChiPhi = danhSach != null ? danhSach : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void themChiPhi(ItemChiPhi item) {
        if (item != null) {
            this.danhSachChiPhi.add(0, item);
            notifyItemInserted(0);
        }
    }

    @NonNull
    @Override
    public ViewHolderChiPhi onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChiPhiBinding binding = ItemChiPhiBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolderChiPhi(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderChiPhi holder, int position) {
        holder.bind(danhSachChiPhi.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return danhSachChiPhi.size();
    }

    static class ViewHolderChiPhi extends RecyclerView.ViewHolder {
        private final ItemChiPhiBinding binding;

        public ViewHolderChiPhi(@NonNull ItemChiPhiBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ItemChiPhi item, SuKienChonChiPhiListener listener) {
            binding.tvTitle.setText(item.getTieuDe());
            binding.tvSubtitle.setText(item.getTieuDePhu());
            binding.tvAmount.setText(item.getSoTienDinhDang());

            switch (item.getLoaiDanhMuc()) {
                case DI_CHUYEN:
                    binding.iconContainer.setBackgroundResource(R.drawable.bg_badge_move);
                    binding.imgCategoryIcon.setImageResource(R.drawable.ic_pin_flight);
                    binding.imgCategoryIcon.setColorFilter(Color.parseColor("#4338CA"));
                    break;
                case AN_UONG:
                    binding.iconContainer.setBackgroundResource(R.drawable.bg_badge_amber);
                    binding.imgCategoryIcon.setImageResource(R.drawable.ic_restaurant);
                    binding.imgCategoryIcon.setColorFilter(Color.parseColor("#B45309"));
                    break;
                case LUU_TRU:
                    binding.iconContainer.setBackgroundResource(R.drawable.bg_badge_stay);
                    binding.imgCategoryIcon.setImageResource(R.drawable.ic_hotel);
                    binding.imgCategoryIcon.setColorFilter(Color.parseColor("#7E22CE"));
                    break;
                case KHAC:
                default:
                    binding.iconContainer.setBackgroundResource(R.drawable.bg_badge_teal);
                    binding.imgCategoryIcon.setImageResource(R.drawable.ic_wallet);
                    binding.imgCategoryIcon.setColorFilter(Color.parseColor("#0D9488"));
                    break;
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChonChiPhi(item);
                }
            });
        }
    }
}
