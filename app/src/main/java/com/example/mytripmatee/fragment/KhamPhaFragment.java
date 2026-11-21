package com.example.mytripmatee.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytripmatee.R;
import com.example.mytripmatee.activity.CaiDatActivity;
import com.example.mytripmatee.activity.ChiTietChuyenDiActivity;
import com.example.mytripmatee.adapter.AdapterChuyenDi;
import com.example.mytripmatee.dao.AnhDAO;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.database.NguoiDungDAO;
import com.example.mytripmatee.databinding.FragmentKhamPhaBinding;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.model.ItemChuyenDi;
import com.example.mytripmatee.model.NguoiDung;
import com.example.mytripmatee.util.QuanLyPhien;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KhamPhaFragment extends Fragment {

    private FragmentKhamPhaBinding binding;
    private AdapterChuyenDi adapterChuyenDi;
    private ChuyenDiDAO chuyenDiDAO;
    private NguoiDungDAO nguoiDungDAO;
    private AnhDAO anhDAO;
    private QuanLyPhien quanLyPhien;

    private List<ChuyenDi> fullChuyenDiList = new ArrayList<>();
    private ChuyenDi randomFeaturedTrip = null;
    private String currentFilter = "ALL";

    // Quản lý Slideshow đổi 5 ảnh cảnh đẹp tự động sau mỗi 5 giây
    private final Handler slideshowHandler = new Handler(Looper.getMainLooper());
    private Runnable slideshowRunnable;
    private List<String> featuredPhotoUris = new ArrayList<>();
    private int currentPhotoIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentKhamPhaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chuyenDiDAO = new ChuyenDiDAO(requireContext());
        nguoiDungDAO = new NguoiDungDAO(requireContext());
        anhDAO = new AnhDAO(requireContext());
        quanLyPhien = new QuanLyPhien(requireContext());

        setupUserInfo();
        setupRecyclerView();
        setupFeaturedCard();
        setupSettingsButton();
        setupSearchAndFilter();
        loadTripsFromDatabase();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTripsFromDatabase();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopFeaturedSlideshow();
    }

    private void setupUserInfo() {
        int userId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
        if (userId <= 0) userId = 1;

        NguoiDung user = nguoiDungDAO.layTheoId(userId);
        if (user != null) {
            binding.tvUserName.setText(user.getHoTen());
        } else {
            binding.tvUserName.setText("Khách");
        }

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 11) {
            binding.tvGreeting.setText("Chào buổi sáng ✨");
        } else if (hour >= 11 && hour < 14) {
            binding.tvGreeting.setText("Chào buổi trưa ☀️");
        } else if (hour >= 14 && hour < 18) {
            binding.tvGreeting.setText("Chào buổi chiều 🌤️");
        } else {
            binding.tvGreeting.setText("Chào buổi tối 🌙");
        }
    }

    private void setupRecyclerView() {
        adapterChuyenDi = new AdapterChuyenDi(item -> {
            if (getContext() != null && item != null && item.getMaId() != null) {
                try {
                    int tripId = Integer.parseInt(item.getMaId());
                    Intent intent = ChiTietChuyenDiActivity.newIntent(
                            getContext(),
                            tripId,
                            item.getTieuDe(),
                            item.getTheNgay(),
                            0
                    );
                    startActivity(intent);
                } catch (NumberFormatException ignored) {}
            }
        });

        binding.rvTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTrips.setAdapter(adapterChuyenDi);
    }

    private void setupFeaturedCard() {
        binding.cardFeatured.setOnClickListener(v -> {
            if (getContext() != null && randomFeaturedTrip != null) {
                String dateTag = (randomFeaturedTrip.getNgayDi() != null ? randomFeaturedTrip.getNgayDi() : "") +
                        " – " + (randomFeaturedTrip.getNgayVe() != null ? randomFeaturedTrip.getNgayVe() : "");
                // Bấm vào thẻ nổi bật -> Nhảy trực tiếp vào tab Gallery (Album) của chuyến đi đó (tab 3)
                Intent intent = ChiTietChuyenDiActivity.newIntent(
                        getContext(),
                        randomFeaturedTrip.getMaChuyenDi(),
                        randomFeaturedTrip.getTenChuyenDi(),
                        dateTag,
                        3
                );
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Chưa có Album chuyến đi nào!", Toast.LENGTH_SHORT).show();
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

    private void setupSearchAndFilter() {
        if (binding.etSearch != null) {
            binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    return true;
                }
                return false;
            });

            binding.etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    if (binding.btnClearSearch != null) {
                        binding.btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    filterTrips(query);
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (binding.btnClearSearch != null) {
            binding.btnClearSearch.setOnClickListener(v -> {
                if (binding.etSearch != null) {
                    binding.etSearch.setText("");
                }
                hideKeyboard();
            });
        }

        if (binding.btnTune != null) {
            binding.btnTune.setOnClickListener(v -> {
                switch (currentFilter) {
                    case "ALL":
                        currentFilter = "UPCOMING";
                        break;
                    case "UPCOMING":
                        currentFilter = "ONGOING";
                        break;
                    case "ONGOING":
                    case "DRAFT":
                        currentFilter = "DONE";
                        break;
                    case "DONE":
                    default:
                        currentFilter = "ALL";
                        break;
                }
                updateTabUI();
                filterTrips(binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "");
            });
        }

        if (binding.tabAll != null) {
            binding.tabAll.setOnClickListener(v -> {
                currentFilter = "ALL";
                updateTabUI();
                filterTrips(binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "");
            });
        }
        if (binding.tabUpcoming != null) {
            binding.tabUpcoming.setOnClickListener(v -> {
                currentFilter = "UPCOMING";
                updateTabUI();
                filterTrips(binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "");
            });
        }
        if (binding.tabDraft != null) {
            binding.tabDraft.setOnClickListener(v -> {
                currentFilter = "ONGOING";
                updateTabUI();
                filterTrips(binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "");
            });
        }
        if (binding.tabDone != null) {
            binding.tabDone.setOnClickListener(v -> {
                currentFilter = "DONE";
                updateTabUI();
                filterTrips(binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "");
            });
        }
    }

    private void updateTabUI() {
        if (binding == null) return;

        int activeBg = R.drawable.bg_pill_teal;
        int inactiveBg = R.drawable.bg_pill_white;
        int activeTextColor = ContextCompat.getColor(requireContext(), android.R.color.white);
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        if (binding.tabAll != null) {
            binding.tabAll.setBackgroundResource("ALL".equals(currentFilter) ? activeBg : inactiveBg);
            binding.tabAll.setTextColor("ALL".equals(currentFilter) ? activeTextColor : inactiveTextColor);
        }
        if (binding.tabUpcoming != null) {
            binding.tabUpcoming.setBackgroundResource("UPCOMING".equals(currentFilter) ? activeBg : inactiveBg);
            binding.tabUpcoming.setTextColor("UPCOMING".equals(currentFilter) ? activeTextColor : inactiveTextColor);
        }
        if (binding.tabDraft != null) {
            boolean isOngoing = "ONGOING".equals(currentFilter) || "DRAFT".equals(currentFilter);
            binding.tabDraft.setBackgroundResource(isOngoing ? activeBg : inactiveBg);
            binding.tabDraft.setTextColor(isOngoing ? activeTextColor : inactiveTextColor);
        }
        if (binding.tabDone != null) {
            binding.tabDone.setBackgroundResource("DONE".equals(currentFilter) ? activeBg : inactiveBg);
            binding.tabDone.setTextColor("DONE".equals(currentFilter) ? activeTextColor : inactiveTextColor);
        }
    }

    private void hideKeyboard() {
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    private void loadTripsFromDatabase() {
        int userId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
        if (userId <= 0) userId = 1;

        fullChuyenDiList = chuyenDiDAO.layDanhSachChuyenDi(userId);

        filterTrips(binding.etSearch != null ? binding.etSearch.getText().toString().trim() : "");

        // Lấy ngẫu nhiên (random) một chuyến đi để hiển thị Album Nổi Bật
        if (fullChuyenDiList != null && !fullChuyenDiList.isEmpty()) {
            randomFeaturedTrip = fullChuyenDiList.get(new Random().nextInt(fullChuyenDiList.size()));

            binding.cardFeatured.setVisibility(View.VISIBLE);
            binding.tvFeaturedTitle.setText("Album: " + randomFeaturedTrip.getTenChuyenDi());
            if (binding.tvFeaturedSub != null) {
                String destStr = (randomFeaturedTrip.getDiemDen() != null && !randomFeaturedTrip.getDiemDen().isEmpty())
                        ? randomFeaturedTrip.getDiemDen()
                        : "Kỷ niệm cảnh đẹp";
                String sub = destStr + " • Chạm để mở album hình ảnh ›";
                binding.tvFeaturedSub.setText(sub);
            }

            startFeaturedSlideshow(randomFeaturedTrip);
        } else {
            randomFeaturedTrip = null;
            stopFeaturedSlideshow();
            binding.cardFeatured.setVisibility(View.GONE);
        }
    }

    private void startFeaturedSlideshow(ChuyenDi featured) {
        stopFeaturedSlideshow();

        if (binding == null || featured == null) return;

        // Lấy ngẫu nhiên tối đa 5 bức ảnh thuộc tag "Cảnh đẹp" của chuyến đi này
        featuredPhotoUris = anhDAO.layTopDanhSachAnhCanhDepRandom(featured.getMaChuyenDi(), 5);
        currentPhotoIndex = 0;

        ImageView imgFeaturedBg = binding.cardFeatured.findViewById(R.id.imgFeaturedBackground);
        if (imgFeaturedBg == null) return;

        if (featuredPhotoUris != null && !featuredPhotoUris.isEmpty()) {
            try {
                imgFeaturedBg.setAlpha(1.0f);
                imgFeaturedBg.setImageURI(Uri.parse(featuredPhotoUris.get(0)));
            } catch (Exception e) {
                imgFeaturedBg.setImageResource(layAnhResTheoDiemDen(featured.getDiemDen()));
            }

            // Nếu có từ 2 bức ảnh trở lên, thực hiện tự động xoay đổi ảnh sau mỗi 5 giây kèm hiệu ứng chuyển cảnh mượt mà
            if (featuredPhotoUris.size() > 1) {
                slideshowRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (binding == null || featuredPhotoUris == null || featuredPhotoUris.isEmpty()) return;

                        currentPhotoIndex = (currentPhotoIndex + 1) % featuredPhotoUris.size();
                        String nextUri = featuredPhotoUris.get(currentPhotoIndex);

                        ImageView img = binding.cardFeatured.findViewById(R.id.imgFeaturedBackground);
                        if (img != null) {
                            changeFeaturedImageWithFade(img, nextUri);
                        }

                        slideshowHandler.postDelayed(this, 5000);
                    }
                };
                slideshowHandler.postDelayed(slideshowRunnable, 5000);
            }
        } else {
            imgFeaturedBg.setAlpha(1.0f);
            imgFeaturedBg.setImageResource(layAnhResTheoDiemDen(featured.getDiemDen()));
        }
    }

    private void changeFeaturedImageWithFade(ImageView imageView, String newUri) {
        if (imageView == null || newUri == null) return;

        imageView.animate()
                .alpha(0.2f)
                .setDuration(400)
                .withEndAction(() -> {
                    if (binding == null) return;
                    try {
                        imageView.setImageURI(Uri.parse(newUri));
                    } catch (Exception ignored) {}

                    imageView.animate()
                            .alpha(1.0f)
                            .setDuration(500)
                            .start();
                })
                .start();
    }

    private void stopFeaturedSlideshow() {
        if (slideshowRunnable != null) {
            slideshowHandler.removeCallbacks(slideshowRunnable);
            slideshowRunnable = null;
        }
    }

    private int layAnhResTheoDiemDen(String destination) {
        if (destination == null) return R.drawable.img_dalat;
        String d = destination.toLowerCase();
        if (d.contains("phú quốc")) return R.drawable.img_phuquoc;
        if (d.contains("hà giang")) return R.drawable.img_hagiang;
        return R.drawable.img_dalat;
    }

    private void filterTrips(String keyword) {
        int userId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
        if (userId <= 0) userId = 1;

        List<ChuyenDi> searchResult;
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchResult = chuyenDiDAO.timKiemChuyenDi(userId, keyword.trim());
        } else {
            searchResult = fullChuyenDiList;
        }

        List<ItemChuyenDi> filteredItems = new ArrayList<>();
        for (ChuyenDi cd : searchResult) {
            boolean matchesStatus = true;
            String status = cd.getTrangThaiThucTe();

            if ("UPCOMING".equals(currentFilter)) {
                matchesStatus = "SAP_TOI".equalsIgnoreCase(status);
            } else if ("ONGOING".equals(currentFilter) || "DRAFT".equals(currentFilter)) {
                matchesStatus = "DANG_DI".equalsIgnoreCase(status);
            } else if ("DONE".equals(currentFilter)) {
                matchesStatus = "DA_XONG".equalsIgnoreCase(status);
            }

            if (matchesStatus) {
                ItemChuyenDi item = cd.toTripItem();
                String randomCanhDepUri = anhDAO.layAnhCanhDepRandomTheoChuyenDi(cd.getMaChuyenDi());
                if (randomCanhDepUri != null && !randomCanhDepUri.trim().isEmpty()) {
                    item.setDuongDanAnhUri(randomCanhDepUri);
                }
                filteredItems.add(item);
            }
        }

        if (binding != null) {
            // Ẩn/hiện thẻ nổi bật tùy theo đang tìm kiếm hay không
            if (keyword != null && !keyword.trim().isEmpty()) {
                binding.cardFeatured.setVisibility(View.GONE);
            } else {
                binding.cardFeatured.setVisibility(fullChuyenDiList.isEmpty() ? View.GONE : View.VISIBLE);
            }

            // Nếu danh sách tìm kiếm rỗng -> hiển thị tvEmptySearch
            if (binding.tvEmptySearch != null) {
                if (filteredItems.isEmpty()) {
                    binding.tvEmptySearch.setVisibility(View.VISIBLE);
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        binding.tvEmptySearch.setText("Không tìm thấy chuyến đi nào phù hợp với \"" + keyword + "\"");
                    } else {
                        binding.tvEmptySearch.setText("Không có chuyến đi nào trong danh mục này");
                    }
                    binding.rvTrips.setVisibility(View.GONE);
                } else {
                    binding.tvEmptySearch.setVisibility(View.GONE);
                    binding.rvTrips.setVisibility(View.VISIBLE);
                }
            }
        }

        if (adapterChuyenDi != null) {
            adapterChuyenDi.setDanhSachChuyenDi(filteredItems);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopFeaturedSlideshow();
        binding = null;
    }
}
