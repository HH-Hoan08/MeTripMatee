package com.example.mytripmatee;

public class Trip {

    public enum StatusType {
        DRAFT,
        COMPLETED,
        UPCOMING
    }

    private final String id;
    private final String title;
    private final String status;
    private final StatusType statusType;
    private final String dateTag;
    private final String details;
    private final int imageResId;

    public Trip(String id, String title, String status, StatusType statusType, String dateTag, String details, int imageResId) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.statusType = statusType;
        this.dateTag = dateTag;
        this.details = details;
        this.imageResId = imageResId;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public StatusType getStatusType() {
        return statusType;
    }

    public String getDateTag() {
        return dateTag;
    }

    public String getDetails() {
        return details;
    }

    public int getImageResId() {
        return imageResId;
    }
}