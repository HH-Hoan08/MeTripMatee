package com.example.mytripmatee.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytripmatee.R;
import com.example.mytripmatee.activity.CaiDatActivity;
import com.example.mytripmatee.activity.ChiTietChuyenDiActivity;
import com.example.mytripmatee.adapter.AdapterChuyenDi;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.database.NguoiDungDAO;
import com.example.mytripmatee.databinding.FragmentKhamPhaBinding;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.model.ItemChuyenDi;
import com.example.mytripmatee.model.NguoiDung;
import com.example.mytripmatee.util.QuanLyPhien;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class KhamPhaFragment extends Fragment {

    private FragmentKhamPhaBinding binding;
    private AdapterChuyenDi adapterChuyenDi;
    private ChuyenDiDAO chuyenDiDAO;
    private QuanLyPhien quanLyPhien;

    private List<ChuyenDi> fullChuyenDiList = new ArrayList<>();
    private String currentFilter = "ALL"; // ALL, UPCOMING, DRAFT, DONE

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentKhamPhaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chuyenDiDAO = new ChuyenDiDAO(requireContext());
        quanLyPhien = new QuanLyPhien(requireContext());

        setupUserHeader();
        setupRecyclerView();
        setupFeaturedCard();
        setupSettingsButton();
        setupSearchAndFilters();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tự động làm mới thông tin người dùng và danh sách chuyến đi từ SQLite ngay khi quay lại màn hình chính
        setupUserHeader();
        loadTripsFromDatabase();
    }

    private void setupUserHeader() {
        if (binding == null) return;

        // 1. Cập nhật lời chào thời gian thực theo giờ trong ngày (Sáng / Chiều / Tối)
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Chào buổi sáng ✨";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Chào buổi chiều ☀️";
        } else {
            greeting = "Chào buổi tối 🌙";
        }
        if (binding.tvGreeting != null) {
            binding.tvGreeting.setText(greeting);
        }

        // 2. Lấy Họ Tên / Tên Đăng Nhập của tài khoản đang đăng nhập
        String displayName = null;
        if (quanLyPhien != null) {
            displayName = quanLyPhien.getHoTen();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = quanLyPhien.getTenDangNhap();
            }
        }

        if ((displayName == null || displayName.trim().isEmpty()) && getContext() != null) {
            int userId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
            if (userId > 0) {
                NguoiDungDAO nguoiDungDAO = new NguoiDungDAO(requireContext());
                NguoiDung user = nguoiDungDAO.layTheoId(userId);
                if (user != null) {
                    if (user.getHoTen() != null && !user.getHoTen().trim().isEmpty()) {
                        displayName = user.getHoTen().trim();
                    } else if (user.getTenDangNhap() != null && !user.getTenDangNhap().trim().isEmpty()) {
                        displayName = user.getTenDangNhap().trim();
                    }
                }
            }
        }

        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = "Người dùng MeTripMatee";
        }

        if (binding.tvUserName != null) {
            binding.tvUserName.setText(displayName);
        }
    }

    private void setupRecyclerView() {
        adapterChuyenDi = new AdapterChuyenDi(itemChuyenDi -> {
            if (getContext() != null) {
                int tripId = 1;
                try {
                    tripId = Integer.parseInt(itemChuyenDi.getMaId());
                } catch (Exception ignored) {}
                Intent intent = ChiTietChuyenDiActivity.newIntent(
                        getContext(),
                        tripId,
                        itemChuyenDi.getTieuDe(),
                        itemChuyenDi.getTheNgay() != null ? itemChuyenDi.getTheNgay() : "Chi tiết chuyến đi"
                );
                startActivity(intent);
            }
        });

        binding.rvTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTrips.setAdapter(adapterChuyenDi);
    }

    private void setupFeaturedCard() {
        binding.cardFeatured.setOnClickListener(v -> {
            if (getContext() != null) {
                if (!fullChuyenDiList.isEmpty()) {
                    ChuyenDi featured = fullChuyenDiList.get(0);
                    Intent intent = ChiTietChuyenDiActivity.newIntent(
                            getContext(),
                            featured.getMaChuyenDi(),
                            featured.getTenChuyenDi(),
                            (featured.getNgayDi() != null ? featured.getNgayDi() : "") + " – " + (featured.getNgayVe() != null ? featured.getNgayVe() : "")
                    );
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(), "Chưa có chuyến đi nào. Hãy bấm '+' để tạo chuyến đi mới!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSettingsButton() {
        binding.btnSettings.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), CaiDatActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupSearchAndFilters() {
        if (binding.etSearch != null) {
            binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    return true;
                }
                return false;
            });
            binding.etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    if (binding.btnClearSearch != null) {
                        binding.btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    filterTrips(query);
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (binding.btnClearSearch != null) {
            binding.btnClearSearch.setOnClickListener(v -> {
                if (binding.etSearch != null) {
                    binding.etSearch.setText("");
                }
                hideKeyboard();
            });
        }

        if (binding.btnTune != null) {
            binding.btnTune.setOnClickListener(v -> {
                switch (currentFilter) {
                    case "ALL":
                        currentFilter = "UPCOMING";
                        break;
                    case "UPCOMING":
                        currentFilter = "ONGOING";
                        break;
                    case "ONGOING":
                    case "DRAFT":
                        currentFilter = "DONE";
                        break;
                    case "DONE":
                    default:
                        currentFilter = "ALL";
                        break;
                }
                updateTabUI();
                filterTrips(binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "");
            });
        }

        if (binding.tabAll != null) {
            binding.tabAll.setOnClickListener(v -> {
                currentFilter = "ALL";
                updateTabUI();
                filterTrips(binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "");
            });
        }
        if (binding.tabUpcoming != null) {
            binding.tabUpcoming.setOnClickListener(v -> {
                currentFilter = "UPCOMING";
                updateTabUI();
                filterTrips(binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "");
            });
        }
        if (binding.tabDraft != null) {
            binding.tabDraft.setOnClickListener(v -> {
                currentFilter = "ONGOING";
                updateTabUI();
                filterTrips(binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "");
            });
        }
        if (binding.tabDone != null) {
            binding.tabDone.setOnClickListener(v -> {
                currentFilter = "DONE";
                updateTabUI();
                filterTrips(binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "");
            });
        }
    }

    private void updateTabUI() {
        if (binding == null || getContext() == null) return;

        int activeBg = R.drawable.bg_pill_teal;
        int activeTextColor = ContextCompat.getColor(requireContext(), android.R.color.white);
        int inactiveBg = R.drawable.bg_pill_white;
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        if (binding.tabAll != null) {
            binding.tabAll.setBackgroundResource("ALL".equals(currentFilter) ? activeBg : inactiveBg);
            binding.tabAll.setTextColor("ALL".equals(currentFilter) ? activeTextColor : inactiveTextColor);
        }
        if (binding.tabUpcoming != null) {
            binding.tabUpcoming.setBackgroundResource("UPCOMING".equals(currentFilter) ? activeBg : inactiveBg);
            binding.tabUpcoming.setTextColor("UPCOMING".equals(currentFilter) ? activeTextColor : inactiveTextColor);
        }
        if (binding.tabDraft != null) {
            boolean isOngoing = "ONGOING".equals(currentFilter) || "DRAFT".equals(currentFilter);
            binding.tabDraft.setBackgroundResource(isOngoing ? activeBg : inactiveBg);
            binding.tabDraft.setTextColor(isOngoing ? activeTextColor : inactiveTextColor);
        }
        if (binding.tabDone != null) {
            binding.tabDone.setBackgroundResource("DONE".equals(currentFilter) ? activeBg : inactiveBg);
            binding.tabDone.setTextColor("DONE".equals(currentFilter) ? activeTextColor : inactiveTextColor);
        }
    }

    private void hideKeyboard() {
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    private void loadTripsFromDatabase() {
        int userId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
        if (userId <= 0) userId = 1;

        fullChuyenDiList = chuyenDiDAO.layDanhSachChuyenDi(userId);

        filterTrips(binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "");

        // Cập nhật thẻ nổi bật (Featured Card)
        if (!fullChuyenDiList.isEmpty()) {
            ChuyenDi featured = fullChuyenDiList.get(0);
            binding.cardFeatured.setVisibility(View.VISIBLE);
            binding.tvFeaturedTitle.setText(featured.getTenChuyenDi());
            if (binding.tvFeaturedSub != null) {
                String sub = (featured.getDiemDen() != null ? featured.getDiemDen() : "") +
                        " • " + (featured.getNgayDi() != null ? featured.getNgayDi() : "");
                binding.tvFeaturedSub.setText(sub);
            }
        } else {
            binding.cardFeatured.setVisibility(View.GONE);
        }
    }

    private void filterTrips(String keyword) {
        int userId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
        if (userId <= 0) userId = 1;

        List<ChuyenDi> searchResult;
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchResult = chuyenDiDAO.timKiemChuyenDi(userId, keyword.trim());
        } else {
            searchResult = fullChuyenDiList;
        }

        List<ItemChuyenDi> filteredItems = new ArrayList<>();
        for (ChuyenDi cd : searchResult) {
            boolean matchesStatus = true;
            String status = cd.getTrangThaiThucTe();

            if ("UPCOMING".equals(currentFilter)) {
                matchesStatus = "SAP_TOI".equalsIgnoreCase(status);
            } else if ("ONGOING".equals(currentFilter) || "DRAFT".equals(currentFilter)) {
                matchesStatus = "DANG_DI".equalsIgnoreCase(status);
            } else if ("DONE".equals(currentFilter)) {
                matchesStatus = "DA_XONG".equalsIgnoreCase(status);
            }

            if (matchesStatus) {
                filteredItems.add(cd.toTripItem());
            }
        }

        if (binding != null) {
            // Ẩn/hiện thẻ nổi bật tùy theo đang tìm kiếm hay không
            if (keyword != null && !keyword.trim().isEmpty()) {
                binding.cardFeatured.setVisibility(View.GONE);
            } else {
                binding.cardFeatured.setVisibility(fullChuyenDiList.isEmpty() ? View.GONE : View.VISIBLE);
            }

            // Nếu danh sách tìm kiếm rỗng -> hiển thị tvEmptySearch
            if (binding.tvEmptySearch != null) {
                if (filteredItems.isEmpty()) {
                    binding.tvEmptySearch.setVisibility(View.VISIBLE);
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        binding.tvEmptySearch.setText("Không tìm thấy chuyến đi nào phù hợp với \"" + keyword + "\"");
                    } else {
                        binding.tvEmptySearch.setText("Không có chuyến đi nào trong danh mục này");
                    }
                    binding.rvTrips.setVisibility(View.GONE);
                } else {
                    binding.tvEmptySearch.setVisibility(View.GONE);
                    binding.rvTrips.setVisibility(View.VISIBLE);
                }
            }
        }

        if (adapterChuyenDi != null) {
            adapterChuyenDi.setDanhSachChuyenDi(filteredItems);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
