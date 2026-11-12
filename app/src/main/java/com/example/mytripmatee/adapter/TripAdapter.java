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
import com.example.mytripmatee.databinding.ItemTripBinding;
import com.example.mytripmatee.model.TripItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    public interface OnTripClickListener {
        void onTripClick(TripItem tripItem);
    }

    private final List<TripItem> tripItemList = new ArrayList<>();
    private final OnTripClickListener listener;

    public TripAdapter(OnTripClickListener listener) {
        this.listener = listener;
    }

    public void setTrips(List<TripItem> newTripItems) {
        List<TripItem> safeList = (newTripItems != null) ? newTripItems : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TripDiffCallback(this.tripItemList, safeList));
        this.tripItemList.clear();
        this.tripItemList.addAll(safeList);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTripBinding binding = ItemTripBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new TripViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        if (position >= 0 && position < tripItemList.size()) {
            holder.bind(tripItemList.get(position), listener);
        }
    }

    @Override
    public int getItemCount() {
        return tripItemList.size();
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        private final ItemTripBinding binding;

        public TripViewHolder(@NonNull ItemTripBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TripItem tripItem, OnTripClickListener listener) {
            if (tripItem == null) return;

            Context context = binding.getRoot().getContext();

            // Set Image Thumbnail
            if (tripItem.getImageResId() != 0) {
                binding.imgThumbnail.setImageResource(tripItem.getImageResId());
            } else {
                binding.imgThumbnail.setImageResource(R.drawable.img_phuquoc);
            }

            // Set Title, Status, Date
            binding.tvTitle.setText(tripItem.getTitle() != null ? tripItem.getTitle() : "");
            binding.tvStatusBadge.setText(tripItem.getStatus() != null ? tripItem.getStatus() : "");
            binding.tvDateTag.setText(tripItem.getDateTag() != null ? tripItem.getDateTag() : "");

            // Format & Style Details Text
            styleDetailsText(context, tripItem);

            // Configure Status Badge styling according to StatusType
            int pL = binding.tvStatusBadge.getPaddingLeft();
            int pT = binding.tvStatusBadge.getPaddingTop();
            int pR = binding.tvStatusBadge.getPaddingRight();
            int pB = binding.tvStatusBadge.getPaddingBottom();

            switch (tripItem.getStatusType()) {
                case DRAFT:
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange);
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_draft_text));
                    break;
                case COMPLETED:
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_gray);
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_completed_text));
                    break;
                case UPCOMING:
                default:
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_teal);
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_upcoming_text));
                    break;
            }

            // Restore padding in case background change resets it
            binding.tvStatusBadge.setPadding(pL, pT, pR, pB);

            // Item Click handling
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTripClick(tripItem);
                }
            });
        }

        private void styleDetailsText(Context context, TripItem tripItem) {
            String details = tripItem.getDetails();
            if (details == null) {
                binding.tvDetails.setText("");
                return;
            }

            if (details.contains("•")) {
                int dotIndex = details.indexOf("•");
                SpannableString spannable = new SpannableString(details);
                int tealColor = ContextCompat.getColor(context, R.color.teal_primary);

                // Highlight section after bullet point for draft trips (e.g., "12 mục đã lập")
                if (tripItem.getStatusType() == TripItem.StatusType.DRAFT && dotIndex + 1 < details.length()) {
                    spannable.setSpan(new ForegroundColorSpan(tealColor), dotIndex + 1, details.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), dotIndex + 1, details.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                binding.tvDetails.setText(spannable);
            } else {
                binding.tvDetails.setText(details);
            }
        }
    }

    private static class TripDiffCallback extends DiffUtil.Callback {
        private final List<TripItem> oldList;
        private final List<TripItem> newList;

        public TripDiffCallback(List<TripItem> oldList, List<TripItem> newList) {
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
            TripItem oldItem = oldList.get(oldItemPosition);
            TripItem newItem = newList.get(newItemPosition);
            if (oldItem.getId() != null && newItem.getId() != null) {
                return oldItem.getId().equals(newItem.getId());
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TripItem oldItem = oldList.get(oldItemPosition);
            TripItem newItem = newList.get(newItemPosition);
            return Objects.equals(oldItem.getTitle(), newItem.getTitle())
                    && Objects.equals(oldItem.getStatus(), newItem.getStatus())
                    && oldItem.getStatusType() == newItem.getStatusType()
                    && Objects.equals(oldItem.getDateTag(), newItem.getDateTag())
                    && Objects.equals(oldItem.getDetails(), newItem.getDetails())
                    && oldItem.getImageResId() == newItem.getImageResId();
        }
    }
}
