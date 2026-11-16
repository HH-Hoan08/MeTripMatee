package com.example.mytripmatee.model;

public class ItemLichTrinh {

    public enum LoaiHoatDong {
        DI_CHUYEN,
        AN_UONG,
        LUU_TRU,
        THAM_QUAN
    }

    private final String maId;
    private final LoaiHoatDong loaiHoatDong;
    private final String nhanLoaiHoatDong;
    private final String khungGio;
    private final String tieuDe;
    private final String moTa;
    private final String thongTinPhu;
    private final String vanBanTrangThai;
    private boolean daHoanThanh;
    private final boolean coBanDo;
    private final String vanBanCanhBao;

    public ItemLichTrinh(String maId, LoaiHoatDong loaiHoatDong, String nhanLoaiHoatDong, String khungGio,
                         String tieuDe, String moTa, String thongTinPhu,
                         String vanBanTrangThai, boolean daHoanThanh, boolean coBanDo, String vanBanCanhBao) {
        this.maId = maId;
        this.loaiHoatDong = loaiHoatDong;
        this.nhanLoaiHoatDong = nhanLoaiHoatDong;
        this.khungGio = khungGio;
        this.tieuDe = tieuDe;
        this.moTa = moTa;
        this.thongTinPhu = thongTinPhu;
        this.vanBanTrangThai = vanBanTrangThai;
        this.daHoanThanh = daHoanThanh;
        this.coBanDo = coBanDo;
        this.vanBanCanhBao = vanBanCanhBao;
    }

    public String getMaId() {
        return maId;
    }

    public LoaiHoatDong getLoaiHoatDong() {
        return loaiHoatDong;
    }

    public String getNhanLoaiHoatDong() {
        return nhanLoaiHoatDong;
    }

    public String getKhungGio() {
        return khungGio;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public String getMoTa() {
        return moTa;
    }

    public String getThongTinPhu() {
        return thongTinPhu;
    }

    public String getVanBanTrangThai() {
        return vanBanTrangThai;
    }

    public boolean isDaHoanThanh() {
        return daHoanThanh;
    }

    public void setDaHoanThanh(boolean daHoanThanh) {
        this.daHoanThanh = daHoanThanh;
    }

    public boolean isCoBanDo() {
        return coBanDo;
    }

    public String getVanBanCanhBao() {
        return vanBanCanhBao;
    }
}
