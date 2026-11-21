package com.example.mytripmatee.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.mytripmatee.R;
import com.example.mytripmatee.adapter.AdapterThuVienAnh;
import com.example.mytripmatee.dao.AnhDAO;
import com.example.mytripmatee.databinding.FragmentThuVienAnhBinding;
import com.example.mytripmatee.model.ItemAnhThuVien;

import java.util.ArrayList;
import java.util.List;

public class ThuVienAnhFragment extends Fragment {

    public static ThuVienAnhFragment newInstance(int maChuyenDi) {
        ThuVienAnhFragment fragment = new ThuVienAnhFragment();
        Bundle args = new Bundle();
        args.putInt("maChuyenDi", maChuyenDi);
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentThuVienAnhBinding binding;
    private AdapterThuVienAnh adapter;
    private AnhDAO anhDAO;
    private int maChuyenDi = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentThuVienAnhBinding.inflate(inflater, container, false);
        return binding.getRoot();
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

        anhDAO = new AnhDAO(requireContext());

        setupRecyclerView();
        loadPhotosFromDatabase();

        binding.fabAddPhoto.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Bấm để chọn và thêm hình ảnh mới cho chuyến đi", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPhotosFromDatabase();
    }

    private void setupRecyclerView() {
        adapter = new AdapterThuVienAnh(this::showPhotoEnlargedDialog);

        binding.rvGallery.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.rvGallery.setAdapter(adapter);
    }

    private void loadPhotosFromDatabase() {
        if (binding == null) return;

        List<ItemAnhThuVien> listPhotos = anhDAO.layDanhSachAnhTheoChuyenDi(maChuyenDi);
        if (listPhotos == null) {
            listPhotos = new ArrayList<>();
        }

        adapter.setDanhSachAnh(listPhotos);
    }

    private void showPhotoEnlargedDialog(ItemAnhThuVien photo) {
        if (getContext() == null || photo == null) return;

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_xem_anh);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageView imgEnlarged = dialog.findViewById(R.id.imgEnlarged);
        TextView tvTitle = dialog.findViewById(R.id.tvPreviewTitle);
        TextView tvLocation = dialog.findViewById(R.id.tvPreviewLocation);
        View btnClose = dialog.findViewById(R.id.btnClose);

        if (imgEnlarged != null) {
            imgEnlarged.setImageResource(photo.getMaAnhResId() != 0 ? photo.getMaAnhResId() : R.drawable.img_dalat);
        }
        if (tvTitle != null) {
            tvTitle.setText(photo.getTieuDe());
        }
        if (tvLocation != null) {
            tvLocation.setText("📍 " + photo.getDiaDiem());
        }
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
