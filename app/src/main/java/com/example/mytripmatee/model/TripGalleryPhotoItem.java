package com.example.mytripmatee.model;

public class TripGalleryPhotoItem {
    private final String id;
    private final String title;
    private final String location;
    private final int imageResId;
    private final String category;

    public TripGalleryPhotoItem(String id, String title, String location, int imageResId, String category) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.imageResId = imageResId;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getCategory() {
        return category;
    }
}
