package com.example.mytripmatee.model;

import java.util.Locale;

public class ItemChiaTien {
    private String avatarText;   // chữ cái đại diện (VD: "H", "P")
    private boolean isPositive;  // true -> màu xanh (+), false -> màu đỏ (-)
    private String description;  // "Hoàn đã trả tiền phòng", "Phú nợ Hoàn"
    private long amount;         // 1200000, 600000

    public ItemChiaTien(String avatarText, boolean isPositive, String description, long amount) {
        this.avatarText = avatarText;
        this.isPositive = isPositive;
        this.description = description;
        this.amount = amount;
    }

    public String getAvatarText() { return avatarText; }
    public boolean isPositive() { return isPositive; }
    public String getDescription() { return description; }
    public long getAmount() { return amount; }

    public String getDinhDangSoTien() {
        String sign = isPositive ? "+" : "-";
        return String.format(Locale.getDefault(), "%s%,d đ", sign, amount).replace(',', '.');
    }
}