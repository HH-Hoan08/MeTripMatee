package com.example.mytripmatee.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytripmatee.R;
import com.example.mytripmatee.activity.CaiDatActivity;
import com.example.mytripmatee.activity.ChiTietChuyenDiActivity;
import com.example.mytripmatee.adapter.AdapterChuyenDi;
import com.example.mytripmatee.databinding.FragmentKhamPhaBinding;
import com.example.mytripmatee.model.ItemChuyenDi;

import java.util.ArrayList;
import java.util.List;

public class KhamPhaFragment extends Fragment {

    private FragmentKhamPhaBinding binding;
    private AdapterChuyenDi adapterChuyenDi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentKhamPhaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFeaturedCard();
        setupSettingsButton();
    }

    private void setupRecyclerView() {
        adapterChuyenDi = new AdapterChuyenDi(itemChuyenDi -> {
            if (getContext() != null) {
                Intent intent = ChiTietChuyenDiActivity.newIntent(
                        getContext(),
                        itemChuyenDi.getTieuDe(),
                        itemChuyenDi.getTheNgay() != null ? itemChuyenDi.getTheNgay() : "Chi tiết chuyến đi"
                );
                startActivity(intent);
            }
        });

        binding.rvTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTrips.setAdapter(adapterChuyenDi);

        adapterChuyenDi.setDanhSachChuyenDi(getSampleTrips());
    }

    private void setupFeaturedCard() {
        binding.cardFeatured.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = ChiTietChuyenDiActivity.newIntent(
                        getContext(),
                        "Đà Lạt – Sương Mù & Thảnh Thơi",
                        "12/11 - 15/11/2026 • 3 ngày 2 đêm"
                );
                startActivity(intent);
            }
        });
    }

    private void setupSettingsButton() {
        binding.btnSettings.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), CaiDatActivity.class);
                startActivity(intent);
            }
        });
    }

    private List<ItemChuyenDi> getSampleTrips() {
        List<ItemChuyenDi> danhSach = new ArrayList<>();
        danhSach.add(new ItemChuyenDi(
                "1",
                "Phú Quốc – Nghỉ Dưỡng Biển Ngọc",
                "Bản nháp",
                ItemChuyenDi.LoaiTrangThai.NHAP,
                "Tết 2027",
                "4 ngày 3 đêm • 12 mục đã lập",
                R.drawable.img_phuquoc
        ));
        danhSach.add(new ItemChuyenDi(
                "2",
                "Hà Giang – Chinh Phục Mã Pí Lèng",
                "Đã xong",
                ItemChuyenDi.LoaiTrangThai.DA_HOAN_THANH,
                "T10/2026",
                "3 ngày 2 đêm • Hoàn thành 100%",
                R.drawable.img_hagiang
        ));
        danhSach.add(new ItemChuyenDi(
                "3",
                "Đà Lạt – Sương Mù & Thảnh Thơi",
                "Sắp tới",
                ItemChuyenDi.LoaiTrangThai.SAP_TOI,
                "12/11 - 15/11/2026",
                "3 ngày 2 đêm • Đang đếm ngược",
                R.drawable.img_dalat
        ));
        return danhSach;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
