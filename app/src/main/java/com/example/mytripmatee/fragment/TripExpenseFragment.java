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

import com.example.mytripmatee.adapter.ExpenseAdapter;
import com.example.mytripmatee.databinding.DialogExpenseBinding;
import com.example.mytripmatee.databinding.FragmentTripExpenseBinding;
import com.example.mytripmatee.model.ExpenseItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TripExpenseFragment extends Fragment {

    private FragmentTripExpenseBinding binding;
    private ExpenseAdapter expenseAdapter;
    private final List<ExpenseItem> tripExpenses = new ArrayList<>();
    private final long tripBudget = 5000000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTripExpenseBinding.inflate(inflater, container, false);
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
        expenseAdapter = new ExpenseAdapter(item ->
                Toast.makeText(requireContext(), "Khoản chi: " + item.getTitle() + " - " + item.getFormattedAmount(), Toast.LENGTH_SHORT).show()
        );

        binding.rvTripExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTripExpenses.setAdapter(expenseAdapter);
    }

    private void loadSampleTripExpenses() {
        tripExpenses.clear();
        tripExpenses.add(new ExpenseItem(
                "1",
                "Vé máy bay Vietjet VJ362",
                "Di chuyển • Tân Sơn Nhất ➔ Liên Khương",
                1250000,
                ExpenseItem.CategoryType.MOVE,
                "Tôi",
                "Đà Lạt – Sương Mù"
        ));
        tripExpenses.add(new ExpenseItem(
                "2",
                "Thung Lũng Mây Homestay",
                "Lưu trú • 2 đêm (View đồi)",
                1800000,
                ExpenseItem.CategoryType.STAY,
                "Hoàn",
                "Đà Lạt – Sương Mù"
        ));

        expenseAdapter.setExpenses(tripExpenses);
        recalculateTripTotals();
    }

    private void recalculateTripTotals() {
        long totalSpent = 0;
        for (ExpenseItem item : tripExpenses) {
            totalSpent += item.getAmount();
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
        DialogExpenseBinding dialogBinding = DialogExpenseBinding.inflate(getLayoutInflater());
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
            ExpenseItem.CategoryType categoryType;
            switch (catPos) {
                case 1:
                    categoryType = ExpenseItem.CategoryType.FOOD;
                    break;
                case 2:
                    categoryType = ExpenseItem.CategoryType.STAY;
                    break;
                case 3:
                    categoryType = ExpenseItem.CategoryType.OTHER;
                    break;
                case 0:
                default:
                    categoryType = ExpenseItem.CategoryType.MOVE;
                    break;
            }

            String selectedPayer = payers[dialogBinding.spPayer.getSelectedItemPosition()];
            String catName = categories[catPos];
            String subtitle = catName + " • " + selectedPayer + " thanh toán";

            ExpenseItem newItem = new ExpenseItem(
                    String.valueOf(System.currentTimeMillis()),
                    title,
                    subtitle,
                    amount,
                    categoryType,
                    selectedPayer,
                    "Đà Lạt – Sương Mù"
            );

            tripExpenses.add(0, newItem);
            expenseAdapter.addExpense(newItem);
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
