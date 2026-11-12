package com.example.mytripmatee;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.databinding.ItemExpenseBinding;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseClickListener {
        void onExpenseClick(ExpenseItem item);
    }

    private List<ExpenseItem> expenseList = new ArrayList<>();
    private final OnExpenseClickListener listener;

    public ExpenseAdapter(OnExpenseClickListener listener) {
        this.listener = listener;
    }

    public void setExpenses(List<ExpenseItem> expenses) {
        this.expenseList = expenses != null ? expenses : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addExpense(ExpenseItem item) {
        if (item != null) {
            this.expenseList.add(0, item);
            notifyItemInserted(0);
        }
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExpenseBinding binding = ItemExpenseBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ExpenseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.bind(expenseList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final ItemExpenseBinding binding;

        public ExpenseViewHolder(@NonNull ItemExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ExpenseItem item, OnExpenseClickListener listener) {
            binding.tvTitle.setText(item.getTitle());
            binding.tvSubtitle.setText(item.getSubtitle());
            binding.tvAmount.setText(item.getFormattedAmount());

            // Set Category Icon and background style
            switch (item.getCategoryType()) {
                case MOVE:
                    binding.iconContainer.setBackgroundResource(R.drawable.bg_badge_move);
                    binding.imgCategoryIcon.setImageResource(R.drawable.ic_pin_flight);
                    binding.imgCategoryIcon.setColorFilter(Color.parseColor("#4338CA"));
                    break;
                case FOOD:
                    binding.iconContainer.setBackgroundResource(R.drawable.bg_badge_amber);
                    binding.imgCategoryIcon.setImageResource(R.drawable.ic_restaurant);
                    binding.imgCategoryIcon.setColorFilter(Color.parseColor("#B45309"));
                    break;
                case STAY:
                    binding.iconContainer.setBackgroundResource(R.drawable.bg_badge_stay);
                    binding.imgCategoryIcon.setImageResource(R.drawable.ic_hotel);
                    binding.imgCategoryIcon.setColorFilter(Color.parseColor("#7E22CE"));
                    break;
                case OTHER:
                default:
                    binding.iconContainer.setBackgroundResource(R.drawable.bg_badge_teal);
                    binding.imgCategoryIcon.setImageResource(R.drawable.ic_wallet);
                    binding.imgCategoryIcon.setColorFilter(Color.parseColor("#0D9488"));
                    break;
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExpenseClick(item);
                }
            });
        }
    }
}
