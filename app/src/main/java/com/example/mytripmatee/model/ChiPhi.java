package com.example.mytripmatee.model;

public class ChiPhi {
    private int maChiPhi;
    private int maChuyenDi;
    private int maNguoiDung = 1;
    private int maDanhMuc;
    private long soTien;
    private String ngayChi; // Định dạng YYYY-MM-DD
    private String ghiChu;
    private int laCaNhan = 0; // 0: Chi phí nhóm/lịch trình, 1: Chi phí cá nhân

    // Các thuộc tính hỗ trợ hiển thị danh mục
    private String tenDanhMuc;
    private String mauSac;

    public ChiPhi() {}

    public ChiPhi(int maChuyenDi, int maDanhMuc, long soTien, String ngayChi, String ghiChu) {
        this.maChuyenDi = maChuyenDi;
        this.maDanhMuc = maDanhMuc;
        this.soTien = soTien;
        this.ngayChi = ngayChi;
        this.ghiChu = ghiChu;
        this.laCaNhan = 0;
        this.maNguoiDung = 1;
    }

    public ChiPhi(int maChuyenDi, int maDanhMuc, long soTien, String ngayChi, String ghiChu, int laCaNhan) {
        this.maChuyenDi = maChuyenDi;
        this.maDanhMuc = maDanhMuc;
        this.soTien = soTien;
        this.ngayChi = ngayChi;
        this.ghiChu = ghiChu;
        this.laCaNhan = laCaNhan;
        this.maNguoiDung = 1;
    }

    public int getMaChiPhi() { return maChiPhi; }
    public void setMaChiPhi(int maChiPhi) { this.maChiPhi = maChiPhi; }

    public int getMaChuyenDi() { return maChuyenDi; }
    public void setMaChuyenDi(int maChuyenDi) { this.maChuyenDi = maChuyenDi; }

    public int getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(int maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public int getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(int maDanhMuc) { this.maDanhMuc = maDanhMuc; }

    public long getSoTien() { return soTien; }
    public void setSoTien(long soTien) { this.soTien = soTien; }

    public String getNgayChi() { return ngayChi; }
    public void setNgayChi(String ngayChi) { this.ngayChi = ngayChi; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public int getLaCaNhan() { return laCaNhan; }
    public boolean isCaNhan() { return laCaNhan == 1; }
    public void setLaCaNhan(int laCaNhan) { this.laCaNhan = laCaNhan; }

    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }

    public String getMauSac() { return mauSac; }
    public void setMauSac(String mauSac) { this.mauSac = mauSac; }
}
