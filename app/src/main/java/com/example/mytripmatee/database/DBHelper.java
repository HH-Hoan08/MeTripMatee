package com.example.mytripmatee.database;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    public static final String DATABASE_NAME = "mytripmatee.db";
    public static final int DATABASE_VERSION = 1;

    // sử dụng Singleton đảm bảo toàn ứng dụng chỉ dùng một thể hiện duy nhất của DBHelper
    private static DBHelper instance;
    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    // cấu hình khóa ngoại
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // bắt buộc phải bật ràng buộc khóa ngoại
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Đang khởi tạo cơ sở dữ liệu SQLite");
        db.beginTransaction();
        try {
            // tạo các bảng (Tables)
            createTables(db);
            // tạo các chỉ mục (Indexes)
            createIndexes(db);
            // tạo các Triggers ràng buộc nghiệp vụ
            createTriggers(db);
            // tạo các Views phục vụ báo cáo / thống kê
            createViews(db);
            // thêm dữ liệu mặc định và dữ liệu demo
            insertInitialData(db);
            db.setTransactionSuccessful();
            Log.d(TAG, "Khởi tạo cơ sở dữ liệu thành công");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong quá trình khởi tạo cơ sở dữ liệu", e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Nâng cấp cơ sở dữ liệu từ version " + oldVersion + " lên " + newVersion);
        db.beginTransaction();
        try {
            // xóa Views
            db.execSQL("DROP VIEW IF EXISTS v_TrangThaiChuyenDi;");
            db.execSQL("DROP VIEW IF EXISTS v_ChiTieuTheoNgay;");
            db.execSQL("DROP VIEW IF EXISTS v_ChiTieuTheoDanhMuc;");
            db.execSQL("DROP VIEW IF EXISTS v_TongChiChuyenDi;");
            // xóa Triggers
            db.execSQL("DROP TRIGGER IF EXISTS trg_ChiPhi_KiemTraNgay_Them;");
            db.execSQL("DROP TRIGGER IF EXISTS trg_ChiPhi_KiemTraNgay_Sua;");
            db.execSQL("DROP TRIGGER IF EXISTS trg_LichTrinh_KiemTraNgay_Them;");
            db.execSQL("DROP TRIGGER IF EXISTS trg_LichTrinh_KiemTraNgay_Sua;");
            db.execSQL("DROP TRIGGER IF EXISTS trg_ChuyenDi_KiemTraDoiNgay_LichTrinh;");
            db.execSQL("DROP TRIGGER IF EXISTS trg_ChuyenDi_KiemTraDoiNgay_ChiPhi;");
            db.execSQL("DROP TRIGGER IF EXISTS trg_ChuyenDi_KiemTraDoiNgay;");
            db.execSQL("DROP TRIGGER IF EXISTS trg_ChuyenDi_KiemTraAnhBia;");
            db.execSQL("DROP TRIGGER IF EXISTS trg_Anh_XoaAnhBia;");
            // xóa Tables theo đúng thứ tự ràng buộc khóa ngoại
            db.execSQL("DROP TABLE IF EXISTS Anh;");
            db.execSQL("DROP TABLE IF EXISTS ChiPhi;");
            db.execSQL("DROP TABLE IF EXISTS LichTrinh;");
            db.execSQL("DROP TABLE IF EXISTS DanhMuc;");
            db.execSQL("DROP TABLE IF EXISTS ChuyenDi;");
            db.execSQL("DROP TABLE IF EXISTS NguoiDung;");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        onCreate(db);
    }
    // hàm tạo các bảng
    private void createTables(SQLiteDatabase db) {
        // bảng NguoiDung
        db.execSQL("CREATE TABLE NguoiDung (" +
                "    MaNguoiDung  INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    TenDangNhap  TEXT    NOT NULL," +
                "    MatKhau      TEXT    NOT NULL," +
                "    HoTen        TEXT," +
                "    CONSTRAINT uq_NguoiDung_TenDangNhap UNIQUE (TenDangNhap)," +
                "    CONSTRAINT ck_NguoiDung_TenDangNhap CHECK (" +
                "        LENGTH(TenDangNhap) BETWEEN 3 AND 30" +
                "        AND TenDangNhap NOT GLOB '*[^A-Za-z0-9_.]*'" +
                "    )," +
                "    CONSTRAINT ck_NguoiDung_MatKhau CHECK (LENGTH(MatKhau) >= 6)," +
                "    CONSTRAINT ck_NguoiDung_HoTen CHECK (" +
                "        HoTen IS NULL OR (LENGTH(TRIM(HoTen)) > 0 AND LENGTH(HoTen) <= 100)" +
                "    )" +
                ");");
        // bảng ChuyenDi
        db.execSQL("CREATE TABLE ChuyenDi (" +
                "    MaChuyenDi   INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    MaNguoiDung  INTEGER NOT NULL," +
                "    TenChuyenDi  TEXT    NOT NULL," +
                "    DiemDen      TEXT," +
                "    NgayDi       TEXT    NOT NULL," +
                "    NgayVe       TEXT    NOT NULL," +
                "    NganSach     INTEGER NOT NULL," +
                "    TrangThai    TEXT    NOT NULL DEFAULT 'SAP_TOI'," +
                "    AnhBia       TEXT," +
                "    CONSTRAINT fk_ChuyenDi_NguoiDung FOREIGN KEY (MaNguoiDung)" +
                "        REFERENCES NguoiDung (MaNguoiDung) ON DELETE CASCADE ON UPDATE CASCADE," +
                "    CONSTRAINT uq_ChuyenDi_KhongTrung UNIQUE (MaNguoiDung, TenChuyenDi, NgayDi)," +
                "    CONSTRAINT ck_ChuyenDi_TenChuyenDi CHECK (" +
                "        LENGTH(TRIM(TenChuyenDi)) > 0 AND LENGTH(TenChuyenDi) <= 100" +
                "    )," +
                "    CONSTRAINT ck_ChuyenDi_DiemDen CHECK (" +
                "        DiemDen IS NULL OR LENGTH(DiemDen) <= 100" +
                "    )," +
                "    CONSTRAINT ck_ChuyenDi_NgayDi CHECK (DATE(NgayDi) IS NOT NULL AND DATE(NgayDi) = NgayDi)," +
                "    CONSTRAINT ck_ChuyenDi_NgayVe CHECK (DATE(NgayVe) IS NOT NULL AND DATE(NgayVe) = NgayVe)," +
                "    CONSTRAINT ck_ChuyenDi_NgayVeSauNgayDi CHECK (NgayVe >= NgayDi)," +
                "    CONSTRAINT ck_ChuyenDi_NganSach CHECK (NganSach > 0 AND NganSach <= 100000000000)," +
                "    CONSTRAINT ck_ChuyenDi_TrangThai CHECK (TrangThai IN ('SAP_TOI', 'DANG_DI', 'DA_XONG'))" +
                ");");
        // bảng LichTrinh
        db.execSQL("CREATE TABLE LichTrinh (" +
                "    MaHoatDong    INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    MaChuyenDi    INTEGER NOT NULL," +
                "    NgayHoatDong  TEXT    NOT NULL," +
                "    GioBatDau     TEXT," +
                "    TieuDe        TEXT    NOT NULL," +
                "    DiaDiem       TEXT," +
                "    LinkBanDo     TEXT," +
                "    LoaiHoatDong  TEXT    NOT NULL DEFAULT 'THAM_QUAN'," +
                "    GhiChu        TEXT," +
                "    DaDi          INTEGER NOT NULL DEFAULT 0," +
                "    CONSTRAINT fk_LichTrinh_ChuyenDi FOREIGN KEY (MaChuyenDi)" +
                "        REFERENCES ChuyenDi (MaChuyenDi) ON DELETE CASCADE ON UPDATE CASCADE," +
                "    CONSTRAINT ck_LichTrinh_NgayHoatDong CHECK (" +
                "        DATE(NgayHoatDong) IS NOT NULL AND DATE(NgayHoatDong) = NgayHoatDong" +
                "    )," +
                "    CONSTRAINT ck_LichTrinh_GioBatDau CHECK (" +
                "        GioBatDau IS NULL OR (" +
                "            GioBatDau GLOB '[0-2][0-9]:[0-5][0-9]' AND TIME(GioBatDau) IS NOT NULL" +
                "        )" +
                "    )," +
                "    CONSTRAINT ck_LichTrinh_TieuDe CHECK (" +
                "        LENGTH(TRIM(TieuDe)) > 0 AND LENGTH(TieuDe) <= 150" +
                "    )," +
                "    CONSTRAINT ck_LichTrinh_DiaDiem CHECK (DiaDiem IS NULL OR LENGTH(DiaDiem) <= 200)," +
                "    CONSTRAINT ck_LichTrinh_LinkBanDo CHECK (" +
                "        LinkBanDo IS NULL" +
                "        OR LinkBanDo = ''" +
                "        OR (" +
                "            (LinkBanDo LIKE 'http://%' OR LinkBanDo LIKE 'https://%')" +
                "            AND LinkBanDo NOT LIKE '% %'" +
                "            AND LENGTH(LinkBanDo) <= 2048" +
                "        )" +
                "    )," +
                "    CONSTRAINT ck_LichTrinh_LoaiHoatDong CHECK (" +
                "        LoaiHoatDong IN ('DI_CHUYEN', 'AN_UONG', 'THAM_QUAN', 'LUU_TRU')" +
                "    )," +
                "    CONSTRAINT ck_LichTrinh_GhiChu CHECK (GhiChu IS NULL OR LENGTH(GhiChu) <= 1000)," +
                "    CONSTRAINT ck_LichTrinh_DaDi CHECK (DaDi IN (0, 1))" +
                ");");
        // bảng DanhMuc
        db.execSQL("CREATE TABLE DanhMuc (" +
                "    MaDanhMuc   INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    TenDanhMuc  TEXT NOT NULL COLLATE NOCASE," +
                "    MauSac      TEXT," +
                "    CONSTRAINT uq_DanhMuc_TenDanhMuc UNIQUE (TenDanhMuc)," +
                "    CONSTRAINT ck_DanhMuc_TenDanhMuc CHECK (" +
                "        LENGTH(TRIM(TenDanhMuc)) > 0 AND LENGTH(TenDanhMuc) <= 50" +
                "    )," +
                "    CONSTRAINT ck_DanhMuc_MauSac CHECK (" +
                "        MauSac IS NULL OR MauSac GLOB" +
                "        '#[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]'" +
                "    )" +
                ");");
        // bảng ChiPhi
        db.execSQL("CREATE TABLE ChiPhi (" +
                "    MaChiPhi    INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    MaChuyenDi  INTEGER NOT NULL," +
                "    MaDanhMuc   INTEGER NOT NULL," +
                "    SoTien      INTEGER NOT NULL," +
                "    NgayChi     TEXT    NOT NULL," +
                "    GhiChu      TEXT," +
                "    CONSTRAINT fk_ChiPhi_ChuyenDi FOREIGN KEY (MaChuyenDi)" +
                "        REFERENCES ChuyenDi (MaChuyenDi) ON DELETE CASCADE ON UPDATE CASCADE," +
                "    CONSTRAINT fk_ChiPhi_DanhMuc FOREIGN KEY (MaDanhMuc)" +
                "        REFERENCES DanhMuc (MaDanhMuc) ON DELETE RESTRICT ON UPDATE CASCADE," +
                "    CONSTRAINT ck_ChiPhi_SoTien CHECK (SoTien > 0 AND SoTien <= 1000000000000)," +
                "    CONSTRAINT ck_ChiPhi_NgayChi CHECK (DATE(NgayChi) IS NOT NULL AND DATE(NgayChi) = NgayChi)," +
                "    CONSTRAINT ck_ChiPhi_GhiChu CHECK (GhiChu IS NULL OR LENGTH(GhiChu) <= 500)" +
                ");");
        // bảng Anh
        db.execSQL("CREATE TABLE Anh (" +
                "    MaAnh        INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    MaChuyenDi   INTEGER NOT NULL," +
                "    DuongDanAnh  TEXT    NOT NULL," +
                "    ChuThich     TEXT," +
                "    CONSTRAINT fk_Anh_ChuyenDi FOREIGN KEY (MaChuyenDi)" +
                "        REFERENCES ChuyenDi (MaChuyenDi) ON DELETE CASCADE ON UPDATE CASCADE," +
                "    CONSTRAINT uq_Anh_KhongTrung UNIQUE (MaChuyenDi, DuongDanAnh)," +
                "    CONSTRAINT ck_Anh_DuongDanAnh CHECK (LENGTH(TRIM(DuongDanAnh)) > 0)," +
                "    CONSTRAINT ck_Anh_ChuThich CHECK (ChuThich IS NULL OR LENGTH(ChuThich) <= 200)" +
                ");");
    }
    // hàm tạo các chỉ mục
    private void createIndexes(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX idx_ChuyenDi_NguoiDung ON ChuyenDi (MaNguoiDung, NgayDi);");
        db.execSQL("CREATE INDEX idx_ChuyenDi_TrangThai ON ChuyenDi (MaNguoiDung, TrangThai);");
        db.execSQL("CREATE INDEX idx_LichTrinh_ChuyenDi ON LichTrinh (MaChuyenDi, NgayHoatDong, GioBatDau);");
        db.execSQL("CREATE INDEX idx_ChiPhi_ChuyenDi    ON ChiPhi (MaChuyenDi, NgayChi);");
        db.execSQL("CREATE INDEX idx_ChiPhi_DanhMuc     ON ChiPhi (MaDanhMuc);");
        db.execSQL("CREATE INDEX idx_Anh_ChuyenDi       ON Anh (MaChuyenDi);");
    }
    // hàm tạo trigger
    private void createTriggers(SQLiteDatabase db) {
        db.execSQL("CREATE TRIGGER trg_LichTrinh_KiemTraNgay_Them " +
                "BEFORE INSERT ON LichTrinh " +
                "BEGIN " +
                "    SELECT RAISE(ABORT, 'Ngày hoạt động phải nằm trong khoảng ngày đi - ngày về của chuyến đi') " +
                "    WHERE NOT EXISTS ( " +
                "        SELECT * FROM ChuyenDi " +
                "        WHERE MaChuyenDi = NEW.MaChuyenDi " +
                "          AND NEW.NgayHoatDong BETWEEN NgayDi AND NgayVe " +
                "    ); " +
                "END;");
        db.execSQL("CREATE TRIGGER trg_LichTrinh_KiemTraNgay_Sua " +
                "BEFORE UPDATE OF NgayHoatDong, MaChuyenDi ON LichTrinh " +
                "BEGIN " +
                "    SELECT RAISE(ABORT, 'Ngày hoạt động phải nằm trong khoảng ngày đi - ngày về của chuyến đi') " +
                "    WHERE NOT EXISTS ( " +
                "        SELECT * FROM ChuyenDi " +
                "        WHERE MaChuyenDi = NEW.MaChuyenDi " +
                "          AND NEW.NgayHoatDong BETWEEN NgayDi AND NgayVe " +
                "    ); " +
                "END;");
        db.execSQL("CREATE TRIGGER trg_ChiPhi_KiemTraNgay_Them " +
                "BEFORE INSERT ON ChiPhi " +
                "BEGIN " +
                "    SELECT RAISE(ABORT, 'Ngày chi phải nằm trong khoảng ngày đi - ngày về của chuyến đi') " +
                "    WHERE NOT EXISTS ( " +
                "        SELECT * FROM ChuyenDi " +
                "        WHERE MaChuyenDi = NEW.MaChuyenDi " +
                "          AND NEW.NgayChi BETWEEN NgayDi AND NgayVe " +
                "    ); " +
                "END;");
        db.execSQL("CREATE TRIGGER trg_ChiPhi_KiemTraNgay_Sua " +
                "BEFORE UPDATE OF NgayChi, MaChuyenDi ON ChiPhi " +
                "BEGIN " +
                "    SELECT RAISE(ABORT, 'Ngày chi phải nằm trong khoảng ngày đi - ngày về của chuyến đi') " +
                "    WHERE NOT EXISTS ( " +
                "        SELECT * FROM ChuyenDi " +
                "        WHERE MaChuyenDi = NEW.MaChuyenDi " +
                "          AND NEW.NgayChi BETWEEN NgayDi AND NgayVe " +
                "    ); " +
                "END;");
        db.execSQL("CREATE TRIGGER trg_ChuyenDi_KiemTraDoiNgay_LichTrinh " +
                "BEFORE UPDATE OF NgayDi, NgayVe ON ChuyenDi " +
                "BEGIN " +
                "    SELECT RAISE(ABORT, 'Còn hoạt động lịch trình nằm ngoài khoảng ngày mới của chuyến đi') " +
                "    WHERE EXISTS ( " +
                "        SELECT * FROM LichTrinh " +
                "        WHERE MaChuyenDi = OLD.MaChuyenDi " +
                "          AND NgayHoatDong NOT BETWEEN NEW.NgayDi AND NEW.NgayVe " +
                "    ); " +
                "END;");

        db.execSQL("CREATE TRIGGER trg_ChuyenDi_KiemTraDoiNgay_ChiPhi " +
                "BEFORE UPDATE OF NgayDi, NgayVe ON ChuyenDi " +
                "BEGIN " +
                "    SELECT RAISE(ABORT, 'Còn khoản chi tiêu nằm ngoài khoảng ngày mới của chuyến đi') " +
                "    WHERE EXISTS ( " +
                "        SELECT * FROM ChiPhi " +
                "        WHERE MaChuyenDi = OLD.MaChuyenDi " +
                "          AND NgayChi NOT BETWEEN NEW.NgayDi AND NEW.NgayVe " +
                "    ); " +
                "END;");
        db.execSQL("CREATE TRIGGER trg_ChuyenDi_KiemTraAnhBia " +
                "BEFORE UPDATE OF AnhBia ON ChuyenDi " +
                "WHEN NEW.AnhBia IS NOT NULL " +
                "BEGIN " +
                "    SELECT RAISE(ABORT, 'Ảnh bìa phải là một ảnh thuộc thư viện ảnh của chuyến đi') " +
                "    WHERE NOT EXISTS ( " +
                "        SELECT * FROM Anh " +
                "        WHERE MaChuyenDi = NEW.MaChuyenDi AND DuongDanAnh = NEW.AnhBia " +
                "    ); " +
                "END;");
        db.execSQL("CREATE TRIGGER trg_Anh_XoaAnhBia " +
                "AFTER DELETE ON Anh " +
                "BEGIN " +
                "    UPDATE ChuyenDi SET AnhBia = NULL " +
                "    WHERE MaChuyenDi = OLD.MaChuyenDi AND AnhBia = OLD.DuongDanAnh; " +
                "END;");
    }
    // hàm tạo view
    private void createViews(SQLiteDatabase db) {
        db.execSQL("CREATE VIEW v_TongChiChuyenDi AS " +
                "SELECT  c.MaChuyenDi, " +
                "        c.MaNguoiDung, " +
                "        c.TenChuyenDi, " +
                "        c.NganSach, " +
                "        COALESCE(SUM(p.SoTien), 0)                              AS TongChi, " +
                "        c.NganSach - COALESCE(SUM(p.SoTien), 0)                 AS ConLai, " +
                "        ROUND(100.0 * COALESCE(SUM(p.SoTien), 0) / c.NganSach, 1) AS TyLeSuDung, " +
                "        CASE " +
                "            WHEN COALESCE(SUM(p.SoTien), 0) > c.NganSach            THEN 'VUOT_NGAN_SACH' " +
                "            WHEN COALESCE(SUM(p.SoTien), 0) * 100 >= c.NganSach * 80 THEN 'CANH_BAO' " +
                "            ELSE 'BINH_THUONG' " +
                "        END                                                     AS MucCanhBao " +
                "FROM    ChuyenDi c " +
                "LEFT JOIN ChiPhi p ON p.MaChuyenDi = c.MaChuyenDi " +
                "GROUP BY c.MaChuyenDi;");
        db.execSQL("CREATE VIEW v_ChiTieuTheoDanhMuc AS " +
                "SELECT  p.MaChuyenDi, " +
                "        d.MaDanhMuc, " +
                "        d.TenDanhMuc, " +
                "        d.MauSac, " +
                "        SUM(p.SoTien) AS TongTien " +
                "FROM    ChiPhi p " +
                "JOIN    DanhMuc d ON d.MaDanhMuc = p.MaDanhMuc " +
                "GROUP BY p.MaChuyenDi, d.MaDanhMuc, d.TenDanhMuc, d.MauSac;");
        db.execSQL("CREATE VIEW v_ChiTieuTheoNgay AS " +
                "SELECT  MaChuyenDi, NgayChi, SUM(SoTien) AS TongTien " +
                "FROM    ChiPhi " +
                "GROUP BY MaChuyenDi, NgayChi;");
        db.execSQL("CREATE VIEW v_TrangThaiChuyenDi AS " +
                "SELECT  MaChuyenDi, " +
                "        TenChuyenDi, " +
                "        CASE " +
                "            WHEN DATE('now', 'localtime') < NgayDi THEN 'SAP_TOI' " +
                "            WHEN DATE('now', 'localtime') > NgayVe THEN 'DA_XONG' " +
                "            ELSE 'DANG_DI' " +
                "        END AS TrangThaiThucTe " +
                "FROM    ChuyenDi;");
    }
    // thêm dữ liệu
    private void insertInitialData(SQLiteDatabase db) {
        // dữ liệu danh mục mặc định
        db.execSQL("INSERT INTO DanhMuc (TenDanhMuc, MauSac) VALUES " +
                "('Ăn uống',   '#FF7043'), " +
                "('Di chuyển', '#42A5F5'), " +
                "('Lưu trú',   '#7E57C2'), " +
                "('Tham quan', '#26A69A'), " +
                "('Mua sắm',   '#EC407A'), " +
                "('Khác',      '#78909C');");
        // tài khoản demo: demo / 123456 (mật khẩu đã băm SHA-256)
        db.execSQL("INSERT INTO NguoiDung (TenDangNhap, MatKhau, HoTen) VALUES " +
                "('demo', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Tài khoản demo');");
        // chuyến đi mẫu Đà Lạt 3 ngày
        db.execSQL("INSERT INTO ChuyenDi (MaNguoiDung, TenChuyenDi, DiemDen, NgayDi, NgayVe, NganSach, TrangThai) " +
                "VALUES (1, 'Đà Lạt 3 ngày', 'Đà Lạt', '2026-12-01', '2026-12-03', 3000000, 'SAP_TOI');");
        // lịch trình
        db.execSQL("INSERT INTO LichTrinh (MaChuyenDi, NgayHoatDong, GioBatDau, TieuDe, DiaDiem, LinkBanDo, LoaiHoatDong, GhiChu, DaDi) VALUES " +
                "(1, '2026-12-01', '06:30', 'Xe giường nằm TP.HCM - Đà Lạt', 'Bến xe Miền Đông', NULL,                             'DI_CHUYEN', NULL, 0), " +
                "(1, '2026-12-01', '12:00', 'Ăn trưa lẩu gà lá é',            'Đà Lạt',           'https://maps.app.goo.gl/example', 'AN_UONG',  'Đặt bàn trước', 0), " +
                "(1, '2026-12-01', '14:00', 'Nhận phòng homestay',            'Đà Lạt',           NULL,                             'LUU_TRU',  NULL, 0), " +
                "(1, '2026-12-02', '08:00', 'Tham quan Thung lũng Tình Yêu',  'Đà Lạt',           NULL,                             'THAM_QUAN', NULL, 0), " +
                "(1, '2026-12-03', '09:00', 'Chợ Đà Lạt mua quà',             'Chợ Đà Lạt',       NULL,                             'THAM_QUAN', NULL, 0);");
        // chi phí
        db.execSQL("INSERT INTO ChiPhi (MaChuyenDi, MaDanhMuc, SoTien, NgayChi, GhiChu) VALUES " +
                "(1, 2, 400000, '2026-12-01', 'Vé xe khứ hồi'), " +
                "(1, 1, 250000, '2026-12-01', 'Lẩu gà lá é'), " +
                "(1, 3, 900000, '2026-12-01', 'Homestay 2 đêm'), " +
                "(1, 4, 180000, '2026-12-02', 'Vé vào cổng'), " +
                "(1, 5, 350000, '2026-12-03', 'Đặc sản làm quà');");
    }
}
