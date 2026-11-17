package com.example.mytripmatee.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytripmatee.R;
import com.example.mytripmatee.activity.SuaLichTrinhActivity;
import com.example.mytripmatee.adapter.AdapterLichTrinh;
import com.example.mytripmatee.dao.LichTrinhDAO;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.databinding.FragmentLichTrinhBinding;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.model.LichTrinh;
import com.example.mytripmatee.util.QuanLyPhien;
import com.example.mytripmatee.util.TienIchNgay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LichTrinhFragment extends Fragment {

    public static LichTrinhFragment newInstance(int maChuyenDi) {
        LichTrinhFragment fragment = new LichTrinhFragment();
        Bundle args = new Bundle();
        args.putInt("maChuyenDi", maChuyenDi);
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentLichTrinhBinding binding;
    private AdapterLichTrinh adapter;
    private LichTrinhDAO lichTrinhDAO;
    private ChuyenDiDAO chuyenDiDAO;
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
        chuyenDiDAO = new ChuyenDiDAO(requireContext());

        if (getArguments() != null && getArguments().containsKey("maChuyenDi")) {
            maChuyenDi = getArguments().getInt("maChuyenDi", 1);
        } else if (requireActivity().getIntent() != null) {
            maChuyenDi = requireActivity().getIntent().getIntExtra("extra_ma_chuyen_di",
                    requireActivity().getIntent().getIntExtra("maChuyenDi", 1));
        }

        setupRecyclerView();

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
        if (getArguments() != null && getArguments().containsKey("maChuyenDi")) {
            maChuyenDi = getArguments().getInt("maChuyenDi", 1);
            loadDayData(selectedDay);
        } else {
            kiemTraChuyenDiHomNay();
        }
    }

    /**
     * Tự động kiểm tra theo thời gian thực xem hôm nay có chuyến đi nào không.
     * Nếu có: tự động chuyển sang chuyến đi và ngày hiện tại.
     * Nếu không: hiển thị thông báo thời gian thực cho người dùng.
     */
    public void kiemTraChuyenDiHomNay() {
        if (getContext() == null) return;

        QuanLyPhien quanLyPhien = new QuanLyPhien(requireContext());
        int maNguoiDung = quanLyPhien.getMaNguoiDung();
        if (maNguoiDung <= 0) maNguoiDung = 1;

        SimpleDateFormat sdfYmd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfDmy = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date now = new Date();
        String todayDbStr = sdfYmd.format(now);
        String todayDisplayStr = sdfDmy.format(now);

        ChuyenDi chuyenDiHomNay = chuyenDiDAO.layChuyenDiHomNay(maNguoiDung, todayDbStr);

        if (chuyenDiHomNay != null) {
            maChuyenDi = chuyenDiHomNay.getMaChuyenDi();
            int dayNumber = tinhNgayThuMay(chuyenDiHomNay.getNgayDi(), todayDbStr);
            selectedDay = Math.max(1, dayNumber);

            Toast.makeText(requireContext(),
                    "Hôm nay (" + todayDisplayStr + ") đang diễn ra: " + chuyenDiHomNay.getTenChuyenDi(),
                    Toast.LENGTH_SHORT).show();
        } else {
            maChuyenDi = -1; // Không có chuyến đi hôm nay
            Toast.makeText(requireContext(),
                    "Hôm nay (" + todayDisplayStr + ") bạn không có chuyến đi nào!",
                    Toast.LENGTH_LONG).show();
        }

        loadDayData(selectedDay);
    }

    private int tinhNgayThuMay(String ngayDiStr, String ngayHomNayStr) {
        if (ngayDiStr == null || ngayHomNayStr == null) return 1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d1 = sdf.parse(ngayDiStr);
            Date d2 = sdf.parse(ngayHomNayStr);
            if (d1 != null && d2 != null) {
                long diff = d2.getTime() - d1.getTime();
                long days = diff / (1000 * 60 * 60 * 24);
                return (int) days + 1;
            }
        } catch (Exception ignored) {}
        return 1;
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

            @Override
            public void onXoaMuc(LichTrinh item) {
                if (item == null) return;
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Xóa hoạt động")
                        .setMessage("Bạn có chắc chắn muốn xóa hoạt động '" + item.getTieuDe() + "' khỏi lịch trình?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            boolean success = lichTrinhDAO.xoaHoatDong(item.getMaHoatDong());
                            if (success) {
                                Toast.makeText(requireContext(), "Đã xóa hoạt động ✓", Toast.LENGTH_SHORT).show();
                                loadDayData(selectedDay);
                            } else {
                                Toast.makeText(requireContext(), "Lỗi khi xóa hoạt động", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        binding.rvItinerary.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvItinerary.setAdapter(adapter);
    }

    private void selectDay(int dayNumber) {
        selectedDay = dayNumber;
        loadDayData(dayNumber);
    }

    private void loadDayData(int dayNumber) {
        if (maChuyenDi <= 0) {
            adapter.setDanhSachLichTrinh(new ArrayList<>());
            renderDayPills(new ArrayList<>(), 1);
            if (binding != null && binding.tvEmptyState != null) {
                SimpleDateFormat sdfDmy = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String todayDisplayStr = sdfDmy.format(new Date());
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.tvEmptyState.setText("Hôm nay (" + todayDisplayStr + ") bạn không có chuyến đi nào đang diễn ra!\nHãy bấm '+ Thêm hoạt động' hoặc tạo chuyến đi mới.");
            }
            return;
        }

        List<LichTrinh> allItems = lichTrinhDAO.layDanhSachHoatDongTheoChuyenDi(maChuyenDi);
        ChuyenDi chuyenDi = chuyenDiDAO.layChuyenDiTheoId(maChuyenDi);

        if (chuyenDi != null && chuyenDi.getDiemDen() != null && !chuyenDi.getDiemDen().isEmpty() && binding != null && binding.tvWeatherLocation != null) {
            binding.tvWeatherLocation.setText(chuyenDi.getDiemDen() + " • 19°C Trời se lạnh");
        }

        List<String> tripDates = calculateTripDates(chuyenDi, allItems);

        renderDayPills(tripDates, dayNumber);

        if (allItems == null || allItems.isEmpty()) {
            adapter.setDanhSachLichTrinh(new ArrayList<>());
            if (binding != null && binding.tvEmptyState != null) {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                String tripTitle = (chuyenDi != null && chuyenDi.getTenChuyenDi() != null) ? chuyenDi.getTenChuyenDi() : "chuyến đi này";
                binding.tvEmptyState.setText("Chưa có hoạt động nào cho " + tripTitle + ".\nBấm '+ Thêm hoạt động' để bổ sung!");
            }
            return;
        }

        List<LichTrinh> filteredItems = new ArrayList<>();
        if (dayNumber <= tripDates.size()) {
            String targetDate = tripDates.get(dayNumber - 1);
            for (LichTrinh lt : allItems) {
                if (targetDate.equals(lt.getNgayHoatDong())) {
                    filteredItems.add(lt);
                }
            }
        } else {
            filteredItems.addAll(allItems);
        }

        adapter.setDanhSachLichTrinh(filteredItems);

        if (binding != null && binding.tvEmptyState != null) {
            if (filteredItems.isEmpty()) {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.tvEmptyState.setText("Chưa có hoạt động nào cho Ngày " + dayNumber + ".\nBấm '+ Thêm hoạt động' để bổ sung!");
            } else {
                binding.tvEmptyState.setVisibility(View.GONE);
            }
        }
    }

    private List<String> calculateTripDates(ChuyenDi cd, List<LichTrinh> activities) {
        List<String> dates = new ArrayList<>();
        if (cd != null && cd.getNgayDi() != null && cd.getNgayVe() != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                Date startDate = sdf.parse(cd.getNgayDi());
                Date endDate = sdf.parse(cd.getNgayVe());
                if (startDate != null && endDate != null) {
                    cal.setTime(startDate);
                    while (!cal.getTime().after(endDate)) {
                        dates.add(sdf.format(cal.getTime()));
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                    }
                }
            } catch (Exception ignored) {}
        }

        if (activities != null) {
            for (LichTrinh lt : activities) {
                if (lt.getNgayHoatDong() != null && !dates.contains(lt.getNgayHoatDong())) {
                    dates.add(lt.getNgayHoatDong());
                }
            }
        }

        return dates;
    }

    private void renderDayPills(List<String> dates, int currentSelectedDay) {
        if (binding == null) return;

        if (dates == null || dates.isEmpty()) {
            if (binding.layoutDayPillsContainer != null) {
                binding.layoutDayPillsContainer.removeAllViews();
            }
            if (binding.layoutDayPillsScroll != null) {
                binding.layoutDayPillsScroll.setVisibility(View.GONE);
            }
            return;
        }

        if (binding.layoutDayPillsScroll != null) {
            binding.layoutDayPillsScroll.setVisibility(View.VISIBLE);
        }
        if (binding.layoutDayPillsContainer == null) return;
        binding.layoutDayPillsContainer.removeAllViews();

        float density = getResources().getDisplayMetrics().density;
        int pillWidth = (int) (90 * density);
        int pillHeight = (int) (54 * density);
        int pillMarginEnd = (int) (10 * density);

        for (int i = 0; i < dates.size(); i++) {
            final int dayNum = i + 1;
            String dateStr = dates.get(i);
            boolean isSelected = (dayNum == currentSelectedDay);

            LinearLayout pillLayout = new LinearLayout(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(pillWidth, pillHeight);
            if (i < dates.size() - 1) {
                lp.setMarginEnd(pillMarginEnd);
            }
            pillLayout.setLayoutParams(lp);
            pillLayout.setOrientation(LinearLayout.VERTICAL);
            pillLayout.setGravity(Gravity.CENTER);
            pillLayout.setBackgroundResource(isSelected ? R.drawable.bg_date_pill_selected : R.drawable.bg_date_pill_unselected);

            TextView tvDayLabel = new TextView(requireContext());
            tvDayLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tvDayLabel.setGravity(Gravity.CENTER);
            tvDayLabel.setText("NGÀY " + dayNum);
            tvDayLabel.setTextSize(10);
            tvDayLabel.setTypeface(null, Typeface.BOLD);
            tvDayLabel.setTextColor(Color.parseColor(isSelected ? "#CCFBF1" : "#94A3B8"));

            TextView tvDateVal = new TextView(requireContext());
            tvDateVal.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tvDateVal.setGravity(Gravity.CENTER);
            tvDateVal.setText(TienIchNgay.dinhDangNgayPill(dateStr));
            tvDateVal.setTextSize(14);
            tvDateVal.setTypeface(null, Typeface.BOLD);
            tvDateVal.setTextColor(Color.parseColor(isSelected ? "#FFFFFF" : "#1E293B"));

            pillLayout.addView(tvDayLabel);
            pillLayout.addView(tvDateVal);

            pillLayout.setOnClickListener(v -> selectDay(dayNum));

            binding.layoutDayPillsContainer.addView(pillLayout);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
