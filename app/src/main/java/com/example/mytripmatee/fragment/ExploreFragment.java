package com.example.mytripmatee.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytripmatee.R;
import com.example.mytripmatee.activity.SettingsActivity;
import com.example.mytripmatee.activity.TripDetailActivity;
import com.example.mytripmatee.adapter.TripAdapter;
import com.example.mytripmatee.databinding.FragmentExploreBinding;
import com.example.mytripmatee.model.TripItem;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private TripAdapter tripAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFeaturedCard();
        setupSettingsButton();
    }

    private void setupRecyclerView() {
        tripAdapter = new TripAdapter(tripItem -> {
            if (getContext() != null) {
                Intent intent = TripDetailActivity.newIntent(
                        getContext(),
                        tripItem.getTitle(),
                        tripItem.getDateTag() != null ? tripItem.getDateTag() : "Chi tiết chuyến đi"
                );
                startActivity(intent);
            }
        });

        binding.rvTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTrips.setAdapter(tripAdapter);

        tripAdapter.setTrips(getSampleTrips());
    }

    private void setupFeaturedCard() {
        binding.cardFeatured.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = TripDetailActivity.newIntent(
                        getContext(),
                        "Đà Lạt – Sương Mù & Thảnh Thơi",
                        "12/11 - 15/11/2026 • 3 ngày 2 đêm"
                );
                startActivity(intent);
            }
        });
    }

    private void setupSettingsButton() {
        binding.btnSettings.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private List<TripItem> getSampleTrips() {
        List<TripItem> tripItems = new ArrayList<>();
        tripItems.add(new TripItem(
                "1",
                "Phú Quốc – Nghỉ Dưỡng Biển Ngọc",
                "Bản nháp",
                TripItem.StatusType.DRAFT,
                "Tết 2027",
                "4 ngày 3 đêm • 12 mục đã lập",
                R.drawable.img_phuquoc
        ));
        tripItems.add(new TripItem(
                "2",
                "Hà Giang – Chinh Phục Mã Pí Lèng",
                "Đã xong",
                TripItem.StatusType.COMPLETED,
                "T10/2026",
                "3 ngày 2 đêm • Hoàn thành 100%",
                R.drawable.img_hagiang
        ));
        tripItems.add(new TripItem(
                "3",
                "Đà Lạt – Sương Mù & Thảnh Thơi",
                "Sắp tới",
                TripItem.StatusType.UPCOMING,
                "12/11 - 15/11/2026",
                "3 ngày 2 đêm • Đang đếm ngược",
                R.drawable.img_dalat
        ));
        return tripItems;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
