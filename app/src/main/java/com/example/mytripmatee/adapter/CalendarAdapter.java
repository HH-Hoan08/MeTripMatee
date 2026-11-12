package com.example.mytripmatee.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    public static class DayItem {
        public final int dayNumber;
        public final boolean isCurrentMonth;

        public DayItem(int dayNumber, boolean isCurrentMonth) {
            this.dayNumber = dayNumber;
            this.isCurrentMonth = isCurrentMonth;
        }
    }

    public interface OnDayClickListener {
        void onDayClick(int dayNumber);
    }

    private final List<DayItem> days;
    private final OnDayClickListener listener;
    private Integer startDate = 12;
    private Integer endDate = 15;

    public CalendarAdapter(List<DayItem> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
    }

    public void setSelectedRange(Integer startDate, Integer endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayItem item = days.get(position);
        holder.tvDayNumber.setText(String.valueOf(item.dayNumber));

        if (!item.isCurrentMonth) {
            holder.tvDayNumber.setTextColor(Color.parseColor("#CCCCCC"));
            holder.tvDayNumber.setTypeface(null, Typeface.NORMAL);
            holder.viewCircleHighlight.setVisibility(View.GONE);
            holder.viewRangeHighlight.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            return;
        }

        boolean isStart = startDate != null && item.dayNumber == startDate.intValue();
        boolean isEnd = endDate != null && item.dayNumber == endDate.intValue();
        boolean inRange = startDate != null && endDate != null
                && item.dayNumber > startDate && item.dayNumber < endDate;

        if (isStart || isEnd) {
            holder.viewCircleHighlight.setVisibility(View.VISIBLE);
            holder.viewRangeHighlight.setVisibility(endDate != null ? View.VISIBLE : View.GONE);
            holder.tvDayNumber.setTextColor(Color.WHITE);
            holder.tvDayNumber.setTypeface(null, Typeface.BOLD);
        } else if (inRange) {
            holder.viewCircleHighlight.setVisibility(View.GONE);
            holder.viewRangeHighlight.setVisibility(View.VISIBLE);
            holder.tvDayNumber.setTextColor(Color.parseColor("#00896A"));
            holder.tvDayNumber.setTypeface(null, Typeface.BOLD);
        } else {
            holder.viewCircleHighlight.setVisibility(View.GONE);
            holder.viewRangeHighlight.setVisibility(View.GONE);
            holder.tvDayNumber.setTextColor(Color.parseColor("#1A1A1A"));
            holder.tvDayNumber.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDayClick(item.dayNumber);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        final View viewRangeHighlight;
        final View viewCircleHighlight;
        final TextView tvDayNumber;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            viewRangeHighlight = itemView.findViewById(R.id.viewRangeHighlight);
            viewCircleHighlight = itemView.findViewById(R.id.viewCircleHighlight);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
        }
    }
}
