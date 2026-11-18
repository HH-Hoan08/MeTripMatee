package com.example.mytripmatee.model;

public class DanhMuc {
    private int maDanhMuc;
    private String tenDanhMuc;
    private String mauSac;

    public DanhMuc() {}

    public DanhMuc(int maDanhMuc, String tenDanhMuc, String mauSac) {
        this.maDanhMuc = maDanhMuc;
        this.tenDanhMuc = tenDanhMuc;
        this.mauSac = mauSac;
    }

    public int getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(int maDanhMuc) { this.maDanhMuc = maDanhMuc; }

    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }

    public String getMauSac() { return mauSac; }
    public void setMauSac(String mauSac) { this.mauSac = mauSac; }
}