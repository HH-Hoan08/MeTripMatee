package com.example.mytripmatee.util;

import android.content.Context;

import com.example.mytripmatee.dao.ChiPhiDAO;
import com.example.mytripmatee.model.DanhMucThongKe;

import java.util.List;
import java.util.Map;

public class ThongKe {

    private final ChiPhiDAO chiPhiDAO;

    public ThongKe(Context context) {
        this.chiPhiDAO = new ChiPhiDAO(context);
    }

    // Lấy danh sách thống kê tổng chi tiêu và tỷ lệ % từng danh mục
    public List<DanhMucThongKe> layThongKeTheoDanhMuc(int maChuyenDi) {
        return chiPhiDAO.thongKeChiTieuTheoDanhMuc(maChuyenDi);
    }

    // Lấy thống kê chi tiêu lọc động theo khoảng ngày
    public Map<String, Long> layThongKeTheoKhoangNgay(int maChuyenDi, String tuNgay, String denNgay) {
        return chiPhiDAO.thongKeChiTieuTheoNgay(maChuyenDi, tuNgay, denNgay);
    }
}