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

import com.example.mytripmatee.adapter.ExpenseAdapter;
import com.example.mytripmatee.databinding.DialogExpenseBinding;
import com.example.mytripmatee.databinding.FragmentExpenseBinding;
import com.example.mytripmatee.model.ExpenseItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseFragment extends Fragment {

    private FragmentExpenseBinding binding;
    private ExpenseAdapter expenseAdapter;
    private final List<ExpenseItem> allExpenses = new ArrayList<>();
    private final List<ExpenseItem> filteredExpenses = new ArrayList<>();
    private final long totalBudget = 8000000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExpenseBinding.inflate(inflater, container, false);
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
        expenseAdapter = new ExpenseAdapter(item ->
                Toast.makeText(requireContext(), item.getTitle() + " (" + item.getTripTitle() + "): " + item.getFormattedAmount(), Toast.LENGTH_SHORT).show()
        );

        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExpenses.setAdapter(expenseAdapter);
    }

    private void loadSampleAllExpenses() {
        allExpenses.clear();
        allExpenses.add(new ExpenseItem(
                "1",
                "Vé máy bay Vietjet VJ362",
                "Di chuyển • Tân Sơn Nhất ➔ Liên Khương",
                1250000,
                ExpenseItem.CategoryType.MOVE,
                "Tôi",
                "Đà Lạt 12/11"
        ));
        allExpenses.add(new ExpenseItem(
                "2",
                "Thung Lũng Mây Homestay",
                "Lưu trú • 2 đêm (View đồi)",
                1800000,
                ExpenseItem.CategoryType.STAY,
                "Hoàn",
                "Đà Lạt 12/11"
        ));
        allExpenses.add(new ExpenseItem(
                "3",
                "Lẩu gà lá é Tao Ngộ",
                "Ăn uống • Bữa tối nhóm 4 người",
                650000,
                ExpenseItem.CategoryType.FOOD,
                "Phú",
                "Đà Lạt 12/11"
        ));
        allExpenses.add(new ExpenseItem(
                "4",
                "Resort Biển Ngọc Phú Quốc",
                "Lưu trú • Đặt cọc phòng Tết",
                1750000,
                ExpenseItem.CategoryType.STAY,
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

        filteredExpenses.clear();
        for (ExpenseItem item : allExpenses) {
            boolean matchTrip = selectedTrip.equals("Tất cả chuyến đi") || item.getTripTitle().contains(selectedTrip) || selectedTrip.contains(item.getTripTitle());
            boolean matchCategory = true;
            if (catPos == 1) matchCategory = (item.getCategoryType() == ExpenseItem.CategoryType.MOVE);
            else if (catPos == 2) matchCategory = (item.getCategoryType() == ExpenseItem.CategoryType.FOOD);
            else if (catPos == 3) matchCategory = (item.getCategoryType() == ExpenseItem.CategoryType.STAY);
            else if (catPos == 4) matchCategory = (item.getCategoryType() == ExpenseItem.CategoryType.OTHER);

            if (matchTrip && matchCategory) {
                filteredExpenses.add(item);
            }
        }

        expenseAdapter.setExpenses(filteredExpenses);
        recalculateTotals();
    }

    private void recalculateTotals() {
        long totalSpent = 0;
        for (ExpenseItem item : filteredExpenses) {
            totalSpent += item.getAmount();
        }

        long remaining = totalBudget - totalSpent;
        int percent = (int) Math.min(100, (totalSpent * 100) / totalBudget);

        binding.tvTotalBudget.setText(formatMoney(totalBudget));
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
            String selectedTrip = binding.spFilterDate.getSelectedItem() != null ? binding.spFilterDate.getSelectedItem().toString() : "Đà Lạt 12/11";

            ExpenseItem newItem = new ExpenseItem(
                    String.valueOf(System.currentTimeMillis()),
                    title,
                    catName + " • " + selectedPayer + " thanh toán",
                    amount,
                    categoryType,
                    selectedPayer,
                    selectedTrip
            );

            allExpenses.add(0, newItem);
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
