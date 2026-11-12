package com.example.mytripmatee;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytripmatee.databinding.ItemGalleryPhotoBinding;

import java.util.ArrayList;
import java.util.List;

public class TripGalleryAdapter extends RecyclerView.Adapter<TripGalleryAdapter.GalleryViewHolder> {

    public interface OnPhotoClickListener {
        void onPhotoClick(TripGalleryPhoto photo);
    }

    private final List<TripGalleryPhoto> photoList = new ArrayList<>();
    private final OnPhotoClickListener listener;

    public TripGalleryAdapter(OnPhotoClickListener listener) {
        this.listener = listener;
    }

    public void setPhotos(List<TripGalleryPhoto> photos) {
        this.photoList.clear();
        if (photos != null) {
            this.photoList.addAll(photos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGalleryPhotoBinding binding = ItemGalleryPhotoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new GalleryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        if (position >= 0 && position < photoList.size()) {
            holder.bind(photoList.get(position), listener);
        }
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        private final ItemGalleryPhotoBinding binding;

        public GalleryViewHolder(@NonNull ItemGalleryPhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TripGalleryPhoto photo, OnPhotoClickListener listener) {
            if (photo == null) return;

            binding.imgPhoto.setImageResource(photo.getImageResId() != 0 ? photo.getImageResId() : R.drawable.img_dalat);
            binding.tvPhotoTitle.setText(photo.getTitle() != null ? photo.getTitle() : "");

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPhotoClick(photo);
                }
            });
        }
    }
}
