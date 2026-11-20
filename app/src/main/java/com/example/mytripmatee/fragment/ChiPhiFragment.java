package com.example.mytripmatee.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;
import com.example.mytripmatee.adapter.AdapterChiPhi;
import com.example.mytripmatee.adapter.AdapterChiaTien;
import com.example.mytripmatee.dao.ChiPhiDAO;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.database.NguoiDungDAO;
import com.example.mytripmatee.model.ChiPhi;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.model.ItemChiPhi;
import com.example.mytripmatee.model.ItemChiaTien;
import com.example.mytripmatee.model.NguoiDung;
import com.example.mytripmatee.util.QuanLyPhien;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChiPhiFragment extends Fragment {

    private AdapterChiPhi adapterChiPhi;
    private AdapterChiaTien adapterChiaTien;
    private ChuyenDiDAO chuyenDiDAO;
    private ChiPhiDAO chiPhiDAO;
    private NguoiDungDAO nguoiDungDAO;
    private QuanLyPhien quanLyPhien;

    private List<ChuyenDi> danhSachChuyenDi = new ArrayList<>();
    private final List<ItemChiPhi> tatCaChiPhi = new ArrayList<>();
    private final List<ItemChiPhi> chiPhiDaLoc = new ArrayList<>();

    private RecyclerView rvExpenses;
    private RecyclerView rvGroupSplit;
    private Spinner spFilterDate;
    private Spinner spFilterCategory;
    private TextView tvCatMoveAmount;
    private TextView tvCatFoodAmount;
    private TextView tvCatStayAmount;
    private TextView tvTotalBudget;
    private TextView tvSpentAmount;
    private TextView tvRemainingAmount;
    private ProgressBar pbBudgetSpending;
    private TextView tvBadgePercent;
    private View layoutWarningCard;
    private TextView tvWarningText;
    private TextView btnAddExpense;
    private View btnAutoSplit;
    private TextView tvSplitMemberCount;
    private TextView tvGroupSplitDetail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chi_phi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chuyenDiDAO = new ChuyenDiDAO(requireContext());
        chiPhiDAO = new ChiPhiDAO(requireContext());
        nguoiDungDAO = new NguoiDungDAO(requireContext());
        quanLyPhien = new QuanLyPhien(requireContext());

        rvExpenses = view.findViewById(R.id.rvExpenses);
        rvGroupSplit = view.findViewById(R.id.rvGroupSplit);
        spFilterDate = view.findViewById(R.id.spFilterDate);
        spFilterCategory = view.findViewById(R.id.spFilterCategory);
        tvCatMoveAmount = view.findViewById(R.id.tvCatMoveAmount);
        tvCatFoodAmount = view.findViewById(R.id.tvCatFoodAmount);
        tvCatStayAmount = view.findViewById(R.id.tvCatStayAmount);
        tvTotalBudget = view.findViewById(R.id.tvTotalBudget);
        tvSpentAmount = view.findViewById(R.id.tvSpentAmount);
        tvRemainingAmount = view.findViewById(R.id.tvRemainingAmount);
        pbBudgetSpending = view.findViewById(R.id.pbBudgetSpending);
        tvBadgePercent = view.findViewById(R.id.tvBadgePercent);
        layoutWarningCard = view.findViewById(R.id.layoutWarningCard);
        tvWarningText = view.findViewById(R.id.tvWarningText);
        btnAddExpense = view.findViewById(R.id.btnAddExpense);
        btnAutoSplit = view.findViewById(R.id.btnAutoSplit);
        tvSplitMemberCount = view.findViewById(R.id.tvSplitMemberCount);
        tvGroupSplitDetail = view.findViewById(R.id.tvGroupSplitDetail);

        setupRecyclerView();
        loadDataFromDatabase();
        setupAddExpenseButton();
        setupActionButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDataFromDatabase();
    }

    private void setupRecyclerView() {
        adapterChiPhi = new AdapterChiPhi(this::hienThiMenuTuyChonChiPhi);
        if (rvExpenses != null) {
            rvExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvExpenses.setAdapter(adapterChiPhi);
        }

        if (rvGroupSplit != null) {
            adapterChiaTien = new AdapterChiaTien();
            rvGroupSplit.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvGroupSplit.setAdapter(adapterChiaTien);
        }
    }

    private boolean kiemTraQuyenChinhSua() {
        int currentUserId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
        int targetTripId = getTargetTripId();
        ChuyenDi currentTrip = chuyenDiDAO.layChuyenDiTheoId(targetTripId);

        if (currentTrip != null && currentTrip.getMaNguoiDung() != currentUserId) {
            Toast.makeText(requireContext(), "Bạn là thành viên được mời. Chỉ Trưởng nhóm (người tạo chuyến đi) mới có quyền chỉnh sửa/thêm/xóa chi phí!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void hienThiMenuTuyChonChiPhi(ItemChiPhi item) {
        if (item == null) return;

        if (!item.isCaNhan()) {
            if (!kiemTraQuyenChinhSua()) {
                return;
            }
        }

        String[] options = new String[]{"Chỉnh sửa khoản chi", "Xóa khoản chi"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.getTieuDe())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditExpenseDialog(item);
                    } else if (which == 1) {
                        xacNhanXoaChiPhi(item);
                    }
                })
                .show();
    }

    private void loadDataFromDatabase() {
        int userId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
        if (userId <= 0) userId = 1;

        danhSachChuyenDi = chuyenDiDAO.layDanhSachChuyenDi(userId);
        tatCaChiPhi.clear();

        if (danhSachChuyenDi != null && !danhSachChuyenDi.isEmpty()) {
            for (ChuyenDi cd : danhSachChuyenDi) {
                List<ChiPhi> listCp = chiPhiDAO.layDanhSachChiPhiTheoChuyenDi(cd.getMaChuyenDi(), userId);
                if (listCp != null) {
                    for (ChiPhi cp : listCp) {
                        ItemChiPhi.LoaiDanhMuc loai = mapCategoryToEnum(cp.getMaDanhMuc(), cp.getTenDanhMuc());
                        String ghiChuClean = cp.getGhiChu() != null ? cp.getGhiChu() : "";
                        if (ghiChuClean.contains(" (Thanh toán:")) {
                            ghiChuClean = ghiChuClean.replaceAll(" \\(Thanh toán:.*?\\)", "").trim();
                        }
                        String titleText = !ghiChuClean.isEmpty() ? ghiChuClean : (cp.getTenDanhMuc() != null ? cp.getTenDanhMuc() : "Khoản chi");
                        String categoryName = cp.getTenDanhMuc() != null ? cp.getTenDanhMuc() : "Chi phí";
                        String tag = cp.isCaNhan() ? "TÔI (Cá nhân)" : "Lịch trình nhóm";
                        String sub = tag + " • " + categoryName;

                        ItemChiPhi item = new ItemChiPhi(
                                String.valueOf(cp.getMaChiPhi()),
                                titleText,
                                sub,
                                cp.getSoTien(),
                                loai,
                                "Tôi",
                                cd.getTenChuyenDi(),
                                cp.isCaNhan()
                        );
                        tatCaChiPhi.add(item);
                    }
                }
            }
        }

        setupFilters();

        int targetTripId = getTargetTripId();
        SharedPreferences sp = requireContext().getSharedPreferences("group_split_pref", Context.MODE_PRIVATE);
        boolean hasCalculated = sp.getBoolean("has_calculated_split_" + targetTripId, false);

        if (hasCalculated) {
            thucHienChiaTienNhom();
        } else {
            datTrangThaiBanDauChiaTien();
        }
    }

    private void setupFilters() {
        List<String> tripOptions = new ArrayList<>();
        tripOptions.add("Tất cả chuyến đi");
        if (danhSachChuyenDi != null && !danhSachChuyenDi.isEmpty()) {
            for (ChuyenDi cd : danhSachChuyenDi) {
                tripOptions.add(cd.getTenChuyenDi());
            }
        }

        ArrayAdapter<String> tripAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner_chon, tripOptions);
        tripAdapter.setDropDownViewResource(R.layout.item_spinner_danh_sach);
        if (spFilterDate != null) spFilterDate.setAdapter(tripAdapter);

        String[] categoryOptions = new String[]{"Tất cả danh mục", "Di chuyển", "Ăn uống", "Khách sạn", "Khác"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner_chon, categoryOptions);
        catAdapter.setDropDownViewResource(R.layout.item_spinner_danh_sach);
        if (spFilterCategory != null) spFilterCategory.setAdapter(catAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        if (spFilterDate != null) spFilterDate.setOnItemSelectedListener(filterListener);
        if (spFilterCategory != null) spFilterCategory.setOnItemSelectedListener(filterListener);

        applyFilters();
    }

    private int getTargetTripId() {
        int tripPos = spFilterDate != null ? spFilterDate.getSelectedItemPosition() : 0;
        if (danhSachChuyenDi != null && !danhSachChuyenDi.isEmpty()) {
            if (tripPos > 0 && tripPos - 1 < danhSachChuyenDi.size()) {
                return danhSachChuyenDi.get(tripPos - 1).getMaChuyenDi();
            } else {
                return danhSachChuyenDi.get(0).getMaChuyenDi();
            }
        }
        return 1;
    }

    private void applyFilters() {
        int tripPos = spFilterDate != null ? spFilterDate.getSelectedItemPosition() : 0;
        String selectedTrip = (spFilterDate != null && spFilterDate.getSelectedItem() != null) ? spFilterDate.getSelectedItem().toString() : "Tất cả chuyến đi";
        int catPos = spFilterCategory != null ? spFilterCategory.getSelectedItemPosition() : 0;

        boolean hasFilter = (tripPos > 0) || (catPos > 0);

        chiPhiDaLoc.clear();
        for (ItemChiPhi item : tatCaChiPhi) {
            boolean matchTrip = (tripPos == 0) || (item.getTenChuyenDi() != null && item.getTenChuyenDi().contains(selectedTrip)) || selectedTrip.contains(item.getTenChuyenDi());
            boolean matchCategory = true;
            if (catPos == 1) matchCategory = (item.getLoaiDanhMuc() == ItemChiPhi.LoaiDanhMuc.DI_CHUYEN);
            else if (catPos == 2) matchCategory = (item.getLoaiDanhMuc() == ItemChiPhi.LoaiDanhMuc.AN_UONG);
            else if (catPos == 3) matchCategory = (item.getLoaiDanhMuc() == ItemChiPhi.LoaiDanhMuc.LUU_TRU);
            else if (catPos == 4) matchCategory = (item.getLoaiDanhMuc() == ItemChiPhi.LoaiDanhMuc.KHAC);

            if (matchTrip && matchCategory) {
                chiPhiDaLoc.add(item);
            }
        }

        if (adapterChiPhi != null) {
            adapterChiPhi.setDanhSachChiPhi(chiPhiDaLoc);
        }
        recalculateTotals(hasFilter, tripPos);
        updateCategoryCards();
    }

    private void updateCategoryCards() {
        long sumMove = 0;
        long sumFood = 0;
        long sumStay = 0;

        for (ItemChiPhi item : chiPhiDaLoc) {
            if (item.getLoaiDanhMuc() == ItemChiPhi.LoaiDanhMuc.DI_CHUYEN) {
                sumMove += item.getSoTien();
            } else if (item.getLoaiDanhMuc() == ItemChiPhi.LoaiDanhMuc.AN_UONG) {
                sumFood += item.getSoTien();
            } else if (item.getLoaiDanhMuc() == ItemChiPhi.LoaiDanhMuc.LUU_TRU) {
                sumStay += item.getSoTien();
            }
        }

        if (tvCatMoveAmount != null) tvCatMoveAmount.setText(formatShortMoney(sumMove));
        if (tvCatFoodAmount != null) tvCatFoodAmount.setText(formatShortMoney(sumFood));
        if (tvCatStayAmount != null) tvCatStayAmount.setText(formatShortMoney(sumStay));
    }

    private String formatShortMoney(long amount) {
        if (amount <= 0) return "0 đ";
        if (amount >= 1_000_000) {
            double m = amount / 1_000_000.0;
            if (m == (long) m) {
                return String.format(Locale.getDefault(), "%dM", (long) m);
            } else {
                return String.format(Locale.getDefault(), "%.2fM", m).replace(".00", "");
            }
        } else if (amount >= 1_000) {
            return (amount / 1000) + "K";
        }
        return amount + " đ";
    }

    private void recalculateTotals(boolean hasFilter, int tripPos) {
        long budget = 0;
        long totalSpent = 0;
        long groupSpent = 0;

        if (!hasFilter) {
            if (danhSachChuyenDi != null && !danhSachChuyenDi.isEmpty()) {
                for (ChuyenDi cd : danhSachChuyenDi) {
                    budget += cd.getNganSach();
                }
            }
            for (ItemChiPhi item : chiPhiDaLoc) {
                totalSpent += item.getSoTien();
                if (!item.isCaNhan()) {
                    groupSpent += item.getSoTien();
                }
            }
        } else {
            for (ItemChiPhi item : chiPhiDaLoc) {
                totalSpent += item.getSoTien();
                if (!item.isCaNhan()) {
                    groupSpent += item.getSoTien();
                }
            }

            if (tripPos > 0 && danhSachChuyenDi != null && tripPos - 1 < danhSachChuyenDi.size()) {
                ChuyenDi selectedCd = danhSachChuyenDi.get(tripPos - 1);
                budget = selectedCd.getNganSach();
            } else {
                if (danhSachChuyenDi != null && !danhSachChuyenDi.isEmpty()) {
                    for (ChuyenDi cd : danhSachChuyenDi) {
                        budget += cd.getNganSach();
                    }
                }
            }
        }

        long remaining = Math.max(0, budget - groupSpent);
        int percent = budget > 0 ? (int) Math.min(100, (groupSpent * 100) / budget) : 0;

        if (tvTotalBudget != null) tvTotalBudget.setText(formatMoney(budget));
        if (tvSpentAmount != null) tvSpentAmount.setText("Đã tiêu: " + formatMoney(totalSpent));
        if (tvRemainingAmount != null) tvRemainingAmount.setText("Còn lại: " + formatMoney(remaining));
        if (pbBudgetSpending != null) pbBudgetSpending.setProgress(percent);
        if (tvBadgePercent != null) tvBadgePercent.setText("Đã chi " + percent + "%");

        if (layoutWarningCard != null) {
            if (budget > 0 && percent >= 80) {
                layoutWarningCard.setVisibility(View.VISIBLE);
                if (tvWarningText != null) tvWarningText.setText("Cảnh báo: Chi tiêu đã chạm " + percent + "% hạn mức dự trù.");
            } else {
                layoutWarningCard.setVisibility(View.GONE);
            }
        }
    }

    private void datTrangThaiBanDauChiaTien() {
        int tripPos = spFilterDate != null ? spFilterDate.getSelectedItemPosition() : 0;
        ChuyenDi targetTrip = null;

        if (danhSachChuyenDi != null && !danhSachChuyenDi.isEmpty()) {
            if (tripPos > 0 && tripPos - 1 < danhSachChuyenDi.size()) {
                targetTrip = danhSachChuyenDi.get(tripPos - 1);
            } else {
                targetTrip = danhSachChuyenDi.get(0);
            }
        }

        if (targetTrip == null) {
            if (tvSplitMemberCount != null) tvSplitMemberCount.setText("0 Người");
            if (tvGroupSplitDetail != null) {
                tvGroupSplitDetail.setVisibility(View.VISIBLE);
                tvGroupSplitDetail.setText("Chưa có chuyến đi để chia tiền nhóm");
            }
            if (rvGroupSplit != null) rvGroupSplit.setVisibility(View.GONE);
            return;
        }

        List<Integer> memberIds = chuyenDiDAO.layDanhSachIdThanhVien(targetTrip.getMaChuyenDi());
        int totalMembers = (memberIds != null && !memberIds.isEmpty()) ? memberIds.size() : targetTrip.getSoThanhVien();
        if (totalMembers <= 0) totalMembers = 1;

        if (tvSplitMemberCount != null) {
            tvSplitMemberCount.setText(totalMembers + " Người");
        }
        if (tvGroupSplitDetail != null) {
            tvGroupSplitDetail.setVisibility(View.VISIBLE);
            tvGroupSplitDetail.setText("Nhấn nút \"Tính toán chia tiền\" để tự động cấn trừ nợ giữa " + totalMembers + " thành viên trong chuyến đi.");
        }
        if (rvGroupSplit != null) rvGroupSplit.setVisibility(View.GONE);
    }

    private List<String> layDanhSachNguoiThanhToan(int maChuyenDi) {
        List<String> payers = new ArrayList<>();
        payers.add("Tôi");

        int currentUserId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
        List<Integer> listMemberIds = chuyenDiDAO.layDanhSachIdThanhVien(maChuyenDi);

        if (listMemberIds != null && !listMemberIds.isEmpty()) {
            for (int mId : listMemberIds) {
                if (mId != currentUserId) {
                    NguoiDung nd = nguoiDungDAO.layTheoId(mId);
                    if (nd != null) {
                        String ten = (nd.getHoTen() != null && !nd.getHoTen().isEmpty()) ? nd.getHoTen() : nd.getTenDangNhap();
                        payers.add(ten);
                    }
                }
            }
        }
        return payers;
    }

    private void thucHienChiaTienNhom() {
        int tripPos = spFilterDate != null ? spFilterDate.getSelectedItemPosition() : 0;
        ChuyenDi targetTrip = null;

        if (danhSachChuyenDi != null && !danhSachChuyenDi.isEmpty()) {
            if (tripPos > 0 && tripPos - 1 < danhSachChuyenDi.size()) {
                targetTrip = danhSachChuyenDi.get(tripPos - 1);
            } else {
                targetTrip = danhSachChuyenDi.get(0);
            }
        }

        if (targetTrip == null) {
            if (tvSplitMemberCount != null) tvSplitMemberCount.setText("0 Người");
            if (tvGroupSplitDetail != null) {
                tvGroupSplitDetail.setVisibility(View.VISIBLE);
                tvGroupSplitDetail.setText("Chưa có chuyến đi để chia tiền nhóm");
            }
            if (rvGroupSplit != null) rvGroupSplit.setVisibility(View.GONE);
            return;
        }

        List<Integer> memberIds = chuyenDiDAO.layDanhSachIdThanhVien(targetTrip.getMaChuyenDi());
        int totalMembers = (memberIds != null && !memberIds.isEmpty()) ? memberIds.size() : targetTrip.getSoThanhVien();
        if (totalMembers <= 0) totalMembers = 1;

        if (tvSplitMemberCount != null) {
            tvSplitMemberCount.setText(totalMembers + " Người");
        }

        if (chiPhiDaLoc == null || chiPhiDaLoc.isEmpty()) {
            if (tvGroupSplitDetail != null) {
                tvGroupSplitDetail.setVisibility(View.VISIBLE);
                tvGroupSplitDetail.setText("Chưa có khoản chi tiêu nào để chia tiền nhóm");
            }
            if (rvGroupSplit != null) rvGroupSplit.setVisibility(View.GONE);
            return;
        }

        long groupTotalSpent = 0;
        for (ItemChiPhi cp : chiPhiDaLoc) {
            if (!cp.isCaNhan() && cp.getSoTien() > 0) {
                groupTotalSpent += cp.getSoTien();
            }
        }

        if (groupTotalSpent <= 0) {
            if (tvGroupSplitDetail != null) {
                tvGroupSplitDetail.setVisibility(View.VISIBLE);
                tvGroupSplitDetail.setText("Chưa có khoản chi tiêu chung (lịch trình) nào để chia tiền nhóm");
            }
            if (rvGroupSplit != null) rvGroupSplit.setVisibility(View.GONE);
            return;
        }

        long fairShare = groupTotalSpent / totalMembers;
        List<ItemChiaTien> danhSachChiaTien = new ArrayList<>();

        // 1. Dòng tổng chi phí chung chuyến đi & lịch trình
        danhSachChiaTien.add(new ItemChiaTien("C", true, "Tổng chi phí chuyến đi & lịch trình", groupTotalSpent));

        // 2. Dòng chia đều mỗi người
        if (totalMembers > 1) {
            danhSachChiaTien.add(new ItemChiaTien("M", true, "Chia đều mỗi người (" + totalMembers + " người)", fairShare));
        } else {
            danhSachChiaTien.add(new ItemChiaTien("M", true, "Chia đều cá nhân", groupTotalSpent));
        }

        // 3. Dòng các thành viên cần đóng / nợ theo góc nhìn từng tài khoản
        if (totalMembers > 1 && memberIds != null) {
            int currentUserId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
            int creatorId = targetTrip.getMaNguoiDung();

            if (currentUserId == creatorId) {
                for (int mId : memberIds) {
                    if (mId != currentUserId) {
                        NguoiDung nd = nguoiDungDAO.layTheoId(mId);
                        if (nd != null) {
                            String ten = (nd.getHoTen() != null && !nd.getHoTen().isEmpty()) ? nd.getHoTen() : nd.getTenDangNhap();
                            String initial = !ten.isEmpty() ? String.valueOf(ten.charAt(0)).toUpperCase() : "N";
                            danhSachChiaTien.add(new ItemChiaTien(initial, false, ten + " nợ / cần đóng", fairShare));
                        }
                    }
                }
            } else {
                NguoiDung creator = nguoiDungDAO.layTheoId(creatorId);
                String tenTruongNhom = (creator != null && creator.getHoTen() != null && !creator.getHoTen().isEmpty()) ? creator.getHoTen() : (creator != null ? creator.getTenDangNhap() : "Trưởng nhóm");
                danhSachChiaTien.add(new ItemChiaTien("T", false, "Tôi nợ " + tenTruongNhom + " / cần đóng", fairShare));
            }
        }

        if (!danhSachChiaTien.isEmpty()) {
            if (tvGroupSplitDetail != null) tvGroupSplitDetail.setVisibility(View.GONE);
            if (rvGroupSplit != null) rvGroupSplit.setVisibility(View.VISIBLE);
            if (adapterChiaTien != null) adapterChiaTien.setDanhSach(danhSachChiaTien);
        } else {
            if (tvGroupSplitDetail != null) {
                tvGroupSplitDetail.setVisibility(View.VISIBLE);
                tvGroupSplitDetail.setText("Chưa có dữ liệu chia tiền nhóm");
            }
            if (rvGroupSplit != null) rvGroupSplit.setVisibility(View.GONE);
        }
    }

    private ItemChiPhi.LoaiDanhMuc mapCategoryToEnum(int maDanhMuc, String tenDanhMuc) {
        if (tenDanhMuc != null) {
            String lower = tenDanhMuc.toLowerCase();
            if (lower.contains("ăn") || lower.contains("uống")) return ItemChiPhi.LoaiDanhMuc.AN_UONG;
            if (lower.contains("di chuyển") || lower.contains("xe") || lower.contains("vé")) return ItemChiPhi.LoaiDanhMuc.DI_CHUYEN;
            if (lower.contains("lưu trú") || lower.contains("khách sạn") || lower.contains("homestay")) return ItemChiPhi.LoaiDanhMuc.LUU_TRU;
        }
        switch (maDanhMuc) {
            case 1: return ItemChiPhi.LoaiDanhMuc.AN_UONG;
            case 2: return ItemChiPhi.LoaiDanhMuc.DI_CHUYEN;
            case 3: return ItemChiPhi.LoaiDanhMuc.LUU_TRU;
            default: return ItemChiPhi.LoaiDanhMuc.KHAC;
        }
    }

    private void setupAddExpenseButton() {
        if (btnAddExpense != null) {
            btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());
        }
    }

    private void showAddExpenseDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_chi_phi, null);
        dialog.setContentView(dialogView);

        Spinner spCategory = dialogView.findViewById(R.id.spCategory);
        ImageView btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSaveExpense = dialogView.findViewById(R.id.btnSaveExpense);
        EditText edtExpenseTitle = dialogView.findViewById(R.id.edtExpenseTitle);
        EditText edtExpenseAmount = dialogView.findViewById(R.id.edtExpenseAmount);

        String[] categories = new String[]{"Di chuyển", "Ăn uống", "Khách sạn / Lưu trú", "Giải trí / Khác"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner_chon, categories);
        catAdapter.setDropDownViewResource(R.layout.item_spinner_danh_sach);
        if (spCategory != null) spCategory.setAdapter(catAdapter);

        int targetMaChuyenDi = getTargetTripId();

        if (btnCloseDialog != null) btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        final int finalTargetTripId = targetMaChuyenDi;

        if (btnSaveExpense != null) {
            btnSaveExpense.setOnClickListener(v -> {
                String title = edtExpenseTitle != null ? edtExpenseTitle.getText().toString().trim() : "";
                String amountStr = edtExpenseAmount != null ? edtExpenseAmount.getText().toString().trim() : "";

                if (TextUtils.isEmpty(title)) {
                    if (edtExpenseTitle != null) edtExpenseTitle.setError("Vui lòng nhập tên khoản chi");
                    return;
                }
                if (TextUtils.isEmpty(amountStr)) {
                    if (edtExpenseAmount != null) edtExpenseAmount.setError("Vui lòng nhập số tiền");
                    return;
                }

                long amount;
                try {
                    amount = Long.parseLong(amountStr.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException e) {
                    if (edtExpenseAmount != null) edtExpenseAmount.setError("Số tiền không hợp lệ");
                    return;
                }

                if (amount <= 0) {
                    if (edtExpenseAmount != null) edtExpenseAmount.setError("Số tiền phải lớn hơn 0");
                    return;
                }

                int catPos = spCategory != null ? spCategory.getSelectedItemPosition() : 0;
                int maDanhMuc;
                switch (catPos) {
                    case 1: maDanhMuc = 1; break;
                    case 2: maDanhMuc = 3; break;
                    case 3: maDanhMuc = 6; break;
                    case 0:
                    default: maDanhMuc = 2; break;
                }

                if (finalTargetTripId <= 0) {
                    Toast.makeText(requireContext(), "Vui lòng tạo chuyến đi trước khi thêm chi phí", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                SimpleDateFormat sdfYmd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String ngayChiDb = sdfYmd.format(new Date());

                ChuyenDi cd = chuyenDiDAO.layChuyenDiTheoId(finalTargetTripId);
                if (cd != null && cd.getNgayDi() != null && !cd.getNgayDi().isEmpty()) {
                    ngayChiDb = cd.getNgayDi();
                }

                int currentUserId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
                ChiPhi chiPhi = new ChiPhi(finalTargetTripId, maDanhMuc, amount, ngayChiDb, title, 1);
                chiPhi.setMaNguoiDung(currentUserId);
                long id = chiPhiDAO.themChiPhi(chiPhi);

                if (id > 0) {
                    Toast.makeText(requireContext(), "Đã thêm khoản chi phí thành công!", Toast.LENGTH_SHORT).show();
                }

                loadDataFromDatabase();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void showEditExpenseDialog(ItemChiPhi item) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_chi_phi, null);
        dialog.setContentView(dialogView);

        Spinner spCategory = dialogView.findViewById(R.id.spCategory);
        ImageView btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSaveExpense = dialogView.findViewById(R.id.btnSaveExpense);
        EditText edtExpenseTitle = dialogView.findViewById(R.id.edtExpenseTitle);
        EditText edtExpenseAmount = dialogView.findViewById(R.id.edtExpenseAmount);

        String[] categories = new String[]{"Di chuyển", "Ăn uống", "Khách sạn / Lưu trú", "Giải trí / Khác"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner_chon, categories);
        catAdapter.setDropDownViewResource(R.layout.item_spinner_danh_sach);
        if (spCategory != null) spCategory.setAdapter(catAdapter);

        if (spCategory != null) {
            if (item.getLoaiDanhMuc() == ItemChiPhi.LoaiDanhMuc.AN_UONG) spCategory.setSelection(1);
            else if (item.getLoaiDanhMuc() == ItemChiPhi.LoaiDanhMuc.LUU_TRU) spCategory.setSelection(2);
            else if (item.getLoaiDanhMuc() == ItemChiPhi.LoaiDanhMuc.KHAC) spCategory.setSelection(3);
            else spCategory.setSelection(0);
        }

        if (edtExpenseTitle != null) {
            String cleanTitle = item.getTieuDe();
            if (cleanTitle != null && cleanTitle.contains(" (Thanh toán:")) {
                cleanTitle = cleanTitle.replaceAll(" \\(Thanh toán:.*?\\)", "").trim();
            }
            edtExpenseTitle.setText(cleanTitle);
        }
        if (edtExpenseAmount != null) edtExpenseAmount.setText(String.valueOf(item.getSoTien()));
        if (btnSaveExpense != null) btnSaveExpense.setText("Cập nhật chi phí");

        if (btnCloseDialog != null) btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSaveExpense != null) {
            btnSaveExpense.setOnClickListener(v -> {
                String title = edtExpenseTitle != null ? edtExpenseTitle.getText().toString().trim() : "";
                String amountStr = edtExpenseAmount != null ? edtExpenseAmount.getText().toString().trim() : "";

                if (TextUtils.isEmpty(title)) {
                    if (edtExpenseTitle != null) edtExpenseTitle.setError("Vui lòng nhập tên khoản chi");
                    return;
                }
                if (TextUtils.isEmpty(amountStr)) {
                    if (edtExpenseAmount != null) edtExpenseAmount.setError("Vui lòng nhập số tiền");
                    return;
                }

                long amount;
                try {
                    amount = Long.parseLong(amountStr.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException e) {
                    if (edtExpenseAmount != null) edtExpenseAmount.setError("Số tiền không hợp lệ");
                    return;
                }

                if (amount <= 0) {
                    if (edtExpenseAmount != null) edtExpenseAmount.setError("Số tiền phải lớn hơn 0");
                    return;
                }

                int catPos = spCategory != null ? spCategory.getSelectedItemPosition() : 0;
                int maDanhMuc;
                switch (catPos) {
                    case 1: maDanhMuc = 1; break;
                    case 2: maDanhMuc = 3; break;
                    case 3: maDanhMuc = 6; break;
                    case 0:
                    default: maDanhMuc = 2; break;
                }

                int maChiPhi = 0;
                try {
                    maChiPhi = Integer.parseInt(item.getMaId());
                } catch (NumberFormatException ignored) {}

                SimpleDateFormat sdfYmd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String ngayChiDb = sdfYmd.format(new Date());

                int targetTripId = getTargetTripId();
                int currentUserId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
                ChiPhi chiPhi = new ChiPhi();
                chiPhi.setMaChiPhi(maChiPhi);
                chiPhi.setMaChuyenDi(targetTripId);
                chiPhi.setMaNguoiDung(currentUserId);
                chiPhi.setMaDanhMuc(maDanhMuc);
                chiPhi.setSoTien(amount);
                chiPhi.setNgayChi(ngayChiDb);
                chiPhi.setGhiChu(title);
                chiPhi.setLaCaNhan(item.isCaNhan() ? 1 : 0);

                boolean updated = chiPhiDAO.capNhatChiPhi(chiPhi);
                if (updated) {
                    Toast.makeText(requireContext(), "Cập nhật chi phí thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Không thể cập nhật chi phí", Toast.LENGTH_SHORT).show();
                }

                loadDataFromDatabase();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void xacNhanXoaChiPhi(ItemChiPhi item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa khoản chi")
                .setMessage("Bạn có chắc chắn muốn xóa khoản chi '" + item.getTieuDe() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int maChiPhi = 0;
                    try {
                        maChiPhi = Integer.parseInt(item.getMaId());
                    } catch (NumberFormatException ignored) {}

                    if (maChiPhi > 0) {
                        boolean deleted = chiPhiDAO.xoaChiPhi(maChiPhi);
                        if (deleted) {
                            Toast.makeText(requireContext(), "Đã xóa khoản chi!", Toast.LENGTH_SHORT).show();
                            loadDataFromDatabase();
                        } else {
                            Toast.makeText(requireContext(), "Lỗi khi xóa khoản chi", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupActionButtons() {
        if (btnAutoSplit != null) {
            btnAutoSplit.setOnClickListener(v -> {
                int targetTripId = getTargetTripId();
                SharedPreferences sp = requireContext().getSharedPreferences("group_split_pref", Context.MODE_PRIVATE);
                sp.edit().putBoolean("has_calculated_split_" + targetTripId, true).apply();

                thucHienChiaTienNhom();
                Toast.makeText(requireContext(), "Đã tính toán chia tiền nhóm!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private String formatMoney(long amount) {
        return String.format(Locale.getDefault(), "%,d đ", amount).replace(',', '.');
    }
}
