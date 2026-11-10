package com.example.mytripmatee;

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

import com.example.mytripmatee.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

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

        setupViewPagerAndTabs();
    }

    private void setupViewPagerAndTabs() {
        MainPagerAdapter adapter = new MainPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Khám phá");
                    tab.setIcon(R.drawable.ic_explore);
                    break;
                case 1:
                    tab.setText("Lịch trình");
                    tab.setIcon(R.drawable.ic_calendar_month);
                    break;
                case 2:
                    tab.setText("Bản đồ");
                    tab.setIcon(R.drawable.ic_map);
                    break;
                case 3:
                    tab.setText("Ví chi tiêu");
                    tab.setIcon(R.drawable.ic_wallet);
                    break;
            }
        }).attach();
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
                case 1:
                    return new ItineraryFragment();
                case 2:
                    return new MapFragment();
                case 3:
                    return new ExpenseFragment();
                case 0:
                default:
                    return new ExploreFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}