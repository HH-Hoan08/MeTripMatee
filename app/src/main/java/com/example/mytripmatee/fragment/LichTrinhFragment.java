package com.example.mytripmatee.fragment;

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

import com.example.mytripmatee.R;
import com.example.mytripmatee.activity.SuaLichTrinhActivity;
import com.example.mytripmatee.adapter.AdapterLichTrinh;
import com.example.mytripmatee.dao.LichTrinhDAO;
import com.example.mytripmatee.databinding.FragmentLichTrinhBinding;
import com.example.mytripmatee.model.LichTrinh;

import java.util.ArrayList;
import java.util.List;

public class LichTrinhFragment extends Fragment {

    private FragmentLichTrinhBinding binding;
    private AdapterLichTrinh adapter;
    private LichTrinhDAO lichTrinhDAO;
    private int maChuyenDi = 1;
    private int selectedDay = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLichTrinhBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lichTrinhDAO = new LichTrinhDAO(requireContext());

        if (getArguments() != null) {
            maChuyenDi = getArguments().getInt("maChuyenDi", 1);
        } else if (requireActivity().getIntent() != null) {
            maChuyenDi = requireActivity().getIntent().getIntExtra("maChuyenDi", 1);
        }

        setupRecyclerView();

        binding.day1Pill.setOnClickListener(v -> selectDay(1));
        binding.day2Pill.setOnClickListener(v -> selectDay(2));
        binding.day3Pill.setOnClickListener(v -> selectDay(3));

        binding.btnForecast.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Xem dự báo thời tiết 3 ngày", Toast.LENGTH_SHORT).show()
        );

        View.OnClickListener openEditListener = v -> {
            Intent intent = SuaLichTrinhActivity.newIntent(requireContext());
            intent.putExtra("maChuyenDi", maChuyenDi);
            startActivity(intent);
        };

        binding.btnAddActivity.setOnClickListener(openEditListener);
        binding.fabAddActivity.setOnClickListener(openEditListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDayData(selectedDay);
    }

    private void setupRecyclerView() {
        adapter = new AdapterLichTrinh(new AdapterLichTrinh.SuKienChonLichTrinhListener() {
            @Override
            public void onChonMuc(LichTrinh item) {
                if (item == null) return;
                Intent intent = SuaLichTrinhActivity.newIntent(requireContext());
                intent.putExtra("maHoatDong", item.getMaHoatDong());
                intent.putExtra("maChuyenDi", item.getMaChuyenDi());
                startActivity(intent);
            }

            @Override
            public void onMoBanDo(LichTrinh item) {
                if (item != null && item.getLinkBanDo() != null && !item.getLinkBanDo().isEmpty()) {
                    Toast.makeText(requireContext(), "Đang mở bản đồ: " + item.getTieuDe(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Không có link bản đồ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onThayDoiCheck(LichTrinh item, boolean isChecked) {
                if (item == null) return;
                boolean success = lichTrinhDAO.capNhatTrangThaiDaDi(item.getMaHoatDong(), isChecked);
                if (success) {
                    item.setDaDi(isChecked);
                    String state = isChecked ? "Đã xong: " : "Bỏ chọn: ";
                    Toast.makeText(requireContext(), state + item.getTieuDe(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Lỗi khi cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.rvItinerary.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvItinerary.setAdapter(adapter);
    }

    private void selectDay(int dayNumber) {
        selectedDay = dayNumber;
        binding.day1Pill.setBackgroundResource(dayNumber == 1 ? R.drawable.bg_date_pill_selected : R.drawable.bg_date_pill_unselected);
        binding.day2Pill.setBackgroundResource(dayNumber == 2 ? R.drawable.bg_date_pill_selected : R.drawable.bg_date_pill_unselected);
        binding.day3Pill.setBackgroundResource(dayNumber == 3 ? R.drawable.bg_date_pill_selected : R.drawable.bg_date_pill_unselected);

        loadDayData(dayNumber);
    }

    private void loadDayData(int dayNumber) {
        List<LichTrinh> allItems = lichTrinhDAO.layDanhSachHoatDongTheoChuyenDi(maChuyenDi);

        if (allItems == null || allItems.isEmpty()) {
            adapter.setDanhSachLichTrinh(new ArrayList<>());
            return;
        }

        List<String> distinctDates = new ArrayList<>();
        for (LichTrinh lt : allItems) {
            if (lt.getNgayHoatDong() != null && !distinctDates.contains(lt.getNgayHoatDong())) {
                distinctDates.add(lt.getNgayHoatDong());
            }
        }

        List<LichTrinh> filteredItems = new ArrayList<>();
        if (dayNumber <= distinctDates.size()) {
            String targetDate = distinctDates.get(dayNumber - 1);
            for (LichTrinh lt : allItems) {
                if (targetDate.equals(lt.getNgayHoatDong())) {
                    filteredItems.add(lt);
                }
            }
        } else {
            filteredItems.addAll(allItems);
        }

        adapter.setDanhSachLichTrinh(filteredItems);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

