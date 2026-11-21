package com.example.mytripmatee.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;
import com.example.mytripmatee.databinding.ItemAnhThuVienBinding;
import com.example.mytripmatee.model.ItemAnhThuVien;

import java.util.ArrayList;
import java.util.List;

public class AdapterThuVienAnh extends RecyclerView.Adapter<AdapterThuVienAnh.ViewHolderThuVien> {

    public interface SuKienChonAnhListener {
        void onChonAnh(ItemAnhThuVien photo);
        void onXoaAnh(ItemAnhThuVien photo);
    }

    private final List<ItemAnhThuVien> danhSachAnh = new ArrayList<>();
    private final SuKienChonAnhListener listener;

    public AdapterThuVienAnh(SuKienChonAnhListener listener) {
        this.listener = listener;
    }

    public void setDanhSachAnh(List<ItemAnhThuVien> danhSachMoi) {
        this.danhSachAnh.clear();
        if (danhSachMoi != null) {
            this.danhSachAnh.addAll(danhSachMoi);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolderThuVien onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAnhThuVienBinding binding = ItemAnhThuVienBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolderThuVien(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderThuVien holder, int position) {
        if (position >= 0 && position < danhSachAnh.size()) {
            holder.bind(danhSachAnh.get(position), listener);
        }
    }

    @Override
    public int getItemCount() {
        return danhSachAnh.size();
    }

    public static class ViewHolderThuVien extends RecyclerView.ViewHolder {
        private final ItemAnhThuVienBinding binding;

        public ViewHolderThuVien(@NonNull ItemAnhThuVienBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ItemAnhThuVien photo, SuKienChonAnhListener listener) {
            if (photo == null) return;

            if (photo.getDuongDanUri() != null && !photo.getDuongDanUri().trim().isEmpty()) {
                try {
                    binding.imgPhoto.setImageURI(Uri.parse(photo.getDuongDanUri()));
                } catch (Exception e) {
                    binding.imgPhoto.setImageResource(R.drawable.img_dalat);
                }
            } else if (photo.getMaAnhResId() != 0) {
                binding.imgPhoto.setImageResource(photo.getMaAnhResId());
            } else {
                binding.imgPhoto.setImageResource(R.drawable.img_dalat);
            }

            binding.tvPhotoTitle.setText(photo.getTieuDe() != null ? photo.getTieuDe() : "Kỷ niệm");

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChonAnh(photo);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onXoaAnh(photo);
                }
                return true;
            });
        }
    }
}
