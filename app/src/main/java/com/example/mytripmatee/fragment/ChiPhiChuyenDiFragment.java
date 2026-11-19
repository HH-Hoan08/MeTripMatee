package com.example.mytripmatee.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.mytripmatee.dao.ChiPhiDAO;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.database.NguoiDungDAO;
import com.example.mytripmatee.model.ChiPhi;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.model.ItemChiPhi;
import com.example.mytripmatee.model.NguoiDung;
import com.example.mytripmatee.util.QuanLyPhien;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

    private AdapterChiPhi adapterChiPhi;
    private ChiPhiDAO chiPhiDAO;
    private ChuyenDiDAO chuyenDiDAO;
    private NguoiDungDAO nguoiDungDAO;
    private QuanLyPhien quanLyPhien;
    private int maChuyenDi = 1;

    private final List<ItemChiPhi> chiPhiChuyenDiList = new ArrayList<>();

    private RecyclerView rvTripExpenses;
    private TextView tvTripTotalExpense;
    private TextView btnAddExpense;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chi_phi_chuyen_di, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chiPhiDAO = new ChiPhiDAO(requireContext());
        chuyenDiDAO = new ChuyenDiDAO(requireContext());
        nguoiDungDAO = new NguoiDungDAO(requireContext());
        quanLyPhien = new QuanLyPhien(requireContext());

        if (getArguments() != null && getArguments().containsKey("maChuyenDi")) {
            maChuyenDi = getArguments().getInt("maChuyenDi", 1);
        } else if (requireActivity().getIntent() != null) {
            maChuyenDi = requireActivity().getIntent().getIntExtra("extra_ma_chuyen_di",
                    requireActivity().getIntent().getIntExtra("maChuyenDi", 1));
        }

        rvTripExpenses = view.findViewById(R.id.rvTripExpenses);
        tvTripTotalExpense = view.findViewById(R.id.tvTripTotalExpense);
        btnAddExpense = view.findViewById(R.id.btnAddExpense);

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
        adapterChiPhi = new AdapterChiPhi(this::hienThiMenuTuyChonChiPhi);
        if (rvTripExpenses != null) {
            rvTripExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvTripExpenses.setAdapter(adapterChiPhi);
        }
    }

    private void hienThiMenuTuyChonChiPhi(ItemChiPhi item) {
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

    private void loadTripExpenses() {
        chiPhiChuyenDiList.clear();

        ChuyenDi chuyenDi = chuyenDiDAO.layChuyenDiTheoId(maChuyenDi);

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
        }

        if (adapterChiPhi != null) {
            adapterChiPhi.setDanhSachChiPhi(chiPhiChuyenDiList);
        }
        recalculateTripTotals();
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

        if (tvTripTotalExpense != null) {
            tvTripTotalExpense.setText(formatMoney(totalSpent));
        }
    }

    private List<String> layDanhSachNguoiThanhToan(int idChuyenDi) {
        List<String> payers = new ArrayList<>();
        payers.add("Tôi");

        int currentUserId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
        List<Integer> listMemberIds = chuyenDiDAO.layDanhSachIdThanhVien(idChuyenDi);

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
        Spinner spPayer = dialogView.findViewById(R.id.spPayer);
        ImageView btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSaveExpense = dialogView.findViewById(R.id.btnSaveExpense);
        EditText edtExpenseTitle = dialogView.findViewById(R.id.edtExpenseTitle);
        EditText edtExpenseAmount = dialogView.findViewById(R.id.edtExpenseAmount);

        String[] categories = new String[]{"Di chuyển", "Ăn uống", "Khách sạn / Lưu trú", "Giải trí / Khác"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner_chon, categories);
        catAdapter.setDropDownViewResource(R.layout.item_spinner_danh_sach);
        if (spCategory != null) spCategory.setAdapter(catAdapter);

        List<String> payers = layDanhSachNguoiThanhToan(maChuyenDi);
        ArrayAdapter<String> payerAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner_chon, payers);
        payerAdapter.setDropDownViewResource(R.layout.item_spinner_danh_sach);
        if (spPayer != null) spPayer.setAdapter(payerAdapter);

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
        }

        dialog.show();
    }

    private void showEditExpenseDialog(ItemChiPhi item) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_chi_phi, null);
        dialog.setContentView(dialogView);

        Spinner spCategory = dialogView.findViewById(R.id.spCategory);
        Spinner spPayer = dialogView.findViewById(R.id.spPayer);
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

        List<String> payers = layDanhSachNguoiThanhToan(maChuyenDi);
        ArrayAdapter<String> payerAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner_chon, payers);
        payerAdapter.setDropDownViewResource(R.layout.item_spinner_danh_sach);
        if (spPayer != null) spPayer.setAdapter(payerAdapter);

        if (edtExpenseTitle != null) edtExpenseTitle.setText(item.getTieuDe());
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

                ChiPhi chiPhi = new ChiPhi();
                chiPhi.setMaChiPhi(maChiPhi);
                chiPhi.setMaDanhMuc(maDanhMuc);
                chiPhi.setSoTien(amount);
                chiPhi.setNgayChi(ngayChiDb);
                chiPhi.setGhiChu(title);

                boolean updated = chiPhiDAO.capNhatChiPhi(chiPhi);
                if (updated) {
                    Toast.makeText(requireContext(), "Cập nhật chi phí thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Không thể cập nhật chi phí", Toast.LENGTH_SHORT).show();
                }

                loadTripExpenses();
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
                            loadTripExpenses();
                        } else {
                            Toast.makeText(requireContext(), "Lỗi khi xóa khoản chi", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private String formatMoney(long amount) {
        return String.format(Locale.getDefault(), "%,d đ", amount).replace(',', '.');
    }
}