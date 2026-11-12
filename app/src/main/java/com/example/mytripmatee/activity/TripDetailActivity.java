package com.example.mytripmatee.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mytripmatee.R;
import com.example.mytripmatee.databinding.ActivityTripDetailBinding;
import com.example.mytripmatee.fragment.ItineraryFragment;
import com.example.mytripmatee.fragment.TripExpenseFragment;
import com.example.mytripmatee.fragment.TripGalleryFragment;
import com.example.mytripmatee.fragment.TripStatsFragment;
import com.google.android.material.tabs.TabLayoutMediator;

public class TripDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_TITLE = "extra_trip_title";
    public static final String EXTRA_TRIP_DATE = "extra_trip_date";

    public static Intent newIntent(Context context, String title, String dateTag) {
        Intent intent = new Intent(context, TripDetailActivity.class);
        intent.putExtra(EXTRA_TRIP_TITLE, title);
        intent.putExtra(EXTRA_TRIP_DATE, dateTag);
        return intent;
    }

    private ActivityTripDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());

        binding = ActivityTripDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainDetail, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupHeader();
        setupViewPagerAndTabs();
    }

    private void setupHeader() {
        String title = getIntent().getStringExtra(EXTRA_TRIP_TITLE);
        String dateTag = getIntent().getStringExtra(EXTRA_TRIP_DATE);

        if (title != null && !title.isEmpty()) {
            binding.tvTripTitle.setText(title);
        }
        if (dateTag != null && !dateTag.isEmpty()) {
            binding.tvTripSubtitle.setText(dateTag);
        }

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupViewPagerAndTabs() {
        TripDetailPagerAdapter adapter = new TripDetailPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        binding.viewPager.setOffscreenPageLimit(3);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Lịch trình");
                    tab.setIcon(R.drawable.ic_calendar_month);
                    break;
                case 1:
                    tab.setText("Chi phí");
                    tab.setIcon(R.drawable.ic_wallet);
                    break;
                case 2:
                    tab.setText("Thống kê");
                    tab.setIcon(R.drawable.ic_stats);
                    break;
                case 3:
                    tab.setText("Gallery");
                    tab.setIcon(R.drawable.ic_gallery);
                    break;
            }
        }).attach();
    }

    private static class TripDetailPagerAdapter extends FragmentStateAdapter {

        public TripDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ItineraryFragment();
                case 1:
                    return new TripExpenseFragment();
                case 2:
                    return new TripStatsFragment();
                case 3:
                default:
                    return new TripGalleryFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
