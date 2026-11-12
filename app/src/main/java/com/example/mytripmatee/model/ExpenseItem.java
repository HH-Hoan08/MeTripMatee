package com.example.mytripmatee.model;

public class ExpenseItem {
    public enum CategoryType {
        MOVE,
        FOOD,
        STAY,
        OTHER
    }

    private final String id;
    private final String title;
    private final String subtitle;
    private final long amount;
    private final CategoryType categoryType;
    private final String payer;
    private final String tripTitle;

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
