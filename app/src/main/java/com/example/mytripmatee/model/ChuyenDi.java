package com.example.mytripmatee.model;

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
}
