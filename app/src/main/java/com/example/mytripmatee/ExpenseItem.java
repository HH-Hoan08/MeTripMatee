package com.example.mytripmatee;

public class ExpenseItem {
    public enum CategoryType {
        MOVE,
        FOOD,
        STAY,
        OTHER
    }

    private String id;
    private String title;
    private String subtitle;
    private long amount;
    private CategoryType categoryType;
    private String payer;
    private String tripTitle;

    public ExpenseItem(String id, String title, String subtitle, long amount, CategoryType categoryType, String payer, String tripTitle) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.categoryType = categoryType;
        this.payer = payer;
        this.tripTitle = tripTitle;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public long getAmount() { return amount; }
    public CategoryType getCategoryType() { return categoryType; }
    public String getPayer() { return payer; }
    public String getTripTitle() { return tripTitle; }

    public String getFormattedAmount() {
        return String.format(java.util.Locale.getDefault(), "%,d đ", amount).replace(',', '.');
    }
}
