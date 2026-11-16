package com.example.mytripmatee.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytripmatee.adapter.AdapterChiPhi;
import com.example.mytripmatee.databinding.DialogChiPhiBinding;
import com.example.mytripmatee.databinding.FragmentChiPhiChuyenDiBinding;
import com.example.mytripmatee.model.ItemChiPhi;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChiPhiChuyenDiFragment extends Fragment {

    private FragmentChiPhiChuyenDiBinding binding;
    private AdapterChiPhi adapterChiPhi;
    private final List<ItemChiPhi> chiPhiChuyenDi = new ArrayList<>();
    private final long tripBudget = 5000000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChiPhiChuyenDiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        loadSampleTripExpenses();
        setupAddExpenseButton();
    }

    private void setupRecyclerView() {
        adapterChiPhi = new AdapterChiPhi(item ->
                Toast.makeText(requireContext(), "Khoản chi: " + item.getTieuDe() + " - " + item.getSoTienDinhDang(), Toast.LENGTH_SHORT).show()
        );

        binding.rvTripExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTripExpenses.setAdapter(adapterChiPhi);
    }

    private void loadSampleTripExpenses() {
        chiPhiChuyenDi.clear();
        chiPhiChuyenDi.add(new ItemChiPhi(
                "1",
                "Vé máy bay Vietjet VJ362",
                "Di chuyển • Tân Sơn Nhất ➔ Liên Khương",
                1250000,
                ItemChiPhi.LoaiDanhMuc.DI_CHUYEN,
                "Tôi",
                "Đà Lạt – Sương Mù"
        ));
        chiPhiChuyenDi.add(new ItemChiPhi(
                "2",
                "Thung Lũng Mây Homestay",
                "Lưu trú • 2 đêm (View đồi)",
                1800000,
                ItemChiPhi.LoaiDanhMuc.LUU_TRU,
                "Hoàn",
                "Đà Lạt – Sương Mù"
        ));

        adapterChiPhi.setDanhSachChiPhi(chiPhiChuyenDi);
        recalculateTripTotals();
    }

    private void recalculateTripTotals() {
        long totalSpent = 0;
        for (ItemChiPhi item : chiPhiChuyenDi) {
            totalSpent += item.getSoTien();
        }

        long remaining = tripBudget - totalSpent;

        binding.tvTripTotalExpense.setText(formatMoney(totalSpent));

        if (remaining >= 0) {
            Toast.makeText(getContext(), "Tổng chi phí chuyến đi này: " + formatMoney(totalSpent), Toast.LENGTH_SHORT).show();
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
            String subtitle = catName + " • " + selectedPayer + " thanh toán";

            ItemChiPhi newItem = new ItemChiPhi(
                    String.valueOf(System.currentTimeMillis()),
                    title,
                    subtitle,
                    amount,
                    loaiDanhMuc,
                    selectedPayer,
                    "Đà Lạt – Sương Mù"
            );

            chiPhiChuyenDi.add(0, newItem);
            adapterChiPhi.themChiPhi(newItem);
            recalculateTripTotals();

            Toast.makeText(requireContext(), "Đã thêm khoản chi phí thành công!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
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
