package com.example.mytripmatee.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;
import com.example.mytripmatee.databinding.ItemItineraryBinding;
import com.example.mytripmatee.model.ItineraryItem;

import java.util.ArrayList;
import java.util.List;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder> {

    public interface OnItineraryItemClickListener {
        void onItemClick(ItineraryItem item);
        void onOpenMapsClick(ItineraryItem item);
        void onStatusCheckChanged(ItineraryItem item, boolean isChecked);
    }

    private final List<ItineraryItem> itemList = new ArrayList<>();
    private final OnItineraryItemClickListener listener;

    public ItineraryAdapter(OnItineraryItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ItineraryItem> newItems) {
        this.itemList.clear();
        if (newItems != null) {
            this.itemList.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItineraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemItineraryBinding binding = ItemItineraryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ItineraryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItineraryViewHolder holder, int position) {
        if (position >= 0 && position < itemList.size()) {
            holder.bind(itemList.get(position), position, itemList.size(), listener);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItineraryViewHolder extends RecyclerView.ViewHolder {
        private final ItemItineraryBinding binding;

        public ItineraryViewHolder(@NonNull ItemItineraryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ItineraryItem item, int position, int totalSize, OnItineraryItemClickListener listener) {
            if (item == null) return;

            Context context = binding.getRoot().getContext();

            // Timeline continuous line visibility
            binding.timelineLineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
            binding.timelineLineBottom.setVisibility(position == totalSize - 1 ? View.INVISIBLE : View.VISIBLE);

            // Badge, node circle, and icon configuration based on ItemType
            switch (item.getItemType()) {
                case DINING:
                    binding.tvBadgeType.setBackgroundResource(R.drawable.bg_badge_amber);
                    binding.tvBadgeType.setTextColor(ContextCompat.getColor(context, R.color.badge_amber_text));
                    binding.nodeCircle.setBackgroundResource(R.drawable.bg_node_circle_orange);
                    binding.imgNodeIcon.setImageResource(R.drawable.ic_restaurant);
                    break;
                case STAY:
                    binding.tvBadgeType.setBackgroundResource(R.drawable.bg_badge_stay);
                    binding.tvBadgeType.setTextColor(ContextCompat.getColor(context, R.color.badge_stay_text));
                    binding.nodeCircle.setBackgroundResource(R.drawable.bg_node_circle_purple);
                    binding.imgNodeIcon.setImageResource(R.drawable.ic_hotel);
                    break;
                case SIGHTSEEING:
                    binding.tvBadgeType.setBackgroundResource(R.drawable.bg_badge_teal);
                    binding.tvBadgeType.setTextColor(ContextCompat.getColor(context, R.color.teal_primary));
                    binding.nodeCircle.setBackgroundResource(R.drawable.bg_node_circle_blue);
                    binding.imgNodeIcon.setImageResource(R.drawable.ic_explore);
                    break;
                case TRANSPORT:
                default:
                    binding.tvBadgeType.setBackgroundResource(R.drawable.bg_badge_move);
                    binding.tvBadgeType.setTextColor(ContextCompat.getColor(context, R.color.badge_move_text));
                    binding.nodeCircle.setBackgroundResource(R.drawable.bg_node_circle_blue);
                    binding.imgNodeIcon.setImageResource(R.drawable.ic_pin_flight);
                    break;
            }

            // Fill Data
            binding.tvBadgeType.setText(item.getTypeLabel() != null ? item.getTypeLabel() : "");
            binding.tvTimeRange.setText(item.getTimeRange() != null ? item.getTimeRange() : "");
            binding.tvTitle.setText(item.getTitle() != null ? item.getTitle() : "");
            binding.tvDescription.setText(item.getDescription() != null ? item.getDescription() : "");

            // Maps button vs SubInfo
            if (item.hasMaps()) {
                binding.btnOpenMaps.setVisibility(View.VISIBLE);
                binding.tvSubInfo.setVisibility(View.GONE);
                binding.btnOpenMaps.setOnClickListener(v -> {
                    if (listener != null) listener.onOpenMapsClick(item);
                });
            } else {
                binding.btnOpenMaps.setVisibility(View.GONE);
                binding.tvSubInfo.setVisibility(View.VISIBLE);
                binding.tvSubInfo.setText(item.getSubInfo() != null ? item.getSubInfo() : "");
            }

            // Checkbox
            binding.cbDone.setOnCheckedChangeListener(null);
            binding.cbDone.setText(item.getStatusText() != null ? item.getStatusText() : "");
            binding.cbDone.setChecked(item.isDone());
            binding.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setDone(isChecked);
                if (listener != null) {
                    listener.onStatusCheckChanged(item, isChecked);
                }
            });

            // Alert Box
            if (item.getAlertText() != null && !item.getAlertText().isEmpty()) {
                binding.layoutAlert.setVisibility(View.VISIBLE);
                binding.tvAlertText.setText(item.getAlertText());
            } else {
                binding.layoutAlert.setVisibility(View.GONE);
            }

            // Item click
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
