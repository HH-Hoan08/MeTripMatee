package com.example.mytripmatee;

public class ItineraryItem {

    public enum ItemType {
        TRANSPORT,
        DINING,
        STAY,
        SIGHTSEEING
    }

    private final String id;
    private final ItemType itemType;
    private final String typeLabel;
    private final String timeRange;
    private final String title;
    private final String description;
    private final String subInfo;
    private final String statusText;
    private boolean isDone;
    private final boolean hasMaps;
    private final String alertText;

    public ItineraryItem(String id, ItemType itemType, String typeLabel, String timeRange,
                         String title, String description, String subInfo,
                         String statusText, boolean isDone, boolean hasMaps, String alertText) {
        this.id = id;
        this.itemType = itemType;
        this.typeLabel = typeLabel;
        this.timeRange = timeRange;
        this.title = title;
        this.description = description;
        this.subInfo = subInfo;
        this.statusText = statusText;
        this.isDone = isDone;
        this.hasMaps = hasMaps;
        this.alertText = alertText;
    }

    public String getId() {
        return id;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSubInfo() {
        return subInfo;
    }

    public String getStatusText() {
        return statusText;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public boolean hasMaps() {
        return hasMaps;
    }

    public String getAlertText() {
        return alertText;
    }
}
