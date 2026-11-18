package com.example.mytripmatee.model;

public class DanhMucThongKe {
    private String tenDanhMuc;
    private String mauSac;
    private long tongTien;
    private double tyLePercent;

    public DanhMucThongKe(String tenDanhMuc, String mauSac, long tongTien, double tyLePercent) {
        this.tenDanhMuc = tenDanhMuc;
        this.mauSac = mauSac;
        this.tongTien = tongTien;
        this.tyLePercent = tyLePercent;
    }

    public String getTenDanhMuc() { return tenDanhMuc; }
    public String getMauSac() { return mauSac; }
    public long getTongTien() { return tongTien; }
    public double getTyLePercent() { return tyLePercent; }
}