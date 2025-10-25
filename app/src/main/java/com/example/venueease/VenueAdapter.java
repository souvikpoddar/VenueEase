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

    // (We will add listeners for edit/delete later)
    // public interface OnVenueActionListener {
    //     void onEdit(Venue venue);
    //     void onDelete(Venue venue);
    // }

    public VenueAdapter(Context context, List<Venue> venueList) {
        this.context = context;
        this.venueList = venueList;
    }

    @NonNull
    @Override
    public VenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each list item
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_venue_admin, parent, false);
        return new VenueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VenueViewHolder holder, int position) {
        // Get the data for the current position
        Venue venue = venueList.get(position);

        // Bind the data to the views
        holder.tvVenueName.setText(venue.getName());
        holder.tvVenueLocation.setText(venue.getLocation());

        // Format strings for capacity and price
        String capacityText = String.format(Locale.getDefault(), "%d guests", venue.getCapacity());
        holder.tvVenueCapacity.setText(capacityText);

        String priceText = String.format(Locale.getDefault(), "₹%.0f/hr", venue.getPrice());
        holder.tvVenuePrice.setText(priceText);

        holder.tvTagType.setText(venue.getType());

        // Simple amenities display (e.g., "WIFI, Parking, Audio" -> "3 amenities")
        String amenities = venue.getAmenities();
        if (amenities == null || amenities.isEmpty()) {
            holder.tvTagAmenities.setText("0 amenities");
        } else {
            int count = amenities.split(",").length;
            holder.tvTagAmenities.setText(String.format(Locale.getDefault(), "%d amenities", count));
        }

        // 8. Load the image from the URI
        String photoUriString = venue.getPhotoUri();
        if (photoUriString != null && !photoUriString.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(photoUriString);
                holder.ivVenueImage.setImageURI(imageUri);
            } catch (Exception e) {
                // Handle error (e.g., file deleted, URI invalid)
                // Set a placeholder image
                holder.ivVenueImage.setImageResource(android.R.drawable.ic_menu_gallery); // Placeholder
            }
        } else {
            // No image, set a placeholder
            holder.ivVenueImage.setImageResource(android.R.drawable.ic_menu_gallery); // Placeholder
        }
    }

    @Override
    public int getItemCount() {
        return venueList.size();
    }

    // Method to update the list, e.g., after adding a new venue
    public void updateVenues(List<Venue> newVenueList) {
        this.venueList.clear();
        this.venueList.addAll(newVenueList);
        notifyDataSetChanged(); // Refresh the entire list
    }

    /**
     * ViewHolder class
     * Holds all the views for a single list item
     */
    public static class VenueViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVenueImage;
        TextView tvVenuePrice, tvVenueName, tvVenueLocation, tvVenueCapacity, tvTagType, tvTagAmenities;
        ImageButton btnEditVenue, btnDeleteVenue;

        public VenueViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find all views from list_item_venue_admin.xml
            ivVenueImage = itemView.findViewById(R.id.iv_venue_image);
            tvVenuePrice = itemView.findViewById(R.id.tv_venue_price);
            tvVenueName = itemView.findViewById(R.id.tv_venue_name);
            tvVenueLocation = itemView.findViewById(R.id.tv_venue_location);
            tvVenueCapacity = itemView.findViewById(R.id.tv_venue_capacity);
            tvTagType = itemView.findViewById(R.id.tv_tag_type);
            tvTagAmenities = itemView.findViewById(R.id.tv_tag_amenities);
            btnEditVenue = itemView.findViewById(R.id.btn_edit_venue);
            btnDeleteVenue = itemView.findViewById(R.id.btn_delete_venue);

            // (We will add click listeners for edit/delete here)
        }
    }
}