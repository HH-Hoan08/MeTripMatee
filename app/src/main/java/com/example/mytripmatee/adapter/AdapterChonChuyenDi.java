package com.example.mytripmatee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;
import com.example.mytripmatee.model.ChuyenDi;

import java.util.ArrayList;
import java.util.List;

public class AdapterChonChuyenDi extends RecyclerView.Adapter<AdapterChonChuyenDi.SelectTripViewHolder> {

    public interface OnTripSelectedListener {
        void onTripSelected(int tripId, ChuyenDi trip, String tripName);
    }

    private final List<ChuyenDi> danhSachChuyenDi = new ArrayList<>();
    private final OnTripSelectedListener listener;
    private int selectedTripId = -1; // -1 = Tất cả chuyến đi

    public AdapterChonChuyenDi(OnTripSelectedListener listener) {
        this.listener = listener;
    }

    public void setDanhSachChuyenDi(List<ChuyenDi> list, int currentSelectedTripId) {
        this.danhSachChuyenDi.clear();
        if (list != null) {
            this.danhSachChuyenDi.addAll(list);
        }
        this.selectedTripId = currentSelectedTripId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SelectTripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_trip, parent, false);
        return new SelectTripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectTripViewHolder holder, int position) {
        if (position == 0) {
            holder.bindAllTrips(selectedTripId == -1, listener);
        } else {
            ChuyenDi trip = danhSachChuyenDi.get(position - 1);
            holder.bindTrip(trip, trip.getMaChuyenDi() == selectedTripId, listener);
        }
    }

    @Override
    public int getItemCount() {
        return danhSachChuyenDi.size() + 1; // +1 for "Tất cả chuyến đi"
    }

        static class SelectTripViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvSub;
        private final ImageView imgCheck;
        private final View rootItem;

        public SelectTripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTripName);
            tvSub = itemView.findViewById(R.id.tvTripSub);
            imgCheck = itemView.findViewById(R.id.imgSelectedCheck);
            rootItem = itemView.findViewById(R.id.rootTripItem);
        }

        public void bindAllTrips(boolean isSelected, OnTripSelectedListener listener) {
            tvName.setText("Tất cả chuyến đi");
            tvSub.setText("Hiển thị chi phí của toàn bộ chuyến đi");
            imgCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            View.OnClickListener clickListener = v -> {
                if (listener != null) listener.onTripSelected(-1, null, "Tất cả chuyến đi");
            };
            itemView.setOnClickListener(clickListener);
            if (rootItem != null) rootItem.setOnClickListener(clickListener);
        }

        public void bindTrip(ChuyenDi trip, boolean isSelected, OnTripSelectedListener listener) {
            String name = trip.getTenChuyenDi() != null ? trip.getTenChuyenDi() : "Chuyến đi";
            tvName.setText(name);
            String dateRange = (trip.getNgayDi() != null ? trip.getNgayDi() : "") + " → " + (trip.getNgayVe() != null ? trip.getNgayVe() : "");
            String sub = (trip.getDiemDen() != null ? trip.getDiemDen() : "") + " • " + dateRange;
            tvSub.setText(sub);
            imgCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            View.OnClickListener clickListener = v -> {
                if (listener != null) listener.onTripSelected(trip.getMaChuyenDi(), trip, name);
            };
            itemView.setOnClickListener(clickListener);
            if (rootItem != null) rootItem.setOnClickListener(clickListener);
        }
    }
}
