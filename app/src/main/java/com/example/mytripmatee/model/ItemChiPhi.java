package com.example.mytripmatee.model;

public class ItemChiPhi {
    public enum LoaiDanhMuc {
        DI_CHUYEN,
        AN_UONG,
        LUU_TRU,
        KHAC
    }

    private final String maId;
    private final String tieuDe;
    private final String tieuDePhu;
    private final long soTien;
    private final LoaiDanhMuc loaiDanhMuc;
    private final String nguoiChi;
    private final String tenChuyenDi;
    private final boolean isCaNhan;

    public ItemChiPhi(String maId, String tieuDe, String tieuDePhu, long soTien, LoaiDanhMuc loaiDanhMuc, String nguoiChi, String tenChuyenDi, boolean isCaNhan) {
        this.maId = maId;
        this.tieuDe = tieuDe;
        this.tieuDePhu = tieuDePhu;
        this.soTien = soTien;
        this.loaiDanhMuc = loaiDanhMuc;
        this.nguoiChi = nguoiChi;
        this.tenChuyenDi = tenChuyenDi;
        this.isCaNhan = isCaNhan;
    }

    public ItemChiPhi(String maId, String tieuDe, String tieuDePhu, long soTien, LoaiDanhMuc loaiDanhMuc, String nguoiChi, String tenChuyenDi) {
        this(maId, tieuDe, tieuDePhu, soTien, loaiDanhMuc, nguoiChi, tenChuyenDi, false);
    }

    public String getMaId() { return maId; }
    public String getTieuDe() { return tieuDe; }
    public String getTieuDePhu() { return tieuDePhu; }
    public long getSoTien() { return soTien; }
    public LoaiDanhMuc getLoaiDanhMuc() { return loaiDanhMuc; }
    public String getNguoiChi() { return nguoiChi; }
    public String getTenChuyenDi() { return tenChuyenDi; }
    public boolean isCaNhan() { return isCaNhan; }

    public String getSoTienDinhDang() {
        return String.format(java.util.Locale.getDefault(), "%,d đ", soTien).replace(',', '.');
    }
}
