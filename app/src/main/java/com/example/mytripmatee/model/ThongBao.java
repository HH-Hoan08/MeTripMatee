package com.example.mytripmatee.model;

public class ThongBao {
    private int maThongBao;
    private int maNguoiDung;   // Nguoi nhan thong bao
    private int maNguoiGui;    // Nguoi gui / tao hoat dong
    private int maChuyenDi;    // Chuyen di lien quan
    private String tieuDe;
    private String noiDung;
    private String loaiThongBao; // "MOI_THAM_GIA", "CAP_NHAT_CHUYEN_DI", "CAP_NHAT_LICH_TRINH"
    private int trangThai;       // 0: Chua xu ly/Chua doc, 1: Da chap nhan, 2: Da tu choi/Da doc
    private String ngayTao;

    // Additional display properties
    private String tenNguoiGui;
    private String tenChuyenDi;

    public ThongBao() {}

    public ThongBao(int maNguoiDung, int maNguoiGui, int maChuyenDi, String tieuDe, String noiDung, String loaiThongBao, String ngayTao) {
        this.maNguoiDung = maNguoiDung;
        this.maNguoiGui = maNguoiGui;
        this.maChuyenDi = maChuyenDi;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.loaiThongBao = loaiThongBao;
        this.trangThai = 0;
        this.ngayTao = ngayTao;
    }

    public int getMaThongBao() { return maThongBao; }
    public void setMaThongBao(int maThongBao) { this.maThongBao = maThongBao; }

    public int getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(int maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public int getMaNguoiGui() { return maNguoiGui; }
    public void setMaNguoiGui(int maNguoiGui) { this.maNguoiGui = maNguoiGui; }

    public int getMaChuyenDi() { return maChuyenDi; }
    public void setMaChuyenDi(int maChuyenDi) { this.maChuyenDi = maChuyenDi; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getLoaiThongBao() { return loaiThongBao; }
    public void setLoaiThongBao(String loaiThongBao) { this.loaiThongBao = loaiThongBao; }

    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }

    public String getNgayTao() { return ngayTao; }
    public void setNgayTao(String ngayTao) { this.ngayTao = ngayTao; }

    public String getTenNguoiGui() { return tenNguoiGui; }
    public void setTenNguoiGui(String tenNguoiGui) { this.tenNguoiGui = tenNguoiGui; }

    public String getTenChuyenDi() { return tenChuyenDi; }
    public void setTenChuyenDi(String tenChuyenDi) { this.tenChuyenDi = tenChuyenDi; }
}
