package com.example.venueease;

public class FilterCriteria {
    public static final int ANY_CAPACITY = -1;
    public static final double ANY_PRICE = -1.0;

    private String date;
    private String venueType;
    private int minCapacity;
    private double maxPrice;

    public FilterCriteria(String date, String venueType, int minCapacity, double maxPrice) {
        this.date = date;
        this.venueType = venueType;
        this.minCapacity = minCapacity;
        this.maxPrice = maxPrice;
    }

    // Getters
    public String getDate() { return date; }
    public String getVenueType() { return venueType; }
    public int getMinCapacity() { return minCapacity; }
    public double getMaxPrice() { return maxPrice; }
}