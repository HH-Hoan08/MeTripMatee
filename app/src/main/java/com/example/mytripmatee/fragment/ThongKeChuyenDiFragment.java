package com.example.mytripmatee.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mytripmatee.R;
import com.example.mytripmatee.dao.ChiPhiDAO;
import com.example.mytripmatee.dao.LichTrinhDAO;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.model.ChiPhi;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.model.LichTrinh;
import com.example.mytripmatee.format.DinhDangTien;

import java.util.List;
import java.util.Locale;

public class ThongKeChuyenDiFragment extends Fragment {

    public static ThongKeChuyenDiFragment newInstance(int maChuyenDi) {
        ThongKeChuyenDiFragment fragment = new ThongKeChuyenDiFragment();
        Bundle args = new Bundle();
        args.putInt("maChuyenDi", maChuyenDi);
        fragment.setArguments(args);
        return fragment;
    }

    private int maChuyenDi = 1;

    private TextView tvSchedulePercent;
    private TextView tvScheduleCount;
    private TextView tvBudgetPercent;
    private TextView tvBudgetDetail;
    private TextView tvTotalDistance;
    private TextView tvMainVehicle;
    private TextView tvStaySpent;
    private ProgressBar pbStaySpent;
    private TextView tvMoveSpent;
    private ProgressBar pbMoveSpent;
    private TextView tvFoodSpent;
    private ProgressBar pbFoodSpent;
    private TextView tvOtherSpent;
    private ProgressBar pbOtherSpent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thong_ke_chuyen_di, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null && getArguments().containsKey("maChuyenDi")) {
            maChuyenDi = getArguments().getInt("maChuyenDi", 1);
        } else if (requireActivity().getIntent() != null) {
            maChuyenDi = requireActivity().getIntent().getIntExtra("extra_ma_chuyen_di",
                    requireActivity().getIntent().getIntExtra("maChuyenDi", 1));
        }

        tvSchedulePercent = view.findViewById(R.id.tvSchedulePercent);
        tvScheduleCount = view.findViewById(R.id.tvScheduleCount);
        tvBudgetPercent = view.findViewById(R.id.tvBudgetPercent);
        tvBudgetDetail = view.findViewById(R.id.tvBudgetDetail);
        tvStaySpent = view.findViewById(R.id.tvStaySpent);
        pbStaySpent = view.findViewById(R.id.pbStaySpent);
        tvMoveSpent = view.findViewById(R.id.tvMoveSpent);
        pbMoveSpent = view.findViewById(R.id.pbMoveSpent);
        tvFoodSpent = view.findViewById(R.id.tvFoodSpent);
        pbFoodSpent = view.findViewById(R.id.pbFoodSpent);
        tvOtherSpent = view.findViewById(R.id.tvOtherSpent);
        pbOtherSpent = view.findViewById(R.id.pbOtherSpent);

        capNhatThongKe();
    }

    @Override
    public void onResume() {
        super.onResume();
        capNhatThongKe();
    }

    private void capNhatThongKe() {
        if (getContext() == null || getView() == null) return;

        ChuyenDiDAO chuyenDiDAO = new ChuyenDiDAO(requireContext());
        ChiPhiDAO chiPhiDAO = new ChiPhiDAO(requireContext());
        LichTrinhDAO lichTrinhDAO = new LichTrinhDAO(requireContext());

        // 1. Ngân sách & Tổng chi tiêu nhóm của chuyến đi
        ChuyenDi cd = chuyenDiDAO.layChuyenDiTheoId(maChuyenDi);
        long budget = cd != null ? cd.getNganSach() : 0;
        long totalSpent = chiPhiDAO.tinhTongChiNhomTheoChuyenDi(maChuyenDi);

        int budgetPercent = budget > 0 ? (int) Math.min(100, (totalSpent * 100) / budget) : 0;
        if (tvBudgetPercent != null) tvBudgetPercent.setText(budgetPercent + "%");
        if (tvBudgetDetail != null) tvBudgetDetail.setText(formatShortMoney(totalSpent) + " / " + formatShortMoney(budget));

        // 2. Thống kê đầy đủ 4 danh mục Tỷ trọng chi tiêu nhóm (Ăn uống, Di chuyển, Lưu trú, Giải trí/Khác)
        List<ChiPhi> listCp = chiPhiDAO.layDanhSachChiPhiNhomTheoChuyenDi(maChuyenDi);
        long foodSpent = 0;
        long moveSpent = 0;
        long staySpent = 0;
        long otherSpent = 0;

        if (listCp != null) {
            for (ChiPhi cp : listCp) {
                int dm = cp.getMaDanhMuc();
                if (dm == 1) {
                    foodSpent += cp.getSoTien();
                } else if (dm == 2) {
                    moveSpent += cp.getSoTien();
                } else if (dm == 3) {
                    staySpent += cp.getSoTien();
                } else {
                    otherSpent += cp.getSoTien();
                }
            }
        }

        int stayPercent = totalSpent > 0 ? (int) ((staySpent * 100) / totalSpent) : 0;
        int movePercent = totalSpent > 0 ? (int) ((moveSpent * 100) / totalSpent) : 0;
        int foodPercent = totalSpent > 0 ? (int) ((foodSpent * 100) / totalSpent) : 0;
        int otherPercent = totalSpent > 0 ? (int) ((otherSpent * 100) / totalSpent) : 0;

        if (tvStaySpent != null) tvStaySpent.setText(formatMoney(staySpent) + " (" + stayPercent + "%)");
        if (pbStaySpent != null) pbStaySpent.setProgress(stayPercent);

        if (tvMoveSpent != null) tvMoveSpent.setText(formatMoney(moveSpent) + " (" + movePercent + "%)");
        if (pbMoveSpent != null) pbMoveSpent.setProgress(movePercent);

        if (tvFoodSpent != null) tvFoodSpent.setText(formatMoney(foodSpent) + " (" + foodPercent + "%)");
        if (pbFoodSpent != null) pbFoodSpent.setProgress(foodPercent);

        if (tvOtherSpent != null) tvOtherSpent.setText(formatMoney(otherSpent) + " (" + otherPercent + "%)");
        if (pbOtherSpent != null) pbOtherSpent.setProgress(otherPercent);

        // 3. Tính tiến độ lịch trình thực tế
        List<LichTrinh> listLt = lichTrinhDAO.layDanhSachHoatDongTheoChuyenDi(maChuyenDi);
        int totalLt = listLt != null ? listLt.size() : 0;
        int visitedLt = 0;

        if (listLt != null) {
            for (LichTrinh lt : listLt) {
                if (lt.isDaDi()) {
                    visitedLt++;
                }
            }
        }

        int schedulePercent = totalLt > 0 ? (int) ((visitedLt * 100.0) / totalLt) : 0;
        if (tvSchedulePercent != null) tvSchedulePercent.setText(schedulePercent + "%");
        if (tvScheduleCount != null) tvScheduleCount.setText(visitedLt + "/" + totalLt + " địa điểm đã đi");

        if (tvTotalDistance != null) tvTotalDistance.setText("0 km");
        if (tvMainVehicle != null) tvMainVehicle.setText("Chưa xác định");
    }

    private String formatShortMoney(long amount) {
        return DinhDangTien.dinhDangNgan(amount);
    }

    private String formatMoney(long amount) {
        return DinhDangTien.dinhDangDong(amount);
    }
}