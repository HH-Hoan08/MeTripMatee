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
    private TextView tvTripBudget;
    private TextView tvTripRemaining;
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
        tvTripBudget = view.findViewById(R.id.tvTripBudget);
        tvTripRemaining = view.findViewById(R.id.tvTripRemaining);
        btnAddExpense = view.findViewById(R.id.btnAddExpense);

        if (btnAddExpense != null) {
            btnAddExpense.setVisibility(View.GONE);
        }

        setupRecyclerView();
        loadTripExpenses();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTripExpenses();
    }

    private void setupRecyclerView() {
        adapterChiPhi = new AdapterChiPhi(item -> {
            // Xem chi phí nhóm trong chi tiết chuyến đi (Chế độ đọc)
        });
        if (rvTripExpenses != null) {
            rvTripExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvTripExpenses.setAdapter(adapterChiPhi);
        }
    }

    private void loadTripExpenses() {
        chiPhiChuyenDiList.clear();

        ChuyenDi chuyenDi = chuyenDiDAO.layChuyenDiTheoId(maChuyenDi);
        List<ChiPhi> listFromDb = chiPhiDAO.layDanhSachChiPhiNhomTheoChuyenDi(maChuyenDi);

        if (listFromDb != null && !listFromDb.isEmpty()) {
            for (ChiPhi cp : listFromDb) {
                ItemChiPhi.LoaiDanhMuc loai = mapCategoryToEnum(cp.getMaDanhMuc(), cp.getTenDanhMuc());
                String ghiChuClean = cp.getGhiChu() != null ? cp.getGhiChu() : "";
                if (ghiChuClean.contains(" (Thanh toán:")) {
                    ghiChuClean = ghiChuClean.replaceAll(" \\(Thanh toán:.*?\\)", "").trim();
                }
                String titleText = !ghiChuClean.isEmpty() ? ghiChuClean : (cp.getTenDanhMuc() != null ? cp.getTenDanhMuc() : "Khoản chi");
                String categoryName = cp.getTenDanhMuc() != null ? cp.getTenDanhMuc() : "Chi phí";
                String tag = cp.isCaNhan() ? "TÔI (Cá nhân)" : "Lịch trình nhóm";
                String sub = tag + " • " + categoryName;
                String tripName = (chuyenDi != null && chuyenDi.getTenChuyenDi() != null) ? chuyenDi.getTenChuyenDi() : "Chuyến đi";

                ItemChiPhi item = new ItemChiPhi(
                        String.valueOf(cp.getMaChiPhi()),
                        titleText,
                        sub,
                        cp.getSoTien(),
                        loai,
                        "Nhóm",
                        tripName,
                        false
                );
                chiPhiChuyenDiList.add(item);
            }
        }

        if (adapterChiPhi != null) {
            adapterChiPhi.setDanhSachChiPhi(chiPhiChuyenDiList);
        }
        recalculateTripTotals(chuyenDi);
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

    private void recalculateTripTotals(ChuyenDi chuyenDi) {
        long totalSpent = 0;
        for (ItemChiPhi item : chiPhiChuyenDiList) {
            totalSpent += item.getSoTien();
        }

        long budget = (chuyenDi != null) ? chuyenDi.getNganSach() : 0;
        long remaining = Math.max(0, budget - totalSpent);

        if (tvTripTotalExpense != null) {
            tvTripTotalExpense.setText(formatMoney(totalSpent));
        }
        if (tvTripBudget != null) {
            tvTripBudget.setText(formatMoney(budget));
        }
        if (tvTripRemaining != null) {
            tvTripRemaining.setText(formatMoney(remaining));
        }
    }

    private String formatMoney(long amount) {
        return String.format(Locale.getDefault(), "%,d đ", amount).replace(',', '.');
    }
}
