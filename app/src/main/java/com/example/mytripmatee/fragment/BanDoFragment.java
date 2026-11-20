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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;
import com.example.mytripmatee.activity.ChiTietChuyenDiActivity;
import com.example.mytripmatee.dao.LichTrinhDAO;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.databinding.FragmentBanDoBinding;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.model.LichTrinh;
import com.example.mytripmatee.util.QuanLyPhien;
import com.example.mytripmatee.util.TienIchBanDo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BanDoFragment extends Fragment {

    private FragmentBanDoBinding binding;
    private ChuyenDiDAO chuyenDiDAO;
    private LichTrinhDAO lichTrinhDAO;
    private QuanLyPhien quanLyPhien;
    private MapLocationAdapter adapter;

    private List<ChuyenDi> danhSachChuyenDiAll = new ArrayList<>();
    private ChuyenDi selectedTrip = null;

    // Class hiển thị từng địa điểm lịch trình kèm tên chuyến đi tương ứng
    public static class ItemMapLocation {
        private final LichTrinh lichTrinh;
        private final String tenChuyenDi;
        private final int maChuyenDi;

        public ItemMapLocation(LichTrinh lichTrinh, String tenChuyenDi, int maChuyenDi) {
            this.lichTrinh = lichTrinh;
            this.tenChuyenDi = tenChuyenDi;
            this.maChuyenDi = maChuyenDi;
        }

        public LichTrinh getLichTrinh() { return lichTrinh; }
        public String getTenChuyenDi() { return tenChuyenDi; }
        public int getMaChuyenDi() { return maChuyenDi; }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBanDoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chuyenDiDAO = new ChuyenDiDAO(requireContext());
        lichTrinhDAO = new LichTrinhDAO(requireContext());
        quanLyPhien = new QuanLyPhien(requireContext());

        setupRecyclerView();
        setupSearchAutoComplete();
        loadMapData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMapData();
    }

    private void setupRecyclerView() {
        adapter = new MapLocationAdapter(new MapLocationAdapter.OnLocationClickListener() {
            @Override
            public void onOpenMap(ItemMapLocation item) {
                if (item != null && item.getLichTrinh() != null) {
                    LichTrinh lt = item.getLichTrinh();
                    TienIchBanDo.moGoogleMaps(
                            requireContext(),
                            lt.getLinkBanDo(),
                            lt.getDiaDiem(),
                            lt.getTieuDe()
                    );
                }
            }

            @Override
            public void onItemClick(ItemMapLocation item) {
                if (item != null && getContext() != null) {
                    int tripId = item.getMaChuyenDi();
                    ChuyenDi cd = chuyenDiDAO.layChuyenDiTheoId(tripId);
                    String tripTitle = cd != null ? cd.getTenChuyenDi() : item.getTenChuyenDi();
                    String dateTag = cd != null ? ((cd.getNgayDi() != null ? cd.getNgayDi() : "") + " – " + (cd.getNgayVe() != null ? cd.getNgayVe() : "")) : "Chi tiết chuyến đi";

                    Intent intent = ChiTietChuyenDiActivity.newIntent(getContext(), tripId, tripTitle, dateTag);
                    startActivity(intent);
                }
            }
        });

        binding.rvMapLocations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMapLocations.setAdapter(adapter);
    }

    private void setupSearchAutoComplete() {
        if (binding.etSearchTripMap != null) {
            binding.etSearchTripMap.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    return true;
                }
                return false;
            });

            binding.etSearchTripMap.setOnItemClickListener((parent, view, position, id) -> {
                String selectedSuggestion = (String) parent.getItemAtPosition(position);
                filterMapDataBySearch(selectedSuggestion);
                hideKeyboard();
            });

            binding.etSearchTripMap.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    if (binding.btnClearSearchMap != null) {
                        binding.btnClearSearchMap.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    filterMapDataBySearch(query);
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (binding.btnClearSearchMap != null) {
            binding.btnClearSearchMap.setOnClickListener(v -> {
                if (binding.etSearchTripMap != null) {
                    binding.etSearchTripMap.setText("");
                }
                hideKeyboard();
            });
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

    private void loadMapData() {
        if (binding == null) return;

        int userId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
        if (userId <= 0) userId = 1;

        danhSachChuyenDiAll = chuyenDiDAO.layDanhSachChuyenDi(userId);

        if (danhSachChuyenDiAll == null || danhSachChuyenDiAll.isEmpty()) {
            binding.tvDestinationTitle.setText("Chưa có chuyến đi nào");
            binding.tvDestinationSub.setText("Tạo chuyến đi mới để bắt đầu xem vị trí bản đồ");
            binding.btnOpenMainMap.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Chưa có chuyến đi nào để xem chi tiết", Toast.LENGTH_SHORT).show();
            });
            adapter.setLocations(new ArrayList<>());
            binding.tvEmptyMapLocations.setVisibility(View.VISIBLE);
            return;
        }

        // 1. Tự động kiểm tra xem HÔM NAY có chuyến đi nào đang diễn ra không
        SimpleDateFormat sdfYmd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDbStr = sdfYmd.format(new Date());

        ChuyenDi chuyenDiHomNay = chuyenDiDAO.layChuyenDiHomNay(userId, todayDbStr);

        if (chuyenDiHomNay != null) {
            selectedTrip = chuyenDiHomNay;
            if (binding.tvTripBadgeTag != null) {
                binding.tvTripBadgeTag.setText("CHUYẾN ĐI HÔM NAY");
            }
        } else {
            selectedTrip = danhSachChuyenDiAll.get(0);
            if (binding.tvTripBadgeTag != null) {
                binding.tvTripBadgeTag.setText("CHUYẾN ĐI NỔI BẬT");
            }
        }

        // 2. Tạo danh sách gợi ý tự động cho thanh tìm kiếm địa điểm lịch trình
        buildItinerarySearchSuggestions();

        // 3. Hiển thị thông tin chuyến đi & toàn bộ địa điểm lịch trình
        if (binding.etSearchTripMap == null || binding.etSearchTripMap.getText().toString().trim().isEmpty()) {
            displayTripMapDetails(selectedTrip);
        } else {
            filterMapDataBySearch(binding.etSearchTripMap.getText().toString().trim());
        }
    }

    private void buildItinerarySearchSuggestions() {
        if (binding == null || binding.etSearchTripMap == null) return;

        List<String> suggestions = new ArrayList<>();
        for (ChuyenDi cd : danhSachChuyenDiAll) {
            List<LichTrinh> activities = lichTrinhDAO.layDanhSachHoatDongTheoChuyenDi(cd.getMaChuyenDi());
            if (activities != null) {
                for (LichTrinh lt : activities) {
                    if (lt.getTieuDe() != null && !lt.getTieuDe().trim().isEmpty() && !suggestions.contains(lt.getTieuDe().trim())) {
                        suggestions.add(lt.getTieuDe().trim());
                    }
                    if (lt.getDiaDiem() != null && !lt.getDiaDiem().trim().isEmpty() && !suggestions.contains(lt.getDiaDiem().trim())) {
                        suggestions.add(lt.getDiaDiem().trim());
                    }
                }
            }
        }

        ArrayAdapter<String> suggestionAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                suggestions
        );
        binding.etSearchTripMap.setAdapter(suggestionAdapter);
    }

    private void displayTripMapDetails(ChuyenDi cd) {
        if (binding == null || cd == null) return;

        String destStr = (cd.getDiemDen() != null && !cd.getDiemDen().isEmpty())
                ? cd.getDiemDen()
                : cd.getTenChuyenDi();

        binding.tvDestinationTitle.setText(cd.getTenChuyenDi() + " • " + destStr);
        binding.tvDestinationSub.setText("Chạm để mở trang chi tiết chuyến đi (" + cd.getNgayDi() + " → " + cd.getNgayVe() + ") ›");

        // Khi bấm vào thẻ hoặc nút: mở trang Chi tiết chuyến đi (ChiTietChuyenDiActivity)
        View.OnClickListener openTripDetailListener = v -> {
            if (getContext() != null) {
                Intent intent = ChiTietChuyenDiActivity.newIntent(
                        getContext(),
                        cd.getMaChuyenDi(),
                        cd.getTenChuyenDi(),
                        (cd.getNgayDi() != null ? cd.getNgayDi() : "") + " – " + (cd.getNgayVe() != null ? cd.getNgayVe() : "")
                );
                startActivity(intent);
            }
        };

        binding.cardMainDestination.setOnClickListener(openTripDetailListener);
        binding.btnOpenMainMap.setOnClickListener(openTripDetailListener);

        // Lấy danh sách TOÀN BỘ địa điểm lịch trình của các chuyến đi
        List<ItemMapLocation> mapLocations = new ArrayList<>();
        if (danhSachChuyenDiAll != null) {
            for (ChuyenDi trip : danhSachChuyenDiAll) {
                List<LichTrinh> activities = lichTrinhDAO.layDanhSachHoatDongTheoChuyenDi(trip.getMaChuyenDi());
                if (activities != null) {
                    for (LichTrinh lt : activities) {
                        if (lt.isCoBanDo()) {
                            mapLocations.add(new ItemMapLocation(lt, trip.getTenChuyenDi(), trip.getMaChuyenDi()));
                        }
                    }
                }
            }
        }

        if (mapLocations.isEmpty()) {
            binding.tvEmptyMapLocations.setVisibility(View.VISIBLE);
            binding.tvEmptyMapLocations.setText("Chưa có địa điểm hoặc liên kết bản đồ nào trong các lịch trình");
        } else {
            binding.tvEmptyMapLocations.setVisibility(View.GONE);
        }

        if (binding.tvLocationsHeader != null) {
            binding.tvLocationsHeader.setText("Tất cả " + mapLocations.size() + " địa điểm lịch trình (" + danhSachChuyenDiAll.size() + " chuyến đi)");
        }

        adapter.setLocations(mapLocations);
    }

    private void filterMapDataBySearch(String query) {
        if (binding == null) return;

        if (query == null || query.trim().isEmpty()) {
            if (selectedTrip != null) {
                displayTripMapDetails(selectedTrip);
            } else {
                loadMapData();
            }
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        List<ItemMapLocation> filteredLocations = new ArrayList<>();
        ChuyenDi topMatchTrip = null;

        // Chỉ tìm kiếm trong các địa điểm/hoạt động thuộc Lịch trình
        if (danhSachChuyenDiAll != null) {
            for (ChuyenDi cd : danhSachChuyenDiAll) {
                List<LichTrinh> listLt = lichTrinhDAO.layDanhSachHoatDongTheoChuyenDi(cd.getMaChuyenDi());
                if (listLt != null) {
                    for (LichTrinh lt : listLt) {
                        if (lt.isCoBanDo()) {
                            boolean matchTitle = lt.getTieuDe() != null && lt.getTieuDe().toLowerCase().contains(lowerQuery);
                            boolean matchLocation = lt.getDiaDiem() != null && lt.getDiaDiem().toLowerCase().contains(lowerQuery);
                            boolean matchNotes = lt.getGhiChu() != null && lt.getGhiChu().toLowerCase().contains(lowerQuery);

                            if (matchTitle || matchLocation || matchNotes) {
                                filteredLocations.add(new ItemMapLocation(lt, cd.getTenChuyenDi(), cd.getMaChuyenDi()));
                                if (topMatchTrip == null) {
                                    topMatchTrip = cd;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cập nhật thẻ thông tin chuyến đi phù hợp với kết quả tìm kiếm top
        if (topMatchTrip != null) {
            displayTripMapDetailsHeaderOnly(topMatchTrip);
        }

        if (filteredLocations.isEmpty()) {
            binding.tvEmptyMapLocations.setVisibility(View.VISIBLE);
            binding.tvEmptyMapLocations.setText("Không tìm thấy địa điểm lịch trình nào khớp với từ khóa \"" + query + "\"");
        } else {
            binding.tvEmptyMapLocations.setVisibility(View.GONE);
        }

        if (binding.tvLocationsHeader != null) {
            binding.tvLocationsHeader.setText("Kết quả tìm kiếm lịch trình (" + filteredLocations.size() + " địa điểm)");
        }

        adapter.setLocations(filteredLocations);
    }

    private void displayTripMapDetailsHeaderOnly(ChuyenDi cd) {
        if (binding == null || cd == null) return;

        String destStr = (cd.getDiemDen() != null && !cd.getDiemDen().isEmpty())
                ? cd.getDiemDen()
                : cd.getTenChuyenDi();

        binding.tvDestinationTitle.setText(cd.getTenChuyenDi() + " • " + destStr);
        binding.tvDestinationSub.setText("Chạm để mở trang chi tiết chuyến đi (" + cd.getNgayDi() + " → " + cd.getNgayVe() + ") ›");

        View.OnClickListener openTripDetailListener = v -> {
            if (getContext() != null) {
                Intent intent = ChiTietChuyenDiActivity.newIntent(
                        getContext(),
                        cd.getMaChuyenDi(),
                        cd.getTenChuyenDi(),
                        (cd.getNgayDi() != null ? cd.getNgayDi() : "") + " – " + (cd.getNgayVe() != null ? cd.getNgayVe() : "")
                );
                startActivity(intent);
            }
        };

        binding.cardMainDestination.setOnClickListener(openTripDetailListener);
        binding.btnOpenMainMap.setOnClickListener(openTripDetailListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Adapter hiển thị danh sách địa điểm lịch trình kèm tên chuyến đi tương ứng
    private static class MapLocationAdapter extends RecyclerView.Adapter<MapLocationAdapter.ViewHolder> {

        interface OnLocationClickListener {
            void onOpenMap(ItemMapLocation item);
            void onItemClick(ItemMapLocation item);
        }

        private final List<ItemMapLocation> list = new ArrayList<>();
        private final OnLocationClickListener listener;

        public MapLocationAdapter(OnLocationClickListener listener) {
            this.listener = listener;
        }

        public void setLocations(List<ItemMapLocation> newList) {
            this.list.clear();
            if (newList != null) {
                this.list.addAll(newList);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dia_diem_ban_do, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (position >= 0 && position < list.size()) {
                holder.bind(list.get(position), listener);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTripName, tvTitle, tvLocation, btnOpenMap;
            ImageView imgIcon;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTripName = itemView.findViewById(R.id.tvTripName);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                btnOpenMap = itemView.findViewById(R.id.btnOpenMap);
                imgIcon = itemView.findViewById(R.id.imgIcon);
            }

            public void bind(ItemMapLocation item, OnLocationClickListener listener) {
                if (item == null || item.getLichTrinh() == null) return;

                LichTrinh lt = item.getLichTrinh();

                View.OnClickListener openMapListener = v -> {
                    if (listener != null) listener.onOpenMap(item);
                };

                View.OnClickListener itemClickListener = v -> {
                    if (listener != null) listener.onItemClick(item);
                };

                if (tvTripName != null) {
                    String tripNameText = item.getTenChuyenDi() != null ? "📌 " + item.getTenChuyenDi() : "📌 Chuyến đi";
                    tvTripName.setText(tripNameText);
                    tvTripName.setOnClickListener(itemClickListener);
                }

                tvTitle.setText(lt.getTieuDe() != null ? lt.getTieuDe() : "");

                String locText = (lt.getDiaDiem() != null && !lt.getDiaDiem().isEmpty())
                        ? lt.getDiaDiem()
                        : (lt.getLinkBanDo() != null ? lt.getLinkBanDo() : "Chưa có vị trí");

                if (lt.getNgayHoatDong() != null && !lt.getNgayHoatDong().isEmpty()) {
                    String timeStr = lt.getGioBatDau() != null ? " • " + lt.getGioBatDau() : "";
                    locText += "\n(" + lt.getNgayHoatDong() + timeStr + ")";
                }

                tvLocation.setText(locText);

                itemView.setOnClickListener(itemClickListener);
                if (btnOpenMap != null) btnOpenMap.setOnClickListener(openMapListener);
            }
        }
    }
}
