package com.example.venueease;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class VenueAdapter extends RecyclerView.Adapter<VenueAdapter.VenueViewHolder> {

    private Context context;
    private List<Venue> venueList;

    // Create an interface for actions
    public interface OnVenueActionListener {
        void onEditClicked(Venue venue);
        void onDeleteClicked(Venue venue);
    }
    private final OnVenueActionListener actionListener;

    // Update the constructor to accept the listener
    public VenueAdapter(Context context, List<Venue> venueList, OnVenueActionListener listener) {
        this.context = context;
        this.venueList = venueList;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public VenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_venue_admin, parent, false);
        // Pass the listener to the ViewHolder
        return new VenueViewHolder(view, actionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull VenueViewHolder holder, int position) {
        Venue venue = venueList.get(position);
        holder.bind(venue);
    }

    @Override
    public int getItemCount() {
        return venueList.size();
    }

    public void updateVenues(List<Venue> newVenueList) {
        this.venueList.clear();
        this.venueList.addAll(newVenueList);
        notifyDataSetChanged();
    }

    public class VenueViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVenueImage;
        TextView tvVenuePrice, tvVenueName, tvVenueLocation, tvVenueCapacity, tvTagType, tvTagAmenities;
        ImageButton btnEditVenue, btnDeleteVenue;

        // Update ViewHolder constructor
        public VenueViewHolder(@NonNull View itemView, OnVenueActionListener listener) {
            super(itemView);
            ivVenueImage = itemView.findViewById(R.id.iv_venue_image);
            tvVenuePrice = itemView.findViewById(R.id.tv_venue_price);
            tvVenueName = itemView.findViewById(R.id.tv_venue_name);
            tvVenueLocation = itemView.findViewById(R.id.tv_venue_location);
            tvVenueCapacity = itemView.findViewById(R.id.tv_venue_capacity);
            tvTagType = itemView.findViewById(R.id.tv_tag_type);
            tvTagAmenities = itemView.findViewById(R.id.tv_tag_amenities);
            btnEditVenue = itemView.findViewById(R.id.btn_edit_venue);
            btnDeleteVenue = itemView.findViewById(R.id.btn_delete_venue);

            // Set click listeners
            btnEditVenue.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditClicked(venueList.get(position));
                }
            });

            btnDeleteVenue.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClicked(venueList.get(position));
                }
            });
        }

        // Helper method to bind data
        void bind(Venue venue) {
            tvVenueName.setText(venue.getName());
            tvVenueLocation.setText(venue.getLocation());
            tvVenueCapacity.setText(String.format(Locale.getDefault(), "%d guests", venue.getCapacity()));
            tvVenuePrice.setText(String.format(Locale.getDefault(), "₹%.0f/hr", venue.getPrice()));
            tvTagType.setText(venue.getType());

            String amenities = venue.getAmenities();
            if (amenities == null || amenities.isEmpty()) {
                tvTagAmenities.setText("0 amenities");
            } else {
                int count = amenities.split(",").length;
                tvTagAmenities.setText(String.format(Locale.getDefault(), "%d amenities", count));
            }

            String photoUriString = venue.getPhotoUri();
            if (photoUriString != null && !photoUriString.isEmpty()) {
                try {
                    ivVenueImage.setImageURI(Uri.parse(photoUriString));
                } catch (Exception e) {
                    ivVenueImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                ivVenueImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }
}