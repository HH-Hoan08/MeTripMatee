package com.example.mytripmatee.model;

public class ItemAnhThuVien {
    private final String maId;
    private final String tieuDe;
    private final String diaDiem;
    private final int maAnhResId;
    private final String danhMuc;

    public ItemAnhThuVien(String maId, String tieuDe, String diaDiem, int maAnhResId, String danhMuc) {
        this.maId = maId;
        this.tieuDe = tieuDe;
        this.diaDiem = diaDiem;
        this.maAnhResId = maAnhResId;
        this.danhMuc = danhMuc;
    }

    public String getMaId() {
        return maId;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public String getDiaDiem() {
        return diaDiem;
    }

    public int getMaAnhResId() {
        return maAnhResId;
    }

    public String getDanhMuc() {
        return danhMuc;
    }
}
