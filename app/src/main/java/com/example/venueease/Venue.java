package com.example.venueease;

import java.io.Serializable;

public class Venue implements Serializable {

    int id;
    String name;
    String location;
    int capacity;
    String type;
    double price;
    String description;
    String amenities;
    String photoUri;

    // You can create an empty constructor
    public Venue() {
    }

    // And a constructor with all the fields
    public Venue(int id, String name, String location, int capacity, String type,
                 double price, String description, String amenities, String photoUri) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.type = type;
        this.price = price;
        this.description = description;
        this.amenities = amenities;
        this.photoUri = photoUri;
    }

    // --- Getters and Setters for all fields ---
    // (You can generate these in Android Studio: Right Click -> Generate -> Getters and Setters)

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
    public String getPhotoUri() { return photoUri; }
    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; }
}