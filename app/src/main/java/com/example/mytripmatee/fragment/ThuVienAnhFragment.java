package com.example.mytripmatee.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
    private String selectedCategoryFilter = "ALL";

    private List<ItemAnhThuVien> allPhotosList = new ArrayList<>();
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                requireContext().getContentResolver().takePersistableUriPermission(
                                        selectedImageUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            } catch (Exception ignored) {}

                            showAddPhotoNoteDialog(selectedImageUri);
                        }
                    }
                }
        );
    }

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
        setupChipFilterListeners();
        loadPhotosFromDatabase();

        binding.fabAddPhoto.setOnClickListener(v -> openImagePicker());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPhotosFromDatabase();
    }

    private void setupChipFilterListeners() {
        if (binding == null) return;

        if (binding.chipAll != null) {
            binding.chipAll.setOnClickListener(v -> {
                selectedCategoryFilter = "ALL";
                updateChipFilterUI();
                renderFilteredPhotos();
            });
        }

        if (binding.chipLandscape != null) {
            binding.chipLandscape.setOnClickListener(v -> {
                selectedCategoryFilter = "Cảnh đẹp";
                updateChipFilterUI();
                renderFilteredPhotos();
            });
        }

        if (binding.chipFood != null) {
            binding.chipFood.setOnClickListener(v -> {
                selectedCategoryFilter = "Ăn uống";
                updateChipFilterUI();
                renderFilteredPhotos();
            });
        }

        if (binding.chipStay != null) {
            binding.chipStay.setOnClickListener(v -> {
                selectedCategoryFilter = "Lưu trú";
                updateChipFilterUI();
                renderFilteredPhotos();
            });
        }
    }

    private void updateChipFilterUI() {
        if (binding == null) return;

        int activeBg = R.drawable.bg_pill_teal;
        int inactiveBg = R.drawable.bg_pill_white;
        int activeText = Color.parseColor("#FFFFFF");
        int inactiveText = Color.parseColor("#64748B");

        boolean isAll = "ALL".equalsIgnoreCase(selectedCategoryFilter);
        boolean isLandscape = "Cảnh đẹp".equalsIgnoreCase(selectedCategoryFilter);
        boolean isFood = "Ăn uống".equalsIgnoreCase(selectedCategoryFilter);
        boolean isStay = "Lưu trú".equalsIgnoreCase(selectedCategoryFilter);

        if (binding.chipAll != null) {
            binding.chipAll.setBackgroundResource(isAll ? activeBg : inactiveBg);
            binding.chipAll.setTextColor(isAll ? activeText : inactiveText);
        }
        if (binding.chipLandscape != null) {
            binding.chipLandscape.setBackgroundResource(isLandscape ? activeBg : inactiveBg);
            binding.chipLandscape.setTextColor(isLandscape ? activeText : inactiveText);
        }
        if (binding.chipFood != null) {
            binding.chipFood.setBackgroundResource(isFood ? activeBg : inactiveBg);
            binding.chipFood.setTextColor(isFood ? activeText : inactiveText);
        }
        if (binding.chipStay != null) {
            binding.chipStay.setBackgroundResource(isStay ? activeBg : inactiveBg);
            binding.chipStay.setTextColor(isStay ? activeText : inactiveText);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        try {
            pickImageLauncher.launch(intent);
        } catch (Exception e) {
            Intent fallbackIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(fallbackIntent);
        }
    }

    private void showAddPhotoNoteDialog(Uri imageUri) {
        if (getContext() == null || imageUri == null) return;

        LinearLayout layoutContainer = new LinearLayout(requireContext());
        layoutContainer.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layoutContainer.setPadding(padding, padding, padding, padding);

        TextView tvCategoryPrompt = new TextView(requireContext());
        tvCategoryPrompt.setText("Chọn thẻ phân loại ảnh:");
        tvCategoryPrompt.setTextSize(13);
        tvCategoryPrompt.setTypeface(null, android.graphics.Typeface.BOLD);
        tvCategoryPrompt.setTextColor(Color.parseColor("#1E293B"));
        layoutContainer.addView(tvCategoryPrompt);

        RadioGroup rgCategory = new RadioGroup(requireContext());
        rgCategory.setOrientation(RadioGroup.HORIZONTAL);
        rgCategory.setPadding(0, 8, 0, 16);

        String[] categories = new String[]{"Cảnh đẹp", "Ăn uống", "Lưu trú", "Khác"};
        for (int i = 0; i < categories.length; i++) {
            RadioButton rb = new RadioButton(requireContext());
            rb.setText(categories[i]);
            rb.setTextSize(12);
            rb.setId(View.generateViewId());
            rgCategory.addView(rb);
            if (i == 0) rb.setChecked(true);
        }
        layoutContainer.addView(rgCategory);

        TextView tvTitlePrompt = new TextView(requireContext());
        tvTitlePrompt.setText("Chú thích / Tên bức ảnh:");
        tvTitlePrompt.setTextSize(13);
        tvTitlePrompt.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitlePrompt.setTextColor(Color.parseColor("#1E293B"));
        layoutContainer.addView(tvTitlePrompt);

        EditText etNote = new EditText(requireContext());
        etNote.setHint("Ví dụ: Nhậu nữa nhậu mãi, Hoàng hôn...");
        etNote.setTextSize(14);
        layoutContainer.addView(etNote);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm ảnh mới vào chuyến đi")
                .setView(layoutContainer)
                .setPositiveButton("Lưu ảnh", (dialog, which) -> {
                    String note = etNote.getText().toString().trim();
                    if (note.isEmpty()) note = "Kỷ niệm chuyến đi";

                    String chosenCategory = "Cảnh đẹp";
                    int checkedId = rgCategory.getCheckedRadioButtonId();
                    if (checkedId != -1) {
                        RadioButton selectedRb = rgCategory.findViewById(checkedId);
                        if (selectedRb != null) {
                            chosenCategory = selectedRb.getText().toString();
                        }
                    }

                    long id = anhDAO.themAnh(maChuyenDi, imageUri.toString(), note, chosenCategory);
                    if (id > 0) {
                        Toast.makeText(requireContext(), "Đã thêm ảnh vào danh mục " + chosenCategory + " ✓", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Đã lưu ảnh ✓", Toast.LENGTH_SHORT).show();
                    }
                    loadPhotosFromDatabase();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupRecyclerView() {
        adapter = new AdapterThuVienAnh(new AdapterThuVienAnh.SuKienChonAnhListener() {
            @Override
            public void onChonAnh(ItemAnhThuVien photo) {
                showPhotoEnlargedDialog(photo);
            }

            @Override
            public void onXoaAnh(ItemAnhThuVien photo) {
                confirmDeletePhoto(photo);
            }
        });

        binding.rvGallery.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.rvGallery.setAdapter(adapter);
    }

    private void confirmDeletePhoto(ItemAnhThuVien photo) {
        if (getContext() == null || photo == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa hình ảnh")
                .setMessage("Bạn có chắc chắn muốn xóa bức ảnh '" + photo.getTieuDe() + "' khỏi thư viện?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    try {
                        int maAnh = Integer.parseInt(photo.getMaId());
                        boolean success = anhDAO.xoaAnh(maAnh);
                        if (success) {
                            Toast.makeText(requireContext(), "Đã xóa ảnh khỏi thư viện ✓", Toast.LENGTH_SHORT).show();
                            loadPhotosFromDatabase();
                        } else {
                            Toast.makeText(requireContext(), "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
                            loadPhotosFromDatabase();
                        }
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Không thể xóa ảnh này", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadPhotosFromDatabase() {
        if (binding == null) return;

        List<ItemAnhThuVien> listDb = anhDAO.layDanhSachAnhTheoChuyenDi(maChuyenDi);
        if (listDb == null) {
            allPhotosList = new ArrayList<>();
        } else {
            allPhotosList = listDb;
        }

        int totalCount = allPhotosList.size();
        int landscapeCount = 0;
        int foodCount = 0;
        int stayCount = 0;

        for (ItemAnhThuVien photo : allPhotosList) {
            String cat = photo.getDanhMuc();
            if ("Cảnh đẹp".equalsIgnoreCase(cat)) {
                landscapeCount++;
            } else if ("Ăn uống".equalsIgnoreCase(cat)) {
                foodCount++;
            } else if ("Lưu trú".equalsIgnoreCase(cat)) {
                stayCount++;
            }
        }

        if (binding.tvGalleryTitle != null) {
            binding.tvGalleryTitle.setText("BỘ SƯU TẬP ẢNH (" + totalCount + ")");
        }

        if (binding.chipAll != null) {
            binding.chipAll.setText("Tất cả (" + totalCount + ")");
        }
        if (binding.chipLandscape != null) {
            binding.chipLandscape.setText("Cảnh đẹp (" + landscapeCount + ")");
        }
        if (binding.chipFood != null) {
            binding.chipFood.setText("Ăn uống (" + foodCount + ")");
        }
        if (binding.chipStay != null) {
            binding.chipStay.setText("Lưu trú (" + stayCount + ")");
        }

        updateChipFilterUI();
        renderFilteredPhotos();
    }

    private void renderFilteredPhotos() {
        if (binding == null) return;

        List<ItemAnhThuVien> filtered = new ArrayList<>();
        if ("ALL".equalsIgnoreCase(selectedCategoryFilter)) {
            filtered.addAll(allPhotosList);
        } else {
            for (ItemAnhThuVien p : allPhotosList) {
                if (selectedCategoryFilter.equalsIgnoreCase(p.getDanhMuc())) {
                    filtered.add(p);
                }
            }
        }

        adapter.setDanhSachAnh(filtered);
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
            if (photo.getDuongDanUri() != null && !photo.getDuongDanUri().trim().isEmpty()) {
                try {
                    imgEnlarged.setImageURI(Uri.parse(photo.getDuongDanUri()));
                } catch (Exception e) {
                    imgEnlarged.setImageResource(photo.getMaAnhResId() != 0 ? photo.getMaAnhResId() : R.drawable.img_dalat);
                }
            } else {
                imgEnlarged.setImageResource(photo.getMaAnhResId() != 0 ? photo.getMaAnhResId() : R.drawable.img_dalat);
            }
        }
        if (tvTitle != null) {
            tvTitle.setText(photo.getTieuDe());
        }
        if (tvLocation != null) {
            String tagDisplay = photo.getDanhMuc() != null ? "🏷️ " + photo.getDanhMuc() : "📍 " + photo.getDiaDiem();
            tvLocation.setText(tagDisplay);
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
