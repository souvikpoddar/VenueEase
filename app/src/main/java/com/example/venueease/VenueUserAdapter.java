package com.example.venueease;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class VenueUserAdapter extends RecyclerView.Adapter<VenueUserAdapter.VenueUserViewHolder> {

    private Context context;
    private List<Venue> venueList;

    // Interface for button clicks
    public interface OnVenueUserActionListener {
        void onViewDetailsClicked(Venue venue);
        void onBookNowClicked(Venue venue);
    }
    private final OnVenueUserActionListener actionListener;

    public VenueUserAdapter(Context context, List<Venue> venueList, OnVenueUserActionListener listener) {
        this.context = context;
        this.venueList = venueList;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public VenueUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_venue_user, parent, false);
        return new VenueUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VenueUserViewHolder holder, int position) {
        Venue venue = venueList.get(position);
        holder.bind(venue, actionListener);
    }

    @Override
    public int getItemCount() {
        return venueList.size();
    }

    // Method to update the list
    public void updateVenues(List<Venue> newVenueList) {
        this.venueList.clear();
        this.venueList.addAll(newVenueList);
        notifyDataSetChanged();
    }

    public class VenueUserViewHolder extends RecyclerView.ViewHolder {

        // Declare all views
        ImageView ivVenueImage;
        TextView tvVenuePrice, tvVenueAvailable, tvVenueName, tvVenueLocation,
                tvVenueCapacity, tvTagType, tvAmenitiesMore;
        LinearLayout llAmenitiesContainer;
        MaterialButton btnViewDetails, btnBookNow;

        public VenueUserViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find all views
            ivVenueImage = itemView.findViewById(R.id.iv_venue_image);
            tvVenuePrice = itemView.findViewById(R.id.tv_venue_price);
            tvVenueAvailable = itemView.findViewById(R.id.tv_venue_available);
            tvVenueName = itemView.findViewById(R.id.tv_venue_name);
            tvVenueLocation = itemView.findViewById(R.id.tv_venue_location);
            tvVenueCapacity = itemView.findViewById(R.id.tv_venue_capacity);
            tvTagType = itemView.findViewById(R.id.tv_tag_type);
            tvAmenitiesMore = itemView.findViewById(R.id.tv_amenities_more);
            llAmenitiesContainer = itemView.findViewById(R.id.ll_amenities_container);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnBookNow = itemView.findViewById(R.id.btn_book_now);
        }

        void bind(Venue venue, OnVenueUserActionListener listener) {
            // Bind basic data
            tvVenueName.setText(venue.getName());
            tvVenueLocation.setText(venue.getLocation());
            tvVenueCapacity.setText(String.format(Locale.getDefault(), "Up to %d guests", venue.getCapacity()));
            tvTagType.setText(venue.getType());
            tvVenuePrice.setText(String.format(Locale.getDefault(), "₹%.0f/hr", venue.getPrice()));

            // Load image
            String photoUriString = venue.getPhotoUri();
            if (photoUriString != null && !photoUriString.isEmpty()) {
                ivVenueImage.setImageURI(Uri.parse(photoUriString));
            } else {
                ivVenueImage.setImageResource(android.R.drawable.ic_menu_gallery); // Placeholder
            }

            tvVenueAvailable.setVisibility(View.VISIBLE);

            addAmenities(venue.getAmenities());

            btnViewDetails.setOnClickListener(v -> listener.onViewDetailsClicked(venue));
            btnBookNow.setOnClickListener(v -> listener.onBookNowClicked(venue));
        }

        private void addAmenities(String amenitiesString) {
            llAmenitiesContainer.removeAllViews();

            if (amenitiesString == null || amenitiesString.isEmpty()) {
                tvAmenitiesMore.setVisibility(View.GONE);
                return;
            }

            String[] amenities = amenitiesString.split(",");
            int maxAmenitiesToShow = 3;

            for (int i = 0; i < amenities.length; i++) {
                if (i < maxAmenitiesToShow) {
                    // Create a new TextView for the amenity
                    TextView amenityView = new TextView(context);
                    amenityView.setText(amenities[i].trim());
                    amenityView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_amenity, 0, 0, 0);
                    amenityView.setCompoundDrawablePadding(8); // 8dp padding

                    // Set layout parameters to add margins
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMarginEnd(16); // 16dp margin
                    amenityView.setLayoutParams(params);

                    llAmenitiesContainer.addView(amenityView);
                }
            }

            // Show "+x more" text if there are more amenities
            if (amenities.length > maxAmenitiesToShow) {
                int remaining = amenities.length - maxAmenitiesToShow;
                tvAmenitiesMore.setText(String.format(Locale.getDefault(), "+%d more", remaining));
                tvAmenitiesMore.setVisibility(View.VISIBLE);
                llAmenitiesContainer.addView(tvAmenitiesMore); // Add it to the container
            } else {
                tvAmenitiesMore.setVisibility(View.GONE);
            }
        }
    }
}