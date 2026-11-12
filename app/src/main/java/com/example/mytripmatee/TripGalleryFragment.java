package com.example.mytripmatee;

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

import com.example.mytripmatee.databinding.FragmentTripGalleryBinding;

import java.util.ArrayList;
import java.util.List;

public class TripGalleryFragment extends Fragment {

    private FragmentTripGalleryBinding binding;
    private TripGalleryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTripGalleryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();

        binding.fabAddPhoto.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Mở bộ sưu tập để chọn thêm ảnh mới", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupRecyclerView() {
        adapter = new TripGalleryAdapter(this::showPhotoEnlargedDialog);

        binding.rvGallery.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.rvGallery.setAdapter(adapter);

        adapter.setPhotos(getSamplePhotos());
    }

    private void showPhotoEnlargedDialog(TripGalleryPhoto photo) {
        if (getContext() == null || photo == null) return;

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_photo_preview);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageView imgEnlarged = dialog.findViewById(R.id.imgEnlarged);
        TextView tvTitle = dialog.findViewById(R.id.tvPreviewTitle);
        TextView tvLocation = dialog.findViewById(R.id.tvPreviewLocation);
        View btnClose = dialog.findViewById(R.id.btnClose);

        if (imgEnlarged != null) {
            imgEnlarged.setImageResource(photo.getImageResId() != 0 ? photo.getImageResId() : R.drawable.img_dalat);
        }
        if (tvTitle != null) {
            tvTitle.setText(photo.getTitle());
        }
        if (tvLocation != null) {
            tvLocation.setText("📍 " + photo.getLocation());
        }
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private List<TripGalleryPhoto> getSamplePhotos() {
        List<TripGalleryPhoto> photos = new ArrayList<>();
        photos.add(new TripGalleryPhoto("1", "Sương Mù Đà Lạt", "Thung Lũng Mây • 12/11", R.drawable.img_dalat, "Cảnh đẹp"));
        photos.add(new TripGalleryPhoto("2", "Nắng Biển Phú Quốc", "Bãi Sao • 12/11", R.drawable.img_phuquoc, "Cảnh đẹp"));
        photos.add(new TripGalleryPhoto("3", "Đèo Mã Pí Lèng", "Hà Giang • 13/11", R.drawable.img_hagiang, "Cảnh đẹp"));
        photos.add(new TripGalleryPhoto("4", "Bánh căn Nhà Chung", "Quận 3, Đà Lạt • 13/11", R.drawable.img_dalat, "Ăn uống"));
        photos.add(new TripGalleryPhoto("5", "Hoàng Hôn Trên Đồi", "Đà Lạt • 14/11", R.drawable.img_phuquoc, "Cảnh đẹp"));
        photos.add(new TripGalleryPhoto("6", "Lẩu gà lá é", "Đường 3/4 • 14/11", R.drawable.img_hagiang, "Ăn uống"));
        photos.add(new TripGalleryPhoto("7", "View Homestay ban đêm", "Thung Lũng Mây • 14/11", R.drawable.img_dalat, "Lưu trú"));
        photos.add(new TripGalleryPhoto("8", "Cà phê Túi Mơ To", "Sào Nam • 15/11", R.drawable.img_phuquoc, "Ăn uống"));
        photos.add(new TripGalleryPhoto("9", "Check-in Quảng trường", "Lâm Viên • 15/11", R.drawable.img_hagiang, "Cảnh đẹp"));
        return photos;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
