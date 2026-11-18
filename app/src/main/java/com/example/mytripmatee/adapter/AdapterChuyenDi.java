package com.example.mytripmatee.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;
import com.example.mytripmatee.databinding.ItemChuyenDiBinding;
import com.example.mytripmatee.model.ItemChuyenDi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdapterChuyenDi extends RecyclerView.Adapter<AdapterChuyenDi.ViewHolderChuyenDi> {

    public interface SuKienChonChuyenDiListener {
        void onChonChuyenDi(ItemChuyenDi itemChuyenDi);
    }

    private final List<ItemChuyenDi> danhSachChuyenDi = new ArrayList<>();
    private final SuKienChonChuyenDiListener listener;

    public AdapterChuyenDi(SuKienChonChuyenDiListener listener) {
        this.listener = listener;
    }

    public void setDanhSachChuyenDi(List<ItemChuyenDi> danhSachMoi) {
        List<ItemChuyenDi> safeList = (danhSachMoi != null) ? danhSachMoi : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallbackChuyenDi(this.danhSachChuyenDi, safeList));
        this.danhSachChuyenDi.clear();
        this.danhSachChuyenDi.addAll(safeList);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolderChuyenDi onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChuyenDiBinding binding = ItemChuyenDiBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolderChuyenDi(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderChuyenDi holder, int position) {
        if (position >= 0 && position < danhSachChuyenDi.size()) {
            holder.bind(danhSachChuyenDi.get(position), listener);
        }
    }

    @Override
    public int getItemCount() {
        return danhSachChuyenDi.size();
    }

    public static class ViewHolderChuyenDi extends RecyclerView.ViewHolder {
        private final ItemChuyenDiBinding binding;

        public ViewHolderChuyenDi(@NonNull ItemChuyenDiBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ItemChuyenDi itemChuyenDi, SuKienChonChuyenDiListener listener) {
            if (itemChuyenDi == null) return;

            Context context = binding.getRoot().getContext();

            if (itemChuyenDi.getMaAnhResId() != 0) {
                binding.imgThumbnail.setImageResource(itemChuyenDi.getMaAnhResId());
            } else {
                binding.imgThumbnail.setImageResource(R.drawable.img_phuquoc);
            }

            binding.tvTitle.setText(itemChuyenDi.getTieuDe() != null ? itemChuyenDi.getTieuDe() : "");
            binding.tvStatusBadge.setText(itemChuyenDi.getTrangThaiHienThi() != null ? itemChuyenDi.getTrangThaiHienThi() : "");
            binding.tvDateTag.setText(itemChuyenDi.getTheNgay() != null ? itemChuyenDi.getTheNgay() : "");

            dinhDangChiTietText(context, itemChuyenDi);

            int pL = binding.tvStatusBadge.getPaddingLeft();
            int pT = binding.tvStatusBadge.getPaddingTop();
            int pR = binding.tvStatusBadge.getPaddingRight();
            int pB = binding.tvStatusBadge.getPaddingBottom();

            switch (itemChuyenDi.getLoaiTrangThai()) {
                case NHAP:
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange);
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_draft_text));
                    break;
                case DA_HOAN_THANH:
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_gray);
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_completed_text));
                    break;
                case SAP_TOI:
                default:
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_teal);
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_upcoming_text));
                    break;
            }

            binding.tvStatusBadge.setPadding(pL, pT, pR, pB);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChonChuyenDi(itemChuyenDi);
                }
            });
        }

        private void dinhDangChiTietText(Context context, ItemChuyenDi itemChuyenDi) {
            String details = itemChuyenDi.getChiTiet();
            if (details == null) {
                binding.tvDetails.setText("");
                return;
            }

            if (details.contains("•")) {
                int dotIndex = details.indexOf("•");
                SpannableString spannable = new SpannableString(details);
                int tealColor = ContextCompat.getColor(context, R.color.teal_primary);

                if (itemChuyenDi.getLoaiTrangThai() == ItemChuyenDi.LoaiTrangThai.NHAP && dotIndex + 1 < details.length()) {
                    spannable.setSpan(new ForegroundColorSpan(tealColor), dotIndex + 1, details.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), dotIndex + 1, details.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                binding.tvDetails.setText(spannable);
            } else {
                binding.tvDetails.setText(details);
            }
        }
    }

    private static class DiffCallbackChuyenDi extends DiffUtil.Callback {
        private final List<ItemChuyenDi> oldList;
        private final List<ItemChuyenDi> newList;

        public DiffCallbackChuyenDi(List<ItemChuyenDi> oldList, List<ItemChuyenDi> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            ItemChuyenDi oldItem = oldList.get(oldItemPosition);
            ItemChuyenDi newItem = newList.get(newItemPosition);
            if (oldItem.getMaId() != null && newItem.getMaId() != null) {
                return oldItem.getMaId().equals(newItem.getMaId());
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            ItemChuyenDi oldItem = oldList.get(oldItemPosition);
            ItemChuyenDi newItem = newList.get(newItemPosition);
            return Objects.equals(oldItem.getTieuDe(), newItem.getTieuDe())
                    && Objects.equals(oldItem.getTrangThaiHienThi(), newItem.getTrangThaiHienThi())
                    && oldItem.getLoaiTrangThai() == newItem.getLoaiTrangThai()
                    && Objects.equals(oldItem.getTheNgay(), newItem.getTheNgay())
                    && Objects.equals(oldItem.getChiTiet(), newItem.getChiTiet())
                    && oldItem.getMaAnhResId() == newItem.getMaAnhResId();
        }
    }
}
