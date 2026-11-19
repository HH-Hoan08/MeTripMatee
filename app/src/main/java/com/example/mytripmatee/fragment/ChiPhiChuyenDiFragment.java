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
import com.example.mytripmatee.dao.ChiPhiDAO;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.databinding.DialogChiPhiBinding;
import com.example.mytripmatee.databinding.FragmentChiPhiChuyenDiBinding;
import com.example.mytripmatee.model.ChiPhi;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.model.ItemChiPhi;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChiPhiChuyenDiFragment extends Fragment {

    public static ChiPhiChuyenDiFragment newInstance(int maChuyenDi) {
        ChiPhiChuyenDiFragment fragment = new ChiPhiChuyenDiFragment();
        Bundle args = new Bundle();
        args.putInt("maChuyenDi", maChuyenDi);
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentChiPhiChuyenDiBinding binding;
    private AdapterChiPhi adapterChiPhi;
    private ChiPhiDAO chiPhiDAO;
    private ChuyenDiDAO chuyenDiDAO;
    private int maChuyenDi = 1;

    private final List<ItemChiPhi> chiPhiChuyenDiList = new ArrayList<>();
    private long tripBudget = 5000000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChiPhiChuyenDiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chiPhiDAO = new ChiPhiDAO(requireContext());
        chuyenDiDAO = new ChuyenDiDAO(requireContext());

        if (getArguments() != null && getArguments().containsKey("maChuyenDi")) {
            maChuyenDi = getArguments().getInt("maChuyenDi", 1);
        } else if (requireActivity().getIntent() != null) {
            maChuyenDi = requireActivity().getIntent().getIntExtra("extra_ma_chuyen_di",
                    requireActivity().getIntent().getIntExtra("maChuyenDi", 1));
        }

        setupRecyclerView();
        loadTripExpenses();
        setupAddExpenseButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTripExpenses();
    }

    private void setupRecyclerView() {
        adapterChiPhi = new AdapterChiPhi(item ->
                Toast.makeText(requireContext(), "Khoản chi: " + item.getTieuDe() + " - " + item.getSoTienDinhDang(), Toast.LENGTH_SHORT).show()
        );

        binding.rvTripExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTripExpenses.setAdapter(adapterChiPhi);
    }

    private void loadTripExpenses() {
        chiPhiChuyenDiList.clear();

        ChuyenDi chuyenDi = chuyenDiDAO.layChuyenDiTheoId(maChuyenDi);
        if (chuyenDi != null) {
            tripBudget = chuyenDi.getNganSach();
        }

        List<ChiPhi> listFromDb = chiPhiDAO.layDanhSachChiPhiTheoChuyenDi(maChuyenDi);
        if (listFromDb != null && !listFromDb.isEmpty()) {
            for (ChiPhi cp : listFromDb) {
                ItemChiPhi.LoaiDanhMuc loai = mapCategoryToEnum(cp.getMaDanhMuc(), cp.getTenDanhMuc());
                String sub = (cp.getTenDanhMuc() != null ? cp.getTenDanhMuc() : "Chi phí") +
                        (cp.getGhiChu() != null && !cp.getGhiChu().isEmpty() ? " • " + cp.getGhiChu() : "");
                String tripName = (chuyenDi != null && chuyenDi.getTenChuyenDi() != null) ? chuyenDi.getTenChuyenDi() : "Chuyến đi";

                ItemChiPhi item = new ItemChiPhi(
                        String.valueOf(cp.getMaChiPhi()),
                        cp.getGhiChu() != null && !cp.getGhiChu().isEmpty() ? cp.getGhiChu() : (cp.getTenDanhMuc() != null ? cp.getTenDanhMuc() : "Khoản chi"),
                        sub,
                        cp.getSoTien(),
                        loai,
                        "Tôi",
                        tripName
                );
                chiPhiChuyenDiList.add(item);
            }
        } else {
            // Hiển thị danh sách mẫu nếu DB trống
            loadSampleTripExpenses(chuyenDi != null ? chuyenDi.getTenChuyenDi() : "Đà Lạt – Sương Mù");
        }

        adapterChiPhi.setDanhSachChiPhi(chiPhiChuyenDiList);
        recalculateTripTotals();
    }

    private void loadSampleTripExpenses(String tripName) {
        chiPhiChuyenDiList.add(new ItemChiPhi(
                "1",
                "Vé máy bay Vietjet VJ362",
                "Di chuyển • Tân Sơn Nhất ➔ Liên Khương",
                1250000,
                ItemChiPhi.LoaiDanhMuc.DI_CHUYEN,
                "Tôi",
                tripName
        ));
        chiPhiChuyenDiList.add(new ItemChiPhi(
                "2",
                "Thung Lũng Mây Homestay",
                "Lưu trú • 2 đêm (View đồi)",
                1800000,
                ItemChiPhi.LoaiDanhMuc.LUU_TRU,
                "Hoàn",
                tripName
        ));
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

    private void recalculateTripTotals() {
        long totalSpent = 0;
        for (ItemChiPhi item : chiPhiChuyenDiList) {
            totalSpent += item.getSoTien();
        }

        binding.tvTripTotalExpense.setText(formatMoney(totalSpent));
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
                    maDanhMuc = 1; // Ăn uống
                    break;
                case 2:
                    maDanhMuc = 3; // Lưu trú
                    break;
                case 3:
                    maDanhMuc = 6; // Khác
                    break;
                case 0:
                default:
                    maDanhMuc = 2; // Di chuyển
                    break;
            }

            SimpleDateFormat sdfYmd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String ngayChiDb = sdfYmd.format(new Date());

            ChuyenDi cd = chuyenDiDAO.layChuyenDiTheoId(maChuyenDi);
            if (cd != null && cd.getNgayDi() != null && !cd.getNgayDi().isEmpty()) {
                ngayChiDb = cd.getNgayDi();
            }

            ChiPhi chiPhi = new ChiPhi(maChuyenDi, maDanhMuc, amount, ngayChiDb, title);
            long id = chiPhiDAO.themChiPhi(chiPhi);

            if (id > 0) {
                Toast.makeText(requireContext(), "Thêm khoản chi thành công ✓", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Đã lưu khoản chi", Toast.LENGTH_SHORT).show();
            }

            loadTripExpenses();
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
