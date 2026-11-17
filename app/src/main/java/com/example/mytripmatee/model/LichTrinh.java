package com.example.mytripmatee.model;

public class LichTrinh {
    private int maHoatDong;
    private int maChuyenDi;
    private String ngayHoatDong;
    private String gioBatDau;
    private String tieuDe;
    private String diaDiem;
    private String linkBanDo;
    private String loaiHoatDong;
    private String ghiChu;
    private boolean daDi;

    //Constructor nếu có đầy đủ thông tin
    public LichTrinh(int maHoatDong, int maChuyenDi, String ngayHoatDong, String gioBatDau,
                     String tieuDe, String diaDiem, String linkBanDo, String loaiHoatDong, String ghiChu, boolean daDi) {
        this.maHoatDong = maHoatDong;
        this.maChuyenDi = maChuyenDi;
        this.ngayHoatDong = ngayHoatDong;
        this.gioBatDau = gioBatDau;
        this.tieuDe = tieuDe;
        this.diaDiem = diaDiem;
        this.linkBanDo = linkBanDo;
        this.loaiHoatDong = loaiHoatDong;
        this.ghiChu = ghiChu;
        this.daDi = daDi;
    }

    public int getMaChuyenDi() {
        return maChuyenDi;
    }

    public void setMaChuyenDi(int maChuyenDi) {
        this.maChuyenDi = maChuyenDi;
    }

    public int getMaHoatDong() {
        return maHoatDong;
    }

    public void setMaHoatDong(int maHoatDong) {
        this.maHoatDong = maHoatDong;
    }

    public String getNgayHoatDong() {
        return ngayHoatDong;
    }

    public void setNgayHoatDong(String ngayHoatDong) {
        this.ngayHoatDong = ngayHoatDong;
    }

    public String getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(String gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getDiaDiem() {
        return diaDiem;
    }

    public void setDiaDiem(String diaDiem) {
        this.diaDiem = diaDiem;
    }

    public String getLinkBanDo() {
        return linkBanDo;
    }

    public void setLinkBanDo(String linkBanDo) {
        this.linkBanDo = linkBanDo;
    }

    public String getLoaiHoatDong() {
        return loaiHoatDong;
    }

    public void setLoaiHoatDong(String loaiHoatDong) {
        this.loaiHoatDong = loaiHoatDong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public boolean isDaDi() {
        return daDi;
    }

    public void setDaDi(boolean daDi) {
        this.daDi = daDi;
    }
}
