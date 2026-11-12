package com.example.mytripmatee;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytripmatee.databinding.FragmentExploreBinding;

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
        tripAdapter = new TripAdapter(trip -> {
            // When any random trip item in the trip list is clicked, transition directly to TripDetailActivity
            if (getContext() != null) {
                Intent intent = TripDetailActivity.newIntent(
                        getContext(),
                        trip.getTitle(),
                        trip.getDateTag() != null ? trip.getDateTag() : "Chi tiết chuyến đi"
                );
                startActivity(intent);
            }
        });

        binding.rvTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTrips.setAdapter(tripAdapter);

        // Load sample trips data
        tripAdapter.setTrips(getSampleTrips());
    }

    private void setupFeaturedCard() {
        // When clicking the featured trip card, transition directly to TripDetailActivity
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

    private List<Trip> getSampleTrips() {
        List<Trip> trips = new ArrayList<>();
        trips.add(new Trip(
                "1",
                "Phú Quốc – Nghỉ Dưỡng Biển Ngọc",
                "Bản nháp",
                Trip.StatusType.DRAFT,
                "Tết 2027",
                "4 ngày 3 đêm • 12 mục đã lập",
                R.drawable.img_phuquoc
        ));
        trips.add(new Trip(
                "2",
                "Hà Giang – Chinh Phục Mã Pí Lèng",
                "Đã xong",
                Trip.StatusType.COMPLETED,
                "T10/2026",
                "3 ngày 2 đêm • Hoàn thành 100%",
                R.drawable.img_hagiang
        ));
        trips.add(new Trip(
                "3",
                "Đà Lạt – Sương Mù & Thảnh Thơi",
                "Sắp tới",
                Trip.StatusType.UPCOMING,
                "12/11 - 15/11/2026",
                "3 ngày 2 đêm • Đang đếm ngược",
                R.drawable.img_dalat
        ));
        return trips;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
