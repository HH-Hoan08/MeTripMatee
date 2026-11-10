package com.example.mytripmatee;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytripmatee.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private TripAdapter tripAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setupRecyclerView();
        loadMockData();
        setupClickListeners();
    }

    private void setupRecyclerView() {
        tripAdapter = new TripAdapter(trip ->
            Toast.makeText(MainActivity.this, "Đã chọn: " + trip.getTitle(), Toast.LENGTH_SHORT).show()
        );
        binding.rvTrips.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTrips.setAdapter(tripAdapter);
    }

    private void loadMockData() {
        List<Trip> trips = new ArrayList<>();

        // Item 1: Draft item from screenshot
        trips.add(new Trip(
                "1",
                "Phú Quốc – Nghỉ Dưỡng Biển Ngọc",
                "Bản nháp",
                Trip.StatusType.DRAFT,
                "Tết 2027",
                "4 ngày 3 đêm  •  12 mục đã lập",
                R.drawable.img_phuquoc
        ));

        // Item 2: Completed item from screenshot
        trips.add(new Trip(
                "2",
                "Hà Giang Loop – Chinh Phục Mã Pí L...",
                "Đã kết thúc",
                Trip.StatusType.COMPLETED,
                "Tháng 09/2026",
                "Tổng chi: 4.800.000 đ  •  Đánh giá 5★",
                R.drawable.img_hagiang
        ));

        // Item 3: Additional mock trip
        trips.add(new Trip(
                "3",
                "Nha Trang – Biển Xanh Nắng Vàng",
                "Sắp tới",
                Trip.StatusType.UPCOMING,
                "Tháng 05/2026",
                "3 ngày 2 đêm  •  5 thành viên",
                R.drawable.img_dalat
        ));

        tripAdapter.setTrips(trips);
    }

    private void setupClickListeners() {
        binding.btnNotification.setOnClickListener(v ->
            Toast.makeText(this, "Thông báo mới", Toast.LENGTH_SHORT).show()
        );

        binding.btnSettings.setOnClickListener(v ->
            Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show()
        );

        binding.tvViewHistory.setOnClickListener(v ->
            Toast.makeText(this, "Xem lịch sử chuyến đi", Toast.LENGTH_SHORT).show()
        );

        binding.fabAdd.setOnClickListener(v ->
            Toast.makeText(this, "Tạo chuyến đi mới", Toast.LENGTH_SHORT).show()
        );

        // Filter tabs click handling
        binding.tabAll.setOnClickListener(v -> selectTab(binding.tabAll, "Tất cả"));
        binding.tabUpcoming.setOnClickListener(v -> selectTab(binding.tabUpcoming, "Sắp tới"));
        binding.tabDraft.setOnClickListener(v -> selectTab(binding.tabDraft, "Bản nháp"));
        binding.tabDone.setOnClickListener(v -> selectTab(binding.tabDone, "Đã xong"));
    }

    private void selectTab(android.widget.TextView selected, String tabName) {
        binding.tabAll.setBackgroundResource(R.drawable.bg_pill_white);
        binding.tabAll.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));

        binding.tabUpcoming.setBackgroundResource(R.drawable.bg_pill_white);
        binding.tabUpcoming.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));

        binding.tabDraft.setBackgroundResource(R.drawable.bg_pill_white);
        binding.tabDraft.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));

        binding.tabDone.setBackgroundResource(R.drawable.bg_pill_white);
        binding.tabDone.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));

        selected.setBackgroundResource(R.drawable.bg_pill_teal);
        selected.setTextColor(getResources().getColor(android.R.color.white, getTheme()));

        Toast.makeText(this, "Lọc: " + tabName, Toast.LENGTH_SHORT).show();
    }
}