package com.example.mytripmatee.model;

public class ItemChuyenDi {

    public enum LoaiTrangThai {
        NHAP,
        DA_HOAN_THANH,
        SAP_TOI
    }

    private final String maId;
    private final String tieuDe;
    private final String trangThaiHienThi;
    private final LoaiTrangThai loaiTrangThai;
    private final String theNgay;
    private final String chiTiet;
    private final int maAnhResId;
    private String duongDanAnhUri;

    public ItemChuyenDi(String maId, String tieuDe, String trangThaiHienThi, LoaiTrangThai loaiTrangThai, String theNgay, String chiTiet, int maAnhResId) {
        this.maId = maId;
        this.tieuDe = tieuDe;
        this.trangThaiHienThi = trangThaiHienThi;
        this.loaiTrangThai = loaiTrangThai;
        this.theNgay = theNgay;
        this.chiTiet = chiTiet;
        this.maAnhResId = maAnhResId;
    }

    public String getMaId() {
        return maId;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public String getTrangThaiHienThi() {
        return trangThaiHienThi;
    }

    public LoaiTrangThai getLoaiTrangThai() {
        return loaiTrangThai;
    }

    public String getTheNgay() {
        return theNgay;
    }

    public String getChiTiet() {
        return chiTiet;
    }

    public int getMaAnhResId() {
        return maAnhResId;
    }

    public String getDuongDanAnhUri() {
        return duongDanAnhUri;
    }

    public void setDuongDanAnhUri(String duongDanAnhUri) {
        this.duongDanAnhUri = duongDanAnhUri;
    }
}
