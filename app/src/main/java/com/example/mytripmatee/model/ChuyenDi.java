package com.example.mytripmatee.model;

import com.example.mytripmatee.R;

public class ChuyenDi {
    private int maChuyenDi;
    private int maNguoiDung;
    private String tenChuyenDi;
    private String diemDen;
    private String ngayDi;      // yyyy-MM-dd
    private String ngayVe;      // yyyy-MM-dd
    private long nganSach;      // VND
    private String trangThai;   // saptoi, dangdi, daxong
    private String anhBia;      // url
    private int soThanhVien = 1;
    // Các trường thống kê mở rộng (từ VIEW v_TongChiChuyenDi)
    private long tongChi;
    private long conLai;
    private double tyLeSuDung;
    private String mucCanhBao;  // binhthuong, canhbao, vuotngansach
    public ChuyenDi() {
        this.trangThai = "SAP_TOI";
    }
    // constructor dùng khi tạo mới chuyến đi
    public ChuyenDi(int maNguoiDung, String tenChuyenDi, String diemDen, String ngayDi, String ngayVe, long nganSach) {
        this.maNguoiDung = maNguoiDung;
        this.tenChuyenDi = tenChuyenDi;
        this.diemDen = diemDen;
        this.ngayDi = ngayDi;
        this.ngayVe = ngayVe;
        this.nganSach = nganSach;
        this.trangThai = "SAP_TOI";
    }

    public int getMaChuyenDi() {
        return maChuyenDi;
    }

    public String getMucCanhBao() {
        return mucCanhBao;
    }

    public double getTyLeSuDung() {
        return tyLeSuDung;
    }

    public long getConLai() {
        return conLai;
    }

    public long getTongChi() {
        return tongChi;
    }

    public String getAnhBia() {
        return anhBia;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public long getNganSach() {
        return nganSach;
    }

    public String getNgayVe() {
        return ngayVe;
    }

    public String getNgayDi() {
        return ngayDi;
    }

    public String getDiemDen() {
        return diemDen;
    }

    public int getMaNguoiDung() {
        return maNguoiDung;
    }

    public String getTenChuyenDi() {
        return tenChuyenDi;
    }

    public void setMaChuyenDi(int maChuyenDi) {
        this.maChuyenDi = maChuyenDi;
    }

    public void setMaNguoiDung(int maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public void setTenChuyenDi(String tenChuyenDi) {
        this.tenChuyenDi = tenChuyenDi;
    }

    public void setDiemDen(String diemDen) {
        this.diemDen = diemDen;
    }

    public void setNgayDi(String ngayDi) {
        this.ngayDi = ngayDi;
    }

    public void setNgayVe(String ngayVe) {
        this.ngayVe = ngayVe;
    }

    public void setNganSach(long nganSach) {
        this.nganSach = nganSach;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public void setAnhBia(String anhBia) {
        this.anhBia = anhBia;
    }

    public void setTongChi(long tongChi) {
        this.tongChi = tongChi;
    }

    public void setTyLeSuDung(double tyLeSuDung) {
        this.tyLeSuDung = tyLeSuDung;
    }

    public void setConLai(long conLai) {
        this.conLai = conLai;
    }

    public void setMucCanhBao(String mucCanhBao) {
        this.mucCanhBao = mucCanhBao;
    }

    public int getSoThanhVien() {
        return soThanhVien;
    }

    public void setSoThanhVien(int soThanhVien) {
        this.soThanhVien = Math.max(1, soThanhVien);
    }

    public String getTrangThaiThucTe() {
        if (ngayDi == null || ngayVe == null || ngayDi.isEmpty() || ngayVe.isEmpty()) {
            return trangThai != null ? trangThai : "SAP_TOI";
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date today = sdf.parse(sdf.format(new java.util.Date()));
            java.util.Date dStart = sdf.parse(ngayDi);
            java.util.Date dEnd = sdf.parse(ngayVe);

            if (today != null && dStart != null && dEnd != null) {
                if (today.after(dEnd)) {
                    return "DA_XONG";
                } else if (!today.before(dStart) && !today.after(dEnd)) {
                    return "DANG_DI";
                } else {
                    return "SAP_TOI";
                }
            }
        } catch (Exception ignored) {}
        return trangThai != null ? trangThai : "SAP_TOI";
    }

    // chuyển đổi đối tượng ChuyenDi sang TripItem để hiển thị trên RecyclerView
    public ItemChuyenDi toTripItem() {
        String realStatus = getTrangThaiThucTe();
        String statusLabel;
        ItemChuyenDi.LoaiTrangThai statusType;

        if ("DANG_DI".equalsIgnoreCase(realStatus)) {
            statusLabel = "Đang diễn ra";
            statusType = ItemChuyenDi.LoaiTrangThai.SAP_TOI;
        } else if ("DA_XONG".equalsIgnoreCase(realStatus)) {
            statusLabel = "Đã hoàn thành";
            statusType = ItemChuyenDi.LoaiTrangThai.DA_HOAN_THANH;
        } else {
            statusLabel = "Sắp tới";
            statusType = ItemChuyenDi.LoaiTrangThai.SAP_TOI;
        }

        String dataRange = (ngayDi != null ? ngayDi : "") + "→" + (ngayVe != null ? ngayVe : "");
        String details = (diemDen != null && !diemDen.isEmpty()) ? diemDen : "Chưa có điểm đến";
        if (soThanhVien > 1) {
            details += " • " + soThanhVien + " thành viên";
        }

        int imageRes = R.drawable.ic_hotel;
        String uriPath = null;

        if (anhBia != null && !anhBia.trim().isEmpty()) {
            uriPath = anhBia.trim();
            String ab = anhBia.toLowerCase();
            if (ab.contains("hagiang") || ab.contains("ha giang")) imageRes = R.drawable.img_hagiang;
            else if (ab.contains("phuquoc") || ab.contains("phu quoc")) imageRes = R.drawable.img_phuquoc;
            else if (ab.contains("nhatrang") || ab.contains("nha trang")) imageRes = R.drawable.img_nhatrang;
            else if (ab.contains("dalat") || ab.contains("da lat")) imageRes = R.drawable.img_dalat;
        }

        return new ItemChuyenDi(
                String.valueOf(maChuyenDi),
                tenChuyenDi,
                statusLabel,
                statusType,
                dataRange,
                details,
                imageRes,
                uriPath
        );
    }
}
