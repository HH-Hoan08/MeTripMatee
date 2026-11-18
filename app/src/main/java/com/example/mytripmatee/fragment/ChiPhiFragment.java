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

import com.example.mytripmatee.adapter.AdapterChiPhi;
import com.example.mytripmatee.databinding.DialogChiPhiBinding;
import com.example.mytripmatee.databinding.FragmentChiPhiBinding;
import com.example.mytripmatee.model.ItemChiPhi;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChiPhiFragment extends Fragment {

    private FragmentChiPhiBinding binding;
    private AdapterChiPhi adapterChiPhi;
    private final List<ItemChiPhi> tatCaChiPhi = new ArrayList<>();
    private final List<ItemChiPhi> chiPhiDaLoc = new ArrayList<>();
    private final long tongNganSach = 8000000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChiPhiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        loadSampleAllExpenses();
        setupFilters();
        setupAddExpenseButton();
        setupActionButtons();
    }

    private void setupRecyclerView() {
        adapterChiPhi = new AdapterChiPhi(item ->
                Toast.makeText(requireContext(), item.getTieuDe() + " (" + item.getTenChuyenDi() + "): " + item.getSoTienDinhDang(), Toast.LENGTH_SHORT).show()
        );

        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExpenses.setAdapter(adapterChiPhi);
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

        applyFilters();
    }

    private void setupFilters() {
        String[] tripOptions = new String[]{"Tất cả chuyến đi", "Đà Lạt 12/11", "Phú Quốc Tết 2027", "Hà Giang T10/2026"};
        ArrayAdapter<String> tripAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, tripOptions);
        binding.spFilterDate.setAdapter(tripAdapter);

        String[] categoryOptions = new String[]{"Tất cả danh mục", "Di chuyển", "Ăn uống", "Khách sạn", "Khác"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryOptions);
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
    }

    private void applyFilters() {
        String selectedTrip = binding.spFilterDate.getSelectedItem() != null ? binding.spFilterDate.getSelectedItem().toString() : "Tất cả chuyến đi";
        int catPos = binding.spFilterCategory.getSelectedItemPosition();

        chiPhiDaLoc.clear();
        for (ItemChiPhi item : tatCaChiPhi) {
            boolean matchTrip = selectedTrip.equals("Tất cả chuyến đi") || item.getTenChuyenDi().contains(selectedTrip) || selectedTrip.contains(item.getTenChuyenDi());
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
        recalculateTotals();
    }

    private void recalculateTotals() {
        long totalSpent = 0;
        for (ItemChiPhi item : chiPhiDaLoc) {
            totalSpent += item.getSoTien();
        }

        long remaining = tongNganSach - totalSpent;
        int percent = (int) Math.min(100, (totalSpent * 100) / tongNganSach);

        binding.tvTotalBudget.setText(formatMoney(tongNganSach));
        binding.tvSpentAmount.setText("Đã tiêu: " + formatMoney(totalSpent));
        binding.tvRemainingAmount.setText("Còn lại: " + formatMoney(Math.max(0, remaining)));
        binding.pbBudgetSpending.setProgress(percent);
        binding.tvBadgePercent.setText("Đã chi " + percent + "%");
    }

    private void setupAddExpenseButton() {
        binding.btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());
    }

    private void showAddExpenseDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        DialogChiPhiBinding dialogBinding = DialogChiPhiBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        String[] categories = new String[]{"Di chuyển", "Ăn uống", "Khách sạn / Lưu trú", "Giải trí / Khác"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        dialogBinding.spCategory.setAdapter(catAdapter);

        String[] payers = new String[]{"Tôi", "Hoàn", "Phú", "Cả nhóm"};
        ArrayAdapter<String> payerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, payers);
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
                amount = Long.parseLong(amountStr);
            } catch (NumberFormatException e) {
                dialogBinding.edtExpenseAmount.setError("Số tiền không hợp lệ");
                return;
            }

            int catPos = dialogBinding.spCategory.getSelectedItemPosition();
            ItemChiPhi.LoaiDanhMuc loaiDanhMuc;
            switch (catPos) {
                case 1:
                    loaiDanhMuc = ItemChiPhi.LoaiDanhMuc.AN_UONG;
                    break;
                case 2:
                    loaiDanhMuc = ItemChiPhi.LoaiDanhMuc.LUU_TRU;
                    break;
                case 3:
                    loaiDanhMuc = ItemChiPhi.LoaiDanhMuc.KHAC;
                    break;
                case 0:
                default:
                    loaiDanhMuc = ItemChiPhi.LoaiDanhMuc.DI_CHUYEN;
                    break;
            }

            String selectedPayer = payers[dialogBinding.spPayer.getSelectedItemPosition()];
            String catName = categories[catPos];
            String selectedTrip = binding.spFilterDate.getSelectedItem() != null ? binding.spFilterDate.getSelectedItem().toString() : "Đà Lạt 12/11";

            ItemChiPhi newItem = new ItemChiPhi(
                    String.valueOf(System.currentTimeMillis()),
                    title,
                    catName + " • " + selectedPayer + " thanh toán",
                    amount,
                    loaiDanhMuc,
                    selectedPayer,
                    selectedTrip
            );

            tatCaChiPhi.add(0, newItem);
            applyFilters();

            Toast.makeText(requireContext(), "Đã thêm khoản chi phí mới!", Toast.LENGTH_SHORT).show();
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
