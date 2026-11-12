package com.example.mytripmatee;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytripmatee.databinding.FragmentItineraryBinding;

import java.util.ArrayList;
import java.util.List;

public class ItineraryFragment extends Fragment {

    private FragmentItineraryBinding binding;
    private ItineraryAdapter adapter;
    private int selectedDay = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentItineraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();

        binding.day1Pill.setOnClickListener(v -> selectDay(1));
        binding.day2Pill.setOnClickListener(v -> selectDay(2));
        binding.day3Pill.setOnClickListener(v -> selectDay(3));

        binding.btnForecast.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Xem dự báo thời tiết 3 ngày", Toast.LENGTH_SHORT).show()
        );

        // Open EditItineraryActivity when clicking "+ Thêm hoạt động" button or Floating Action Button
        View.OnClickListener openEditListener = v -> {
            Intent intent = EditItineraryActivity.newIntent(requireContext());
            startActivity(intent);
        };

        binding.btnAddActivity.setOnClickListener(openEditListener);
        binding.fabAddActivity.setOnClickListener(openEditListener);
    }

    private void setupRecyclerView() {
        adapter = new ItineraryAdapter(new ItineraryAdapter.OnItineraryItemClickListener() {
            @Override
            public void onItemClick(ItineraryItem item) {
                // Clicking an existing item opens EditItineraryActivity to edit
                Intent intent = EditItineraryActivity.newIntent(requireContext());
                startActivity(intent);
            }

            @Override
            public void onOpenMapsClick(ItineraryItem item) {
                Toast.makeText(requireContext(), "Đang mở Google Maps cho " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusCheckChanged(ItineraryItem item, boolean isChecked) {
                String state = isChecked ? "Đã xong: " : "Bỏ chọn: ";
                Toast.makeText(requireContext(), state + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvItinerary.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvItinerary.setAdapter(adapter);

        // Load default Day 1 data
        loadDayData(selectedDay);
    }

    private void selectDay(int dayNumber) {
        selectedDay = dayNumber;
        binding.day1Pill.setBackgroundResource(dayNumber == 1 ? R.drawable.bg_date_pill_selected : R.drawable.bg_date_pill_unselected);
        binding.day2Pill.setBackgroundResource(dayNumber == 2 ? R.drawable.bg_date_pill_selected : R.drawable.bg_date_pill_unselected);
        binding.day3Pill.setBackgroundResource(dayNumber == 3 ? R.drawable.bg_date_pill_selected : R.drawable.bg_date_pill_unselected);

        loadDayData(dayNumber);
    }

    private void loadDayData(int dayNumber) {
        List<ItineraryItem> items = new ArrayList<>();
        if (dayNumber == 1) {
            items.add(new ItineraryItem(
                    "d1_1",
                    ItineraryItem.ItemType.TRANSPORT,
                    "DI CHUYỂN",
                    "06:00 - 08:30",
                    "Bay Tân Sơn Nhất ➔ Liên Khương",
                    "Vietjet VJ362 • Mã vé: HG893X",
                    "Chi phí: 1.250.000 đ",
                    "Đã xong",
                    true,
                    false,
                    null
            ));
            items.add(new ItineraryItem(
                    "d1_2",
                    ItineraryItem.ItemType.DINING,
                    "ĂN UỐNG",
                    "09:15 - 10:15",
                    "Bánh căn Nhà Chung Đà Lạt",
                    "📍 Số 1 Nhà Chung, P.3 • Quán đông, nên đi sớm",
                    null,
                    "Đánh dấu đã đi",
                    false,
                    true,
                    "Cảnh báo: Khoảng cách từ Nhà Chung tới điểm kế tiếp 12km (~35p đèo)."
            ));
            items.add(new ItineraryItem(
                    "d1_3",
                    ItineraryItem.ItemType.STAY,
                    "LƯU TRÚ",
                    "11:00 - 12:00",
                    "Check-in Thung Lũng Mây Homestay",
                    "Nhận phòng view đồi • Mã đặt: AGD-8821",
                    "Chi phí: 1.800.000 đ / 2 đêm",
                    "Đã nhận phòng",
                    false,
                    false,
                    null
            ));
        } else if (dayNumber == 2) {
            items.add(new ItineraryItem(
                    "d2_1",
                    ItineraryItem.ItemType.DINING,
                    "ĂN UỐNG",
                    "06:30 - 08:30",
                    "Cà phê Túi Mơ To - Săn Mây",
                    "📍 Hẻm 31 Sào Nam, P.11 • Quán đông lúc sáng sớm",
                    null,
                    "Đánh dấu đã đi",
                    false,
                    true,
                    null
            ));
            items.add(new ItineraryItem(
                    "d2_2",
                    ItineraryItem.ItemType.SIGHTSEEING,
                    "CẢNH ĐẸP",
                    "09:00 - 11:30",
                    "Check-in Quảng trường Lâm Viên & Hồ Xuân Hương",
                    "Tham quan tự do, chụp ảnh nụ hoa Atiso",
                    "Vé vào cửa: Miễn phí",
                    "Đánh dấu đã đi",
                    false,
                    false,
                    null
            ));
        } else {
            items.add(new ItineraryItem(
                    "d3_1",
                    ItineraryItem.ItemType.DINING,
                    "ĂN UỐNG",
                    "11:30 - 13:00",
                    "Lẩu gà lá é Tao Ngộ",
                    "📍 3/4 Đường 3 Tháng 4 • Món ngon nổi tiếng",
                    null,
                    "Đã ăn",
                    false,
                    true,
                    null
            ));
            items.add(new ItineraryItem(
                    "d3_2",
                    ItineraryItem.ItemType.TRANSPORT,
                    "DI CHUYỂN",
                    "16:30 - 18:00",
                    "Bay Liên Khương ➔ Tân Sơn Nhất",
                    "Bamboo Airways QH1422",
                    "Chi phí: 1.150.000 đ",
                    "Chưa đi",
                    false,
                    false,
                    null
            ));
        }

        adapter.setItems(items);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
