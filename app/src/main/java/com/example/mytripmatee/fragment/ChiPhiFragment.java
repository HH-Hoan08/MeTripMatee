package com.example.mytripmatee.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytripmatee.R;
import com.example.mytripmatee.adapter.AdapterChiPhi;
import com.example.mytripmatee.dao.ChiPhiDAO;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.databinding.DialogChiPhiBinding;
import com.example.mytripmatee.databinding.FragmentChiPhiBinding;
import com.example.mytripmatee.model.ChiPhi;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.model.ItemChiPhi;
import com.example.mytripmatee.util.QuanLyPhien;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChiPhiFragment extends Fragment {

    private FragmentChiPhiBinding binding;
    private AdapterChiPhi adapterChiPhi;
    private ChuyenDiDAO chuyenDiDAO;
    private ChiPhiDAO chiPhiDAO;
    private QuanLyPhien quanLyPhien;

    private List<ChuyenDi> danhSachChuyenDi = new ArrayList<>();
    private final List<ItemChiPhi> tatCaChiPhi = new ArrayList<>();
    private final List<ItemChiPhi> chiPhiDaLoc = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChiPhiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chuyenDiDAO = new ChuyenDiDAO(requireContext());
        chiPhiDAO = new ChiPhiDAO(requireContext());
        quanLyPhien = new QuanLyPhien(requireContext());

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
        adapterChiPhi = new AdapterChiPhi(item ->
                Toast.makeText(requireContext(), item.getTieuDe() + " (" + item.getTenChuyenDi() + "): " + item.getSoTienDinhDang(), Toast.LENGTH_SHORT).show()
        );

        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExpenses.setAdapter(adapterChiPhi);
    }

    private void loadDataFromDatabase() {
        int userId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
        if (userId <= 0) userId = 1;

        danhSachChuyenDi = chuyenDiDAO.layDanhSachChuyenDi(userId);
        tatCaChiPhi.clear();

        if (danhSachChuyenDi != null && !danhSachChuyenDi.isEmpty()) {
            for (ChuyenDi cd : danhSachChuyenDi) {
                List<ChiPhi> listCp = chiPhiDAO.layDanhSachChiPhiTheoChuyenDi(cd.getMaChuyenDi());
                if (listCp != null) {
                    for (ChiPhi cp : listCp) {
                        ItemChiPhi.LoaiDanhMuc loai = mapCategoryToEnum(cp.getMaDanhMuc(), cp.getTenDanhMuc());
                        String sub = (cp.getTenDanhMuc() != null ? cp.getTenDanhMuc() : "Chi phí") +
                                (cp.getGhiChu() != null && !cp.getGhiChu().isEmpty() ? " • " + cp.getGhiChu() : "");

                        ItemChiPhi item = new ItemChiPhi(
                                String.valueOf(cp.getMaChiPhi()),
                                cp.getGhiChu() != null && !cp.getGhiChu().isEmpty() ? cp.getGhiChu() : (cp.getTenDanhMuc() != null ? cp.getTenDanhMuc() : "Khoản chi"),
                                sub,
                                cp.getSoTien(),
                                loai,
                                "Tôi",
                                cd.getTenChuyenDi()
                        );
                        tatCaChiPhi.add(item);
                    }
                }
            }
        }

        if (tatCaChiPhi.isEmpty()) {
            loadSampleAllExpenses();
        } else {
            setupFilters();
        }
    }

    private void loadSampleAllExpenses() {
        tatCaChiPhi.clear();
        tatCaChiPhi.add(new ItemChiPhi(
                "1",
                "Vé máy bay Vietjet VJ362",
                "Di chuyển • Tân Sơn Nhất ➔ Liên Khương",
                1250000,
                ItemChiPhi.LoaiDanhMuc.DI_CHUYEN,
                "Tôi",
                "Đà Lạt 12/11"
        ));
        tatCaChiPhi.add(new ItemChiPhi(
                "2",
                "Thung Lũng Mây Homestay",
                "Lưu trú • 2 đêm (View đồi)",
                1800000,
                ItemChiPhi.LoaiDanhMuc.LUU_TRU,
                "Hoàn",
                "Đà Lạt 12/11"
        ));
        tatCaChiPhi.add(new ItemChiPhi(
                "3",
                "Lẩu gà lá é Tao Ngộ",
                "Ăn uống • Bữa tối nhóm 4 người",
                650000,
                ItemChiPhi.LoaiDanhMuc.AN_UONG,
                "Phú",
                "Đà Lạt 12/11"
        ));
        tatCaChiPhi.add(new ItemChiPhi(
                "4",
                "Resort Biển Ngọc Phú Quốc",
                "Lưu trú • Đặt cọc phòng Tết",
                1750000,
                ItemChiPhi.LoaiDanhMuc.LUU_TRU,
                "Tôi",
                "Phú Quốc Tết 2027"
        ));

        setupFilters();
    }

    private void setupFilters() {
        List<String> tripOptions = new ArrayList<>();
        tripOptions.add("Tất cả chuyến đi");
        if (danhSachChuyenDi != null && !danhSachChuyenDi.isEmpty()) {
            for (ChuyenDi cd : danhSachChuyenDi) {
                tripOptions.add(cd.getTenChuyenDi());
            }
        } else {
            tripOptions.add("Đà Lạt 12/11");
            tripOptions.add("Phú Quốc Tết 2027");
            tripOptions.add("Hà Giang T10/2026");
        }

        ArrayAdapter<String> tripAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner_chon, tripOptions);
        tripAdapter.setDropDownViewResource(R.layout.item_spinner_danh_sach);
        binding.spFilterDate.setAdapter(tripAdapter);

        String[] categoryOptions = new String[]{"Tất cả danh mục", "Di chuyển", "Ăn uống", "Khách sạn", "Khác"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner_chon, categoryOptions);
        catAdapter.setDropDownViewResource(R.layout.item_spinner_danh_sach);
        binding.spFilterCategory.setAdapter(catAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        binding.spFilterDate.setOnItemSelectedListener(filterListener);
        binding.spFilterCategory.setOnItemSelectedListener(filterListener);

        applyFilters();
    }

    private void applyFilters() {
        int tripPos = binding.spFilterDate.getSelectedItemPosition();
        String selectedTrip = binding.spFilterDate.getSelectedItem() != null ? binding.spFilterDate.getSelectedItem().toString() : "Tất cả chuyến đi";
        int catPos = binding.spFilterCategory.getSelectedItemPosition();

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

        adapterChiPhi.setDanhSachChiPhi(chiPhiDaLoc);
        recalculateTotals(hasFilter, tripPos);
        updateCategoryCards();
    }

    private void updateCategoryCards() {
        long sumMove = 0;
        long sumFood = 0;
        long sumStay = 0;

        List<ItemChiPhi> targetList = chiPhiDaLoc;
        for (ItemChiPhi item : targetList) {
            if (item.getLoaiDanhMuc() == ItemChiPhi.LoaiDanhMuc.DI_CHUYEN) {
                sumMove += item.getSoTien();
            } else if (item.getLoaiDanhMuc() == ItemChiPhi.LoaiDanhMuc.AN_UONG) {
                sumFood += item.getSoTien();
            } else if (item.getLoaiDanhMuc() == ItemChiPhi.LoaiDanhMuc.LUU_TRU) {
                sumStay += item.getSoTien();
            }
        }

        binding.tvCatMoveAmount.setText(formatShortMoney(sumMove));
        binding.tvCatFoodAmount.setText(formatShortMoney(sumFood));
        binding.tvCatStayAmount.setText(formatShortMoney(sumStay));
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

    /**
     * TÍNH TỔNG TIỀN VÀ NGÂN SÁCH:
     * - Khi KHÔNG CÓ BỘ LỌC (hasFilter == false): Tính tổng tiền & ngân sách của 1 chuyến đi (chuyến đi chính/đầu tiên).
     * - Khi CÓ BỘ LỌC (hasFilter == true): Tính tổng tiền theo bộ lọc (các khoản chi đã lọc chiPhiDaLoc).
     */
    private void recalculateTotals(boolean hasFilter, int tripPos) {
        long budget;
        long totalSpent = 0;

        if (!hasFilter) {
            // KHÔNG CÓ BỘ LỌC: Tính tổng tiền & ngân sách của 1 chuyến đi
            if (danhSachChuyenDi != null && !danhSachChuyenDi.isEmpty()) {
                ChuyenDi singleTrip = danhSachChuyenDi.get(0);
                budget = singleTrip.getNganSach();
                totalSpent = singleTrip.getTongChi();
            } else {
                // Mẫu: 1 chuyến đi Đà Lạt
                budget = 5000000;
                for (ItemChiPhi item : tatCaChiPhi) {
                    if ("Đà Lạt 12/11".equals(item.getTenChuyenDi())) {
                        totalSpent += item.getSoTien();
                    }
                }
            }
        } else {
            // CÓ BỘ LỌC: Tính tổng tiền theo bộ lọc (chiPhiDaLoc)
            for (ItemChiPhi item : chiPhiDaLoc) {
                totalSpent += item.getSoTien();
            }

            // Nếu lọc theo 1 chuyến đi cụ thể
            if (tripPos > 0 && danhSachChuyenDi != null && tripPos - 1 < danhSachChuyenDi.size()) {
                ChuyenDi selectedCd = danhSachChuyenDi.get(tripPos - 1);
                budget = selectedCd.getNganSach();
            } else {
                budget = 8000000;
            }
        }

        long remaining = Math.max(0, budget - totalSpent);
        int percent = (int) Math.min(100, (totalSpent * 100) / Math.max(1, budget));

        binding.tvTotalBudget.setText(formatMoney(budget));
        binding.tvSpentAmount.setText("Đã tiêu: " + formatMoney(totalSpent));
        binding.tvRemainingAmount.setText("Còn lại: " + formatMoney(remaining));
        binding.pbBudgetSpending.setProgress(percent);
        binding.tvBadgePercent.setText("Đã chi " + percent + "%");

        // Cảnh báo ngân sách (chỉ hiện khi đã tiêu >= 80% ngân sách)
        if (percent >= 80) {
            binding.layoutWarningCard.setVisibility(View.VISIBLE);
            binding.tvWarningText.setText("Cảnh báo: Chi tiêu đã chạm " + percent + "% hạn mức dự trù.");
        } else {
            binding.layoutWarningCard.setVisibility(View.GONE);
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
        binding.btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());
    }

    private void showAddExpenseDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        DialogChiPhiBinding dialogBinding = DialogChiPhiBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        String[] categories = new String[]{"Di chuyển", "Ăn uống", "Khách sạn / Lưu trú", "Giải trí / Khác"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner_chon, categories);
        catAdapter.setDropDownViewResource(R.layout.item_spinner_danh_sach);
        dialogBinding.spCategory.setAdapter(catAdapter);

        String[] payers = new String[]{"Tôi", "Hoàn", "Phú", "Cả nhóm"};
        ArrayAdapter<String> payerAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner_chon, payers);
        payerAdapter.setDropDownViewResource(R.layout.item_spinner_danh_sach);
        dialogBinding.spPayer.setAdapter(payerAdapter);

        dialogBinding.btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnSaveExpense.setOnClickListener(v -> {
            String title = dialogBinding.edtExpenseTitle.getText().toString().trim();
            String amountStr = dialogBinding.edtExpenseAmount.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                dialogBinding.edtExpenseTitle.setError("Vui lòng nhập tên khoản chi");
                return;
            }
            if (TextUtils.isEmpty(amountStr)) {
                dialogBinding.edtExpenseAmount.setError("Vui lòng nhập số tiền");
                return;
            }

            long amount = 0;
            try {
                amount = Long.parseLong(amountStr.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                dialogBinding.edtExpenseAmount.setError("Số tiền không hợp lệ");
                return;
            }

            if (amount <= 0) {
                dialogBinding.edtExpenseAmount.setError("Số tiền phải lớn hơn 0");
                return;
            }

            int catPos = dialogBinding.spCategory.getSelectedItemPosition();
            int maDanhMuc;
            switch (catPos) {
                case 1:
                    maDanhMuc = 1;
                    break;
                case 2:
                    maDanhMuc = 3;
                    break;
                case 3:
                    maDanhMuc = 6;
                    break;
                case 0:
                default:
                    maDanhMuc = 2;
                    break;
            }

            int targetMaChuyenDi = 1;
            if (danhSachChuyenDi != null && !danhSachChuyenDi.isEmpty()) {
                targetMaChuyenDi = danhSachChuyenDi.get(0).getMaChuyenDi();
            }

            SimpleDateFormat sdfYmd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String ngayChiDb = sdfYmd.format(new Date());

            ChuyenDi cd = chuyenDiDAO.layChuyenDiTheoId(targetMaChuyenDi);
            if (cd != null && cd.getNgayDi() != null && !cd.getNgayDi().isEmpty()) {
                ngayChiDb = cd.getNgayDi();
            }

            ChiPhi chiPhi = new ChiPhi(targetMaChuyenDi, maDanhMuc, amount, ngayChiDb, title);
            long id = chiPhiDAO.themChiPhi(chiPhi);

            if (id > 0) {
                Toast.makeText(requireContext(), "Đã thêm khoản chi phí thành công!", Toast.LENGTH_SHORT).show();
            }

            loadDataFromDatabase();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupActionButtons() {
        binding.btnExport.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Đang xuất báo cáo chi tiêu dạng Excel / PDF...", Toast.LENGTH_SHORT).show()
        );

        binding.btnAutoSplit.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Đang tự động chia đều tiền nhóm (Ai nợ Ai)...", Toast.LENGTH_SHORT).show()
        );
    }

    private String formatMoney(long amount) {
        return String.format(Locale.getDefault(), "%,d đ", amount).replace(',', '.');
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
