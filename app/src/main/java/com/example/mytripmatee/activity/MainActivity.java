package com.example.mytripmatee.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mytripmatee.R;
import com.example.mytripmatee.databinding.ActivityMainBinding;
import com.example.mytripmatee.fragment.BanDoFragment;
import com.example.mytripmatee.fragment.ChiPhiFragment;
import com.example.mytripmatee.fragment.KhamPhaFragment;
import com.example.mytripmatee.fragment.LichTrinhFragment;
import com.example.mytripmatee.util.QuanLyPhien;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean isSyncingTab = false;
    private final TabLayout.Tab[] allTabs = new TabLayout.Tab[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QuanLyPhien quanLyPhien = new QuanLyPhien(this);
        if (!quanLyPhien.daDangNhap()) {
            Intent intent = new Intent(this, DangNhapActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        android.content.SharedPreferences prefs = getSharedPreferences(CaiDatActivity.PREFS_NAME, MODE_PRIVATE);
        if (prefs.contains(CaiDatActivity.KEY_DARK_MODE)) {
            boolean isDark = prefs.getBoolean(CaiDatActivity.KEY_DARK_MODE, false);
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

    private TabLayout.Tab createTab(TabLayout tabLayout, String title, int iconRes) {
        View tabView = LayoutInflater.from(this).inflate(R.layout.item_bottom_tab, tabLayout, false);
        ImageView ivIcon = tabView.findViewById(R.id.ivTabIcon);
        TextView tvTitle = tabView.findViewById(R.id.tvTabTitle);
        ivIcon.setImageResource(iconRes);
        tvTitle.setText(title);
        return tabLayout.newTab().setCustomView(tabView);
    }

    private void setupViewPagerAndTabs() {
        binding.viewPager.setAdapter(new MainPagerAdapter(this));

        allTabs[0] = createTab(binding.tabLayoutLeft, "Khám phá", R.drawable.ic_explore);
        allTabs[1] = createTab(binding.tabLayoutLeft, "Lịch trình", R.drawable.ic_calendar_month);
        binding.tabLayoutLeft.addTab(allTabs[0], true);
        binding.tabLayoutLeft.addTab(allTabs[1], false);

        allTabs[2] = createTab(binding.tabLayoutRight, "Bản đồ", R.drawable.ic_map);
        allTabs[3] = createTab(binding.tabLayoutRight, "Ví chi tiêu", R.drawable.ic_wallet);
        binding.tabLayoutRight.addTab(allTabs[2], false);
        binding.tabLayoutRight.addTab(allTabs[3], false);

        binding.tabLayoutLeft.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onTabClicked(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabClicked(tab.getPosition());
            }
        });

        binding.tabLayoutRight.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onTabClicked(tab.getPosition() + 2);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabClicked(tab.getPosition() + 2);
            }
        });

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (isSyncingTab) return;
                isSyncingTab = true;
                updateTabSelection(position);
                isSyncingTab = false;
            }
        });

        updateTabSelection(0);
    }

    private void onTabClicked(int position) {
        if (isSyncingTab) return;
        isSyncingTab = true;
        binding.viewPager.setCurrentItem(position, true);
        updateTabSelection(position);
        isSyncingTab = false;
    }

    private void updateTabSelection(int position) {
        int tealColor = ContextCompat.getColor(this, R.color.teal_primary);
        int mutedColor = ContextCompat.getColor(this, R.color.text_muted);

        if (position < 2) {
            TabLayout.Tab tabLeft = binding.tabLayoutLeft.getTabAt(position);
            if (tabLeft != null && !tabLeft.isSelected()) {
                tabLeft.select();
            }
        } else {
            TabLayout.Tab tabRight = binding.tabLayoutRight.getTabAt(position - 2);
            if (tabRight != null && !tabRight.isSelected()) {
                tabRight.select();
            }
        }

        for (int i = 0; i < allTabs.length; i++) {
            TabLayout.Tab tab = allTabs[i];
            if (tab == null || tab.getCustomView() == null) continue;
            View customView = tab.getCustomView();
            ImageView ivIcon = customView.findViewById(R.id.ivTabIcon);
            TextView tvTitle = customView.findViewById(R.id.tvTabTitle);
            View indicator = customView.findViewById(R.id.viewTabIndicator);

            boolean isSelected = (i == position);
            int color = isSelected ? tealColor : mutedColor;
            if (ivIcon != null) {
                ImageViewCompat.setImageTintList(ivIcon, ColorStateList.valueOf(color));
            }
            if (tvTitle != null) {
                tvTitle.setTextColor(color);
            }
            if (indicator != null) {
                indicator.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    private void setupAddTripButton() {
        binding.btnAddTrip.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SuaChuyenDiActivity.class);
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
                case 1:  return new LichTrinhFragment();
                case 2:  return new BanDoFragment();
                case 3:  return new ChiPhiFragment();
                default: return new KhamPhaFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
