package com.example.mytripmatee;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mytripmatee.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean isSyncingTab = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.content.SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        if (prefs.contains(SettingsActivity.KEY_DARK_MODE)) {
            boolean isDark = prefs.getBoolean(SettingsActivity.KEY_DARK_MODE, false);
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    isDark ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            );
        }

        WindowCompat.enableEdgeToEdge(getWindow());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setupViewPagerAndTabs();
        setupAddTripButton();
    }

    private void setupViewPagerAndTabs() {
        binding.viewPager.setAdapter(new MainPagerAdapter(this));

        // Add 2 tabs to Left TabLayout (Khám phá, Lịch trình)
        TabLayout.Tab tab0 = binding.tabLayoutLeft.newTab().setText("Khám phá").setIcon(R.drawable.ic_explore);
        TabLayout.Tab tab1 = binding.tabLayoutLeft.newTab().setText("Lịch trình").setIcon(R.drawable.ic_calendar_month);
        binding.tabLayoutLeft.addTab(tab0);
        binding.tabLayoutLeft.addTab(tab1);

        // Add 2 tabs to Right TabLayout (Bản đồ, Ví chi tiêu)
        TabLayout.Tab tab2 = binding.tabLayoutRight.newTab().setText("Bản đồ").setIcon(R.drawable.ic_map);
        TabLayout.Tab tab3 = binding.tabLayoutRight.newTab().setText("Ví chi tiêu").setIcon(R.drawable.ic_wallet);
        binding.tabLayoutRight.addTab(tab2);
        binding.tabLayoutRight.addTab(tab3);

        int tealColor = ContextCompat.getColor(this, R.color.teal_primary);

        // Left Tab selection
        binding.tabLayoutLeft.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (isSyncingTab) return;
                isSyncingTab = true;
                int pos = tab.getPosition();
                binding.viewPager.setCurrentItem(pos, true);
                binding.tabLayoutLeft.setSelectedTabIndicatorColor(tealColor);
                binding.tabLayoutRight.setSelectedTabIndicatorColor(Color.TRANSPARENT);
                isSyncingTab = false;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Right Tab selection
        binding.tabLayoutRight.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (isSyncingTab) return;
                isSyncingTab = true;
                int pos = tab.getPosition() + 2;
                binding.viewPager.setCurrentItem(pos, true);
                binding.tabLayoutRight.setSelectedTabIndicatorColor(tealColor);
                binding.tabLayoutLeft.setSelectedTabIndicatorColor(Color.TRANSPARENT);
                isSyncingTab = false;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Sync tabs when ViewPager2 is swiped
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (isSyncingTab) return;
                isSyncingTab = true;

                if (position < 2) {
                    binding.tabLayoutLeft.setSelectedTabIndicatorColor(tealColor);
                    binding.tabLayoutRight.setSelectedTabIndicatorColor(Color.TRANSPARENT);
                    TabLayout.Tab tab = binding.tabLayoutLeft.getTabAt(position);
                    if (tab != null && !tab.isSelected()) {
                        tab.select();
                    }
                } else {
                    int rightIndex = position - 2;
                    binding.tabLayoutRight.setSelectedTabIndicatorColor(tealColor);
                    binding.tabLayoutLeft.setSelectedTabIndicatorColor(Color.TRANSPARENT);
                    TabLayout.Tab tab = binding.tabLayoutRight.getTabAt(rightIndex);
                    if (tab != null && !tab.isSelected()) {
                        tab.select();
                    }
                }

                isSyncingTab = false;
            }
        });
    }

    private void setupAddTripButton() {
        // Nút dấu cộng (+) chính giữa mở trang EditTripActivity để tạo chuyến đi mới
        binding.btnAddTrip.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditTripActivity.class);
            intent.putExtra("mode_create", true);
            startActivity(intent);
        });
    }

    public void selectTab(int position) {
        if (binding != null && binding.viewPager != null) {
            binding.viewPager.setCurrentItem(position, true);
        }
    }

    private static class MainPagerAdapter extends FragmentStateAdapter {
        public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1:  return new ItineraryFragment();
                case 2:  return new MapFragment();
                case 3:  return new ExpenseFragment();
                default: return new ExploreFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
