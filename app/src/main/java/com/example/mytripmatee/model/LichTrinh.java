package com.example.mytripmatee.model;

public class LichTrinh {

    public enum LoaiHoatDong {
        DI_CHUYEN("DI_CHUYEN", "Di chuyển"),
        AN_UONG("AN_UONG", "Ăn uống"),
        THAM_QUAN("THAM_QUAN", "Tham quan"),
        LUU_TRU("LUU_TRU", "Lưu trú");

        private final String code;
        private final String label;

        LoaiHoatDong(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() { return code; }
        public String getLabel() { return label; }

        public static LoaiHoatDong fromCode(String code) {
            if (code == null) return THAM_QUAN;
            for (LoaiHoatDong type : values()) {
                if (type.code.equalsIgnoreCase(code) || type.name().equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return THAM_QUAN;
        }
    }

    private int maHoatDong;
    private int maChuyenDi;
    private String ngayHoatDong; // Định dạng YYYY-MM-DD
    private String gioBatDau;    // Định dạng HH:MM
    private String tieuDe;
    private String diaDiem;
    private String linkBanDo;
    private LoaiHoatDong loaiHoatDong; // DI_CHUYEN, AN_UONG, THAM_QUAN, LUU_TRU
    private String ghiChu;
    private boolean daDi;

    public LichTrinh() {
        this.loaiHoatDong = LoaiHoatDong.THAM_QUAN;
        this.daDi = false;
    }

    // Constructor thêm mới (chưa có maHoatDong)
    public LichTrinh(int maChuyenDi, String ngayHoatDong, String gioBatDau, String tieuDe,
                     String diaDiem, String linkBanDo, LoaiHoatDong loaiHoatDong, String ghiChu, boolean daDi) {
        this.maChuyenDi = maChuyenDi;
        this.ngayHoatDong = ngayHoatDong;
        this.gioBatDau = gioBatDau;
        this.tieuDe = tieuDe;
        this.diaDiem = diaDiem;
        this.linkBanDo = linkBanDo;
        this.loaiHoatDong = loaiHoatDong != null ? loaiHoatDong : LoaiHoatDong.THAM_QUAN;
        this.ghiChu = ghiChu;
        this.daDi = daDi;
    }

    // Constructor đầy đủ
    public LichTrinh(int maHoatDong, int maChuyenDi, String ngayHoatDong, String gioBatDau,
                     String tieuDe, String diaDiem, String linkBanDo, LoaiHoatDong loaiHoatDong, String ghiChu, boolean daDi) {
        this(maChuyenDi, ngayHoatDong, gioBatDau, tieuDe, diaDiem, linkBanDo, loaiHoatDong, ghiChu, daDi);
        this.maHoatDong = maHoatDong;
    }

    // Getters & Setters
    public int getMaHoatDong() { return maHoatDong; }
    public void setMaHoatDong(int maHoatDong) { this.maHoatDong = maHoatDong; }

    public int getMaChuyenDi() { return maChuyenDi; }
    public void setMaChuyenDi(int maChuyenDi) { this.maChuyenDi = maChuyenDi; }

    public String getNgayHoatDong() { return ngayHoatDong; }
    public void setNgayHoatDong(String ngayHoatDong) { this.ngayHoatDong = ngayHoatDong; }

    public String getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(String gioBatDau) { this.gioBatDau = gioBatDau; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getDiaDiem() { return diaDiem; }
    public void setDiaDiem(String diaDiem) { this.diaDiem = diaDiem; }

    public String getLinkBanDo() { return linkBanDo; }
    public void setLinkBanDo(String linkBanDo) { this.linkBanDo = linkBanDo; }

    public LoaiHoatDong getLoaiHoatDong() { return loaiHoatDong; }
    public void setLoaiHoatDong(LoaiHoatDong loaiHoatDong) { this.loaiHoatDong = loaiHoatDong; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public boolean isDaDi() { return daDi; }
    public void setDaDi(boolean daDi) { this.daDi = daDi; }

    // các phương thức hỗ trợ hiển thị Adapter UI
    public String getNhanLoaiHoatDong() {
        return loaiHoatDong != null ? loaiHoatDong.getLabel() : "Tham quan";
    }

    public String getKhungGio() {
        return gioBatDau != null && !gioBatDau.isEmpty() ? gioBatDau : "Cả ngày";
    }

    public String getMoTa() {
        if (diaDiem != null && !diaDiem.trim().isEmpty()) return diaDiem;
        if (ghiChu != null && !ghiChu.trim().isEmpty()) return ghiChu;
        return "";
    }

    public boolean isCoBanDo() {
        return linkBanDo != null && !linkBanDo.trim().isEmpty();
    }

    public String getThongTinPhu() {
        return ghiChu != null ? ghiChu : "";
    }

    public String getVanBanTrangThai() {
        return daDi ? "Đã đi" : "Đánh dấu đã đi";
    }

    public boolean isDaHoanThanh() { return daDi; }
    public void setDaHoanThanh(boolean daHoanThanh) { this.daDi = daHoanThanh; }

    public String getVanBanCanhBao() { return null; }
}