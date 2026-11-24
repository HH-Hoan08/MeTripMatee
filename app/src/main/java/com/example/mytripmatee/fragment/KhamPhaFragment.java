package com.example.mytripmatee.fragment;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.R;
import com.example.mytripmatee.activity.CaiDatActivity;
import com.example.mytripmatee.activity.ChiTietChuyenDiActivity;
import com.example.mytripmatee.adapter.AdapterChuyenDi;
import com.example.mytripmatee.adapter.AdapterThongBao;
import com.example.mytripmatee.dao.AnhDAO;
import com.example.mytripmatee.dao.ThongBaoDAO;
import com.example.mytripmatee.database.ChuyenDiDAO;
import com.example.mytripmatee.database.NguoiDungDAO;
import com.example.mytripmatee.databinding.FragmentKhamPhaBinding;
import com.example.mytripmatee.model.ChuyenDi;
import com.example.mytripmatee.model.ItemChuyenDi;
import com.example.mytripmatee.model.NguoiDung;
import com.example.mytripmatee.model.ThongBao;
import com.example.mytripmatee.util.QuanLyPhien;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
        setupNotificationButton();
        setupSearchAndFilter();
        loadTripsFromDatabase();
        capNhatThongBaoVaChuong();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTripsFromDatabase();
        capNhatThongBaoVaChuong();
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

    private ObjectAnimator bellAnimator;

    private void setupNotificationButton() {
        binding.btnNotification.setOnClickListener(v -> hienThiDialogThongBao());
    }

    private void capNhatThongBaoVaChuong() {
        if (getContext() == null || binding == null) return;

        int currentUserId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
        if (currentUserId <= 0) currentUserId = 1;

        ThongBaoDAO thongBaoDAO = new ThongBaoDAO(requireContext());
        int unreadCount = thongBaoDAO.laySoThongBaoChuaDoc(currentUserId);
        int highestNotifId = thongBaoDAO.layMaThongBaoMoiNhat(currentUserId);

        SharedPreferences sp = requireContext().getSharedPreferences("notif_pref", Context.MODE_PRIVATE);
        int lastSeenId = sp.getInt("last_seen_notif_id_" + currentUserId, 0);

        boolean hasNewNotif = (highestNotifId > 0 && highestNotifId > lastSeenId) || (unreadCount > 0 && highestNotifId > lastSeenId);

        if (hasNewNotif) {
            binding.vNotificationBadge.setVisibility(View.VISIBLE);
            batDauLacChuong();
        } else {
            binding.vNotificationBadge.setVisibility(View.GONE);
            tatLacChuong();
        }
    }

    private void batDauLacChuong() {
        if (binding.imgNotificationBell == null) return;
        if (bellAnimator == null) {
            bellAnimator = ObjectAnimator.ofFloat(binding.imgNotificationBell, "rotation", 0f, -15f, 15f, -10f, 10f, -5f, 5f, 0f);
            bellAnimator.setDuration(1200);
            bellAnimator.setRepeatCount(ValueAnimator.INFINITE);
            bellAnimator.setRepeatMode(ValueAnimator.RESTART);
        }
        if (!bellAnimator.isRunning()) {
            bellAnimator.start();
        }
    }

    private void tatLacChuong() {
        if (bellAnimator != null && bellAnimator.isRunning()) {
            bellAnimator.cancel();
        }
        if (binding.imgNotificationBell != null) {
            binding.imgNotificationBell.setRotation(0f);
        }
    }

    private void hienThiDialogThongBao() {
        if (getContext() == null) return;

        int currentUserId = quanLyPhien != null ? quanLyPhien.getMaNguoiDung() : 1;
        if (currentUserId <= 0) currentUserId = 1;
        final int finalUserId = currentUserId;

        ThongBaoDAO thongBaoDAO = new ThongBaoDAO(requireContext());

        // Đánh dấu đã xem danh sách thông báo khi người dùng bấm vào xem
        int highestNotifId = thongBaoDAO.layMaThongBaoMoiNhat(finalUserId);
        if (highestNotifId > 0) {
            SharedPreferences sp = requireContext().getSharedPreferences("notif_pref", Context.MODE_PRIVATE);
            sp.edit().putInt("last_seen_notif_id_" + finalUserId, highestNotifId).apply();
        }
        thongBaoDAO.danhDauDaDocThongBaoThongTin(finalUserId);

        capNhatThongBaoVaChuong(); // Dừng lắc chuông & ẩn chấm đỏ ngay khi vào xem

        List<ThongBao> listNotif = thongBaoDAO.layDanhSachThongBaoTheoNguoiDung(finalUserId);

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_notifications, null);
        dialog.setContentView(dialogView);

        ImageView btnClose = dialogView.findViewById(R.id.btnCloseDialog);
        RecyclerView rv = dialogView.findViewById(R.id.rvNotifications);
        TextView tvEmpty = dialogView.findViewById(R.id.tvEmptyNotif);

        if (btnClose != null) btnClose.setOnClickListener(dv -> dialog.dismiss());

        final AdapterThongBao[] adapterHolder = new AdapterThongBao[1];
        AdapterThongBao adapter = new AdapterThongBao(new AdapterThongBao.OnThongBaoActionListener() {
            @Override
            public void onAccept(ThongBao thongBao, int position) {
                thongBaoDAO.capNhatTrangThaiThongBao(thongBao.getMaThongBao(), 1);
                chuyenDiDAO.themThanhVienChuyenDi(thongBao.getMaChuyenDi(), finalUserId);

                Toast.makeText(requireContext(), "Đã chấp nhận lời mời tham gia chuyến đi!", Toast.LENGTH_SHORT).show();
                loadTripsFromDatabase();
                capNhatThongBaoVaChuong();

                List<ThongBao> updatedList = thongBaoDAO.layDanhSachThongBaoTheoNguoiDung(finalUserId);
                if (adapterHolder[0] != null) {
                    adapterHolder[0].setDanhSachThongBao(updatedList);
                }
            }

            @Override
            public void onDecline(ThongBao thongBao, int position) {
                thongBaoDAO.capNhatTrangThaiThongBao(thongBao.getMaThongBao(), 2);
                chuyenDiDAO.xoaThanhVienChuyenDi(thongBao.getMaChuyenDi(), finalUserId);

                Toast.makeText(requireContext(), "Đã từ chối lời mời tham gia chuyến đi", Toast.LENGTH_SHORT).show();
                loadTripsFromDatabase();
                capNhatThongBaoVaChuong();

                List<ThongBao> updatedList = thongBaoDAO.layDanhSachThongBaoTheoNguoiDung(finalUserId);
                if (adapterHolder[0] != null) {
                    adapterHolder[0].setDanhSachThongBao(updatedList);
                }
            }
        });
        adapterHolder[0] = adapter;

        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));
            rv.setAdapter(adapter);
            adapter.setDanhSachThongBao(listNotif);
        }

        if (listNotif == null || listNotif.isEmpty()) {
            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
            if (rv != null) rv.setVisibility(View.GONE);
        } else {
            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
            if (rv != null) rv.setVisibility(View.VISIBLE);
        }

        dialog.show();
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
        if (featured.getAnhBia() != null && !featured.getAnhBia().trim().isEmpty()) {
            String ab = featured.getAnhBia().trim();
            if (!featuredPhotoUris.contains(ab)) {
                featuredPhotoUris.add(0, ab);
            }
        }
        currentPhotoIndex = 0;

        ImageView imgFeaturedBg = binding.cardFeatured.findViewById(R.id.imgFeaturedBackground);
        if (imgFeaturedBg == null) return;

        if (featuredPhotoUris != null && !featuredPhotoUris.isEmpty()) {
            imgFeaturedBg.setAlpha(1.0f);
            hienThiAnhLenImageView(imgFeaturedBg, featuredPhotoUris.get(0), featured.getDiemDen());

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
                            changeFeaturedImageWithFade(img, nextUri, featured.getDiemDen());
                        }

                        slideshowHandler.postDelayed(this, 5000);
                    }
                };
                slideshowHandler.postDelayed(slideshowRunnable, 5000);
            }
        } else {
            imgFeaturedBg.setAlpha(1.0f);
            hienThiAnhLenImageView(imgFeaturedBg, null, featured.getDiemDen());
        }
    }

    private void changeFeaturedImageWithFade(ImageView imageView, String newUri, String diemDenFallback) {
        if (imageView == null) return;

        imageView.animate()
                .alpha(0.2f)
                .setDuration(400)
                .withEndAction(() -> {
                    if (binding == null) return;
                    hienThiAnhLenImageView(imageView, newUri, diemDenFallback);

                    imageView.animate()
                            .alpha(1.0f)
                            .setDuration(500)
                            .start();
                })
                .start();
    }

    private void hienThiAnhLenImageView(ImageView imageView, String uriOrName, String diemDenFallback) {
        if (imageView == null) return;
        Context context = imageView.getContext();
        if (context == null) return;

        if (uriOrName != null && !uriOrName.trim().isEmpty()) {
            String clean = uriOrName.trim();
            int resId = context.getResources().getIdentifier(clean, "drawable", context.getPackageName());
            if (resId != 0) {
                imageView.setImageResource(resId);
                return;
            }
            try {
                imageView.setImageURI(Uri.parse(clean));
                return;
            } catch (Exception ignored) {}
        }

        imageView.setImageResource(layAnhResTheoDiemDen(diemDenFallback));
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
        if (d.contains("phú quốc") || d.contains("phu quoc")) return R.drawable.img_phuquoc;
        if (d.contains("hà giang") || d.contains("ha giang")) return R.drawable.img_hagiang;
        if (d.contains("nha trang")) return R.drawable.img_nhatrang;
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
                if (item.getDuongDanAnhUri() == null || item.getDuongDanAnhUri().trim().isEmpty()) {
                    String randomCanhDepUri = anhDAO.layAnhCanhDepRandomTheoChuyenDi(cd.getMaChuyenDi());
                    if (randomCanhDepUri != null && !randomCanhDepUri.trim().isEmpty()) {
                        item.setDuongDanAnhUri(randomCanhDepUri);
                    }
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
