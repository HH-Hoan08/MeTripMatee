package com.example.mytripmatee.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mytripmatee.model.ChuyenDi;

import java.util.ArrayList;
import java.util.List;

public class ChuyenDiDAO {
    private static final String TAG = "ChuyenDiDAO";
    private final DBHelper dbHelper;

    public ChuyenDiDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }
    // thêm chuyến đi mới kèm danh sách thành viên
    public long themChuyenDi(ChuyenDi chuyenDi, List<Integer> danhSachIdThanhVien) {
        if (chuyenDi == null) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MaNguoiDung", chuyenDi.getMaNguoiDung());
        values.put("TenChuyenDi", chuyenDi.getTenChuyenDi().trim());
        values.put("DiemDen", chuyenDi.getDiemDen() != null ? chuyenDi.getDiemDen().trim() : null);
        values.put("NgayDi", chuyenDi.getNgayDi());
        values.put("NgayVe", chuyenDi.getNgayVe());
        values.put("NganSach", chuyenDi.getNganSach());
        values.put("TrangThai", chuyenDi.getTrangThai() != null ? chuyenDi.getTrangThai() : "SAP_TOI");
        values.put("AnhBia", chuyenDi.getAnhBia());
        values.put("SoThanhVien", chuyenDi.getSoThanhVien());

        db.beginTransaction();
        try {
            long id = db.insert("ChuyenDi", null, values);
            if (id > 0) {
                if (danhSachIdThanhVien != null && !danhSachIdThanhVien.isEmpty()) {
                    for (int userId : danhSachIdThanhVien) {
                        ContentValues tvValues = new ContentValues();
                        tvValues.put("MaChuyenDi", id);
                        tvValues.put("MaNguoiDung", userId);
                        db.insertWithOnConflict("ThanhVienChuyenDi", null, tvValues, SQLiteDatabase.CONFLICT_IGNORE);
                    }
                } else {
                    ContentValues tvValues = new ContentValues();
                    tvValues.put("MaChuyenDi", id);
                    tvValues.put("MaNguoiDung", chuyenDi.getMaNguoiDung());
                    db.insertWithOnConflict("ThanhVienChuyenDi", null, tvValues, SQLiteDatabase.CONFLICT_IGNORE);
                }
                db.setTransactionSuccessful();
            }
            return id;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi thêm chuyến đi", e);
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    public long themChuyenDi(ChuyenDi chuyenDi) {
        return themChuyenDi(chuyenDi, null);
    }

    // lấy danh sách chuyến đi của người dùng kèm dữ liệu thống kê từ View v_TongChiChuyenDi (Read)
    public List<ChuyenDi> layDanhSachChuyenDi(int maNguoiDung) {
        List<ChuyenDi> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // kết hợp bảng ChuyenDi với View v_TongChiChuyenDi (lấy chuyến đi do user tạo hoặc user tham gia)
        String sql = "SELECT c.*, " +
                "       COALESCE(v.TongChi, 0) AS TongChi, " +
                "       COALESCE(v.ConLai, c.NganSach) AS ConLai, " +
                "       COALESCE(v.TyLeSuDung, 0.0) AS TyLeSuDung, " +
                "       COALESCE(v.MucCanhBao, 'BINH_THUONG') AS MucCanhBao " +
                "FROM ChuyenDi c " +
                "LEFT JOIN v_TongChiChuyenDi v ON c.MaChuyenDi = v.MaChuyenDi " +
                "WHERE c.MaNguoiDung = ? " +
                "   OR c.MaChuyenDi IN (SELECT MaChuyenDi FROM ThanhVienChuyenDi WHERE MaNguoiDung = ?) " +
                "ORDER BY c.NgayDi DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maNguoiDung), String.valueOf(maNguoiDung)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ChuyenDi cd = parseChuyenDiFromCursor(cursor);
                    list.add(cd);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lấy danh sách chuyến đi", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // lấy chi tiết một chuyến đi theo ID

    public ChuyenDi layChuyenDiTheoId(int maChuyenDi) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT c.*, " +
                "       COALESCE(v.TongChi, 0) AS TongChi, " +
                "       COALESCE(v.ConLai, c.NganSach) AS ConLai, " +
                "       COALESCE(v.TyLeSuDung, 0.0) AS TyLeSuDung, " +
                "       COALESCE(v.MucCanhBao, 'BINH_THUONG') AS MucCanhBao " +
                "FROM ChuyenDi c " +
                "LEFT JOIN v_TongChiChuyenDi v ON c.MaChuyenDi = v.MaChuyenDi " +
                "WHERE c.MaChuyenDi = ?";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maChuyenDi)});
            if (cursor != null && cursor.moveToFirst()) {
                return parseChuyenDiFromCursor(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    // lấy chuyến đi đang diễn ra trong ngày hôm nay (NgayDi <= ngayHomNay <= NgayVe)
    public ChuyenDi layChuyenDiHomNay(int maNguoiDung, String ngayHomNay) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT c.*, " +
                "       COALESCE(v.TongChi, 0) AS TongChi, " +
                "       COALESCE(v.ConLai, c.NganSach) AS ConLai, " +
                "       COALESCE(v.TyLeSuDung, 0.0) AS TyLeSuDung, " +
                "       COALESCE(v.MucCanhBao, 'BINH_THUONG') AS MucCanhBao " +
                "FROM ChuyenDi c " +
                "LEFT JOIN v_TongChiChuyenDi v ON c.MaChuyenDi = v.MaChuyenDi " +
                "WHERE (c.MaNguoiDung = ? OR c.MaChuyenDi IN (SELECT MaChuyenDi FROM ThanhVienChuyenDi WHERE MaNguoiDung = ?)) " +
                "  AND ? BETWEEN c.NgayDi AND c.NgayVe " +
                "ORDER BY c.NgayDi ASC LIMIT 1";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(maNguoiDung), String.valueOf(maNguoiDung), ngayHomNay});
            if (cursor != null && cursor.moveToFirst()) {
                return parseChuyenDiFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lấy chuyến đi hôm nay", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    // cập nhật thông tin chuyến đi kèm danh sách thành viên tham gia (Update)
    public boolean capNhatChuyenDi(ChuyenDi chuyenDi, List<Integer> danhSachIdThanhVien) {
        if (chuyenDi == null) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenChuyenDi", chuyenDi.getTenChuyenDi().trim());
        values.put("DiemDen", chuyenDi.getDiemDen() != null ? chuyenDi.getDiemDen().trim() : null);
        values.put("NgayDi", chuyenDi.getNgayDi());
        values.put("NgayVe", chuyenDi.getNgayVe());
        values.put("NganSach", chuyenDi.getNganSach());
        values.put("TrangThai", chuyenDi.getTrangThai());
        values.put("AnhBia", chuyenDi.getAnhBia());
        values.put("SoThanhVien", chuyenDi.getSoThanhVien());

        db.beginTransaction();
        try {
            int rows = db.update("ChuyenDi", values, "MaChuyenDi = ?",
                    new String[]{String.valueOf(chuyenDi.getMaChuyenDi())});
            if (rows > 0) {
                if (danhSachIdThanhVien != null) {
                    db.delete("ThanhVienChuyenDi", "MaChuyenDi = ?",
                            new String[]{String.valueOf(chuyenDi.getMaChuyenDi())});
                    for (int userId : danhSachIdThanhVien) {
                        ContentValues tvValues = new ContentValues();
                        tvValues.put("MaChuyenDi", chuyenDi.getMaChuyenDi());
                        tvValues.put("MaNguoiDung", userId);
                        db.insertWithOnConflict("ThanhVienChuyenDi", null, tvValues, SQLiteDatabase.CONFLICT_IGNORE);
                    }
                }
                db.setTransactionSuccessful();
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi cập nhật chuyến đi", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public boolean capNhatChuyenDi(ChuyenDi chuyenDi) {
        return capNhatChuyenDi(chuyenDi, null);
    }

    // xóa chuyến đi (Delete - tự động CASCADE xóa Lịch Trình, Chi Phí, Ảnh, Thành Viên)
    public boolean xoaChuyenDi(int maChuyenDi) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rows = db.delete("ChuyenDi", "MaChuyenDi = ?", new String[]{String.valueOf(maChuyenDi)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xóa chuyến đi", e);
            return false;
        }
    }

    // tìm kiếm chuyến đi theo từ khóa (tìm tên chuyến đi, địa điểm, lịch trình, ghi chú...)
    public List<ChuyenDi> timKiemChuyenDi(int maNguoiDung, String keyword) {
        List<ChuyenDi> list = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return layDanhSachChuyenDi(maNguoiDung);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT c.*, " +
                "       COALESCE(v.TongChi, 0) AS TongChi, " +
                "       COALESCE(v.ConLai, c.NganSach) AS ConLai, " +
                "       COALESCE(v.TyLeSuDung, 0.0) AS TyLeSuDung, " +
                "       COALESCE(v.MucCanhBao, 'BINH_THUONG') AS MucCanhBao " +
                "FROM ChuyenDi c " +
                "LEFT JOIN v_TongChiChuyenDi v ON c.MaChuyenDi = v.MaChuyenDi " +
                "WHERE (c.MaNguoiDung = ? OR c.MaChuyenDi IN (SELECT MaChuyenDi FROM ThanhVienChuyenDi WHERE MaNguoiDung = ?)) " +
                "  AND (c.TenChuyenDi LIKE ? OR c.DiemDen LIKE ? " +
                "       OR c.MaChuyenDi IN (SELECT MaChuyenDi FROM LichTrinh WHERE TieuDe LIKE ? OR DiaDiem LIKE ? OR GhiChu LIKE ?) " +
                "       OR c.MaChuyenDi IN (SELECT MaChuyenDi FROM ChiPhi WHERE GhiChu LIKE ?)) " +
                "ORDER BY c.NgayDi DESC";

        String pattern = "%" + keyword.trim() + "%";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{
                    String.valueOf(maNguoiDung),
                    String.valueOf(maNguoiDung),
                    pattern, pattern, pattern, pattern, pattern, pattern
            });
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(parseChuyenDiFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tìm kiếm chuyến đi", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // lấy danh sách ID người dùng tham gia chuyến đi
    public List<Integer> layDanhSachIdThanhVien(int maChuyenDi) {
        List<Integer> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT MaNguoiDung FROM ThanhVienChuyenDi WHERE MaChuyenDi = ?",
                    new String[]{String.valueOf(maChuyenDi)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursor.getInt(0));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lấy danh sách ID thành viên", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public boolean themThanhVienChuyenDi(int maChuyenDi, int maNguoiDung) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MaChuyenDi", maChuyenDi);
        values.put("MaNguoiDung", maNguoiDung);
        try {
            long id = db.insertWithOnConflict("ThanhVienChuyenDi", null, values, SQLiteDatabase.CONFLICT_IGNORE);
            capNhatSoLuongThanhVien(maChuyenDi);
            return id >= 0;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi thêm thành viên", e);
            return false;
        }
    }

    public boolean xoaThanhVienChuyenDi(int maChuyenDi, int maNguoiDung) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rows = db.delete("ThanhVienChuyenDi", "MaChuyenDi = ? AND MaNguoiDung = ?",
                    new String[]{String.valueOf(maChuyenDi), String.valueOf(maNguoiDung)});
            capNhatSoLuongThanhVien(maChuyenDi);
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xóa thành viên", e);
            return false;
        }
    }

    private void capNhatSoLuongThanhVien(int maChuyenDi) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<Integer> tvList = layDanhSachIdThanhVien(maChuyenDi);
        ChuyenDi cd = layChuyenDiTheoId(maChuyenDi);
        if (cd != null) {
            int count = tvList != null ? tvList.size() : 1;
            if (count == 0 && cd.getMaNguoiDung() > 0) count = 1;
            ContentValues cv = new ContentValues();
            cv.put("SoThanhVien", count);
            db.update("ChuyenDi", cv, "MaChuyenDi = ?", new String[]{String.valueOf(maChuyenDi)});
        }
    }

    private ChuyenDi parseChuyenDiFromCursor(Cursor cursor) {
        ChuyenDi cd = new ChuyenDi();
        cd.setMaChuyenDi(cursor.getInt(cursor.getColumnIndexOrThrow("MaChuyenDi")));
        cd.setMaNguoiDung(cursor.getInt(cursor.getColumnIndexOrThrow("MaNguoiDung")));
        cd.setTenChuyenDi(cursor.getString(cursor.getColumnIndexOrThrow("TenChuyenDi")));
        cd.setDiemDen(cursor.getString(cursor.getColumnIndexOrThrow("DiemDen")));
        cd.setNgayDi(cursor.getString(cursor.getColumnIndexOrThrow("NgayDi")));
        cd.setNgayVe(cursor.getString(cursor.getColumnIndexOrThrow("NgayVe")));
        cd.setNganSach(cursor.getLong(cursor.getColumnIndexOrThrow("NganSach")));
        cd.setTrangThai(cursor.getString(cursor.getColumnIndexOrThrow("TrangThai")));
        cd.setAnhBia(cursor.getString(cursor.getColumnIndexOrThrow("AnhBia")));

        int idxSoTV = cursor.getColumnIndex("SoThanhVien");
        if (idxSoTV != -1) {
            cd.setSoThanhVien(cursor.getInt(idxSoTV));
        }

        // Các cột thống kê
        cd.setTongChi(cursor.getLong(cursor.getColumnIndexOrThrow("TongChi")));
        cd.setConLai(cursor.getLong(cursor.getColumnIndexOrThrow("ConLai")));
        cd.setTyLeSuDung(cursor.getDouble(cursor.getColumnIndexOrThrow("TyLeSuDung")));
        cd.setMucCanhBao(cursor.getString(cursor.getColumnIndexOrThrow("MucCanhBao")));
        return cd;
    }
}