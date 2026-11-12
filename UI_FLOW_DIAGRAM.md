```text
🔑 LUỒNG 1: XÁC THỰC & ĐĂNG NHẬP (AUTH FLOW)

1. Màn hình Đăng nhập (LoginActivity.java)
   ├── 📄 Layout chính : activity_login.xml
   ├── 🔘 Nút "Đăng ký mới" (btnNavRegister) ──> 🌐 RegisterActivity.java
   └── 🔘 Nút "Đăng nhập ngay" (btnLogin)    ──> 🌐 MainActivity.java (Màn hình chính)

2. Màn hình Đăng ký (RegisterActivity.java)
   ├── 📄 Layout chính : activity_register.xml
   └── 🔘 Nút "Đăng nhập" (btnNavLogin)      ──> Quay lại LoginActivity.java

🏠 LUỒNG 2: MÀN HÌNH CHÍNH (MAIN NAVIGATION FLOW)

🌐 Màn hình chính (MainActivity.java)
   └── 📄 Layout chính : activity_main.xml (ViewPager2 + Bottom Nav 4 Tab + Nút vòm + ở giữa)

   ├── 📌 TAB 1: Khám phá (ExploreFragment.java)
   │      ├── 📄 Layout Fragment : fragment_explore.xml
   │      ├── 🧩 Layout Dòng thẻ : item_trip.xml (Hiển thị thẻ danh sách chuyến đi)
   │      ├── 🔘 Nút bánh răng (btnSettings) ──> 🌐 SettingsActivity.java
   │      └── 🔘 Click vào 1 chuyến đi        ──> 🌐 TripDetailActivity.java (Xem Luồng 4)
   │
   ├── 📌 TAB 2: Lịch trình chung (ItineraryFragment.java)
   │      ├── 📄 Layout Fragment : fragment_itinerary.xml
   │      ├── 🧩 Layout Dòng mốc  : item_itinerary.xml (Hiển thị timeline giờ & hoạt động)
   │      ├── 🔘 Nút "+ Thêm hoạt động"        ──> 🌐 EditItineraryActivity.java
   │      └── 🔘 Click 1 mốc hoạt động có sẵn ──> 🌐 EditItineraryActivity.java (Chỉnh sửa)
   │
   ├── 📌 NÚT VÒM TRÒN (+) Ở GIỮA THANH BOTTOM NAV (btnAddTrip)
   │      └── 🔘 Click Nút (+) ──> 🌐 EditTripActivity.java (Mở trang Tạo chuyến đi mới)
   │
   ├── 📌 TAB 3: Bản đồ (MapFragment.java)
   │      └── 📄 Layout Fragment : fragment_map.xml (Hiển thị bản đồ địa điểm)
   │
   └── 📌 TAB 4: Ví chi tiêu tổng quan (ExpenseFragment.java)
          ├── 📄 Layout Fragment : fragment_expense.xml
          ├── 🧩 Layout Dòng chi : item_expense.xml (Hiển thị dòng số tiền & icon danh mục)
          └── 🔘 Nút "+ Thêm chi phí" ──> 🪟 dialog_expense.xml (Hộp thoại trồi lên nhập tiền)

📅 LUỒNG 3: QUẢN LÝ CHUYẾN ĐI & CHỌN LỊCH (TRIP CREATION & DATE PICKER FLOW)

1. Màn hình Tạo / Chỉnh sửa Chuyến đi (EditTripActivity.java)
   ├── 📄 Layout chính : activity_trip_edit.xml
   └── 🔘 Click Thẻ thời gian (cvTime) ──> 🌐 CustomDatePickerActivity.java

2. Màn hình Bộ lịch chọn khoảng ngày (CustomDatePickerActivity.java)
   ├── 📄 Layout chính : activity_date_picker.xml
   ├── 🧩 Layout Ô số ngày trên lịch   : item_calendar_day.xml (Ô chọn ngày 1 đến 31)
   ├── 🔘 Các nút Preset nhanh (3N2Đ, Cuối tuần) ──> Tự chọn dải ngày nhanh
   └── 🔘 Nút "Xác nhận ✓" (btnApplyDate)          ──> Trả dải ngày về EditTripActivity & Đóng

🧳 LUỒNG 4: CHI TIẾT 1 CHUYẾN ĐI (TRIP DETAIL FLOW)

🌐 Màn hình Chi tiết chuyến đi (TripDetailActivity.java)
   └── 📄 Layout chính : activity_trip_detail.xml (ViewPager2 + Thanh 4 Tab ngang phía trên)

   ├── 📌 TAB 1: Lịch trình chi tiết (ItineraryFragment.java)
   │      ├── 📄 Layout Fragment : fragment_itinerary.xml (Dùng chung layout với Tab 2 Màn chính)
   │      ├── 🧩 Layout Dòng mốc  : item_itinerary.xml
   │      └── 🔘 Nút "+ Thêm hoạt động" ──> 🌐 EditItineraryActivity.java (activity_itinerary_edit.xml)
   │
   ├── 📌 TAB 2: Chi phí chuyến đi này (TripExpenseFragment.java)
   │      ├── 📄 Layout Fragment : fragment_trip_expense.xml (Chi phí riêng cho 1 chuyến)
   │      ├── 🧩 Layout Dòng chi : item_expense.xml (Tái sử dụng layout dòng chi phí)
   │      └── 🔘 Nút "+ Thêm chi phí" ──> 🪟 dialog_expense.xml (Hộp thoại trồi lên nhập tiền)
   │
   ├── 📌 TAB 3: Thống kê chuyến đi (TripStatsFragment.java)
   │      └── 📄 Layout Fragment : fragment_trip_stats.xml (Biểu đồ & 4 thẻ thống kê)
   │
   └── 📌 TAB 4: Album ảnh chuyến đi (TripGalleryFragment.java)
          ├── 📄 Layout Fragment : fragment_trip_gallery.xml
          ├── 🧩 Layout Ô ảnh vuông : item_gallery_photo.xml (Ảnh nhỏ lưới 3 cột)
          └── 🔘 Click vào 1 ảnh bất kỳ ──> 🪟 dialog_photo_preview.xml (Hộp thoại xem phóng to ảnh)

⚙️ LUỒNG 5: MÀN HÌNH CÀI ĐẶT (SETTINGS FLOW)

🌐 Màn hình Cài đặt (SettingsActivity.java)
   ├── 📄 Layout chính : activity_settings.xml
   ├── 🔘 Switch Chế độ tối (switchDarkMode) ──> Kích hoạt hiệu ứng quét mượt Sáng / Tối từ trái qua phải
   ├── 🔘 Nút "Tạo dữ liệu mẫu" (btnCreateSampleData) ──> Bật MaterialAlertDialog tạo dữ liệu test
   ├── 🔘 Nút "Sao lưu Local" & "Phục hồi"              ──> Lưu/Khôi phục dữ liệu trên máy
   └── 🔘 Nút "Đăng xuất tài khoản" (btnLogout)          ──> Đóng tất cả màn hình ──> Quay về LoginActivity
```

### 💡 Bảng tóm tắt phân loại file Layout XML

| Loại XML | Vai trò trong App | Cách thức hiển thị |
| :--- | :--- | :--- |
| **`activity_*.xml`** | Màn hình chính | Bật mở chiếm toàn bộ màn hình điện thoại. |
| **`fragment_*.xml`** | Màn hình con (Tab) | Nằm gọn bên trong `Activity` (như các Tab Khám phá, Lịch trình, Ví chi tiêu). |
| **`dialog_*.xml`** | Hộp thoại | Trồi lên / Hiện đè lên trên màn hình hiện tại (nhập dữ liệu hoặc xem ảnh). |
| **`item_*.xml`** | Ô / Dòng mẫu | Thiết kế cho 1 dòng đơn lẻ, được nạp vào Danh sách (`RecyclerView`) để lặp đi lặp lại nhiều lần. |
