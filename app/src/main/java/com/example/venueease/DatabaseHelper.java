package com.example.venueease;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    public static final String DATABASE_NAME = "VenueBooking.db";
    public static final int DATABASE_VERSION = 1;

    // Table Name
    public static final String TABLE_VENUES = "Venues";

    // Venues Table Columns
    public static final String KEY_VENUE_ID = "venue_id";
    public static final String KEY_VENUE_NAME = "venue_name";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_CAPACITY = "capacity";
    public static final String KEY_VENUE_TYPE = "venue_type";
    public static final String KEY_PRICE_PER_HOUR = "price_per_hour";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_AMENITIES = "amenities";
    public static final String KEY_VENUE_PHOTOS = "venue_photos"; // Storing as URI string

    // Create table SQL query
    private static final String CREATE_TABLE_VENUES =
            "CREATE TABLE " + TABLE_VENUES + "("
                    + KEY_VENUE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_VENUE_NAME + " TEXT,"
                    + KEY_LOCATION + " TEXT,"
                    + KEY_CAPACITY + " INTEGER,"
                    + KEY_VENUE_TYPE + " TEXT,"
                    + KEY_PRICE_PER_HOUR + " REAL,"
                    + KEY_DESCRIPTION + " TEXT,"
                    + KEY_AMENITIES + " TEXT,"
                    + KEY_VENUE_PHOTOS + " TEXT"
                    + ")";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_VENUES);
        // You could also create a Users table here if needed
    }

    // Called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VENUES);
        // Create tables again
        onCreate(db);
    }

    /**
     * ----------------------------------------------------------------
     * "Venues" table methods (CRUD)
     * ----------------------------------------------------------------
     */

    /**
     * Add a new venue to the database
     * We'll need a Venue model class for this to be cleaner, but for now
     * we can pass all parameters.
     *
     * @return true if insertion is successful, false otherwise
     */
    public boolean addVenue(String name, String location, int capacity, String type,
                            double price, String description, String amenities, String photoUri) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Put values into the ContentValues object
        values.put(KEY_VENUE_NAME, name);
        values.put(KEY_LOCATION, location);
        values.put(KEY_CAPACITY, capacity);
        values.put(KEY_VENUE_TYPE, type);
        values.put(KEY_PRICE_PER_HOUR, price);
        values.put(KEY_DESCRIPTION, description);
        values.put(KEY_AMENITIES, amenities);
        values.put(KEY_VENUE_PHOTOS, photoUri);

        // Insert row
        long result = db.insert(TABLE_VENUES, null, values);
        db.close(); // Close database connection

        // Check if insertion was successful
        // result == -1 means an error occurred
        return result != -1;
    }

    public List<Venue> getAllVenuesList() {
        List<Venue> venueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to select all venues
        String selectQuery = "SELECT * FROM " + TABLE_VENUES;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Loop through all rows and add to list
        if (cursor.moveToFirst()) {
            do {
                Venue venue = new Venue();
                venue.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VENUE_ID)));
                venue.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VENUE_NAME)));
                venue.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION)));
                venue.setCapacity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CAPACITY)));
                venue.setType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VENUE_TYPE)));
                venue.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PRICE_PER_HOUR)));
                venue.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
                venue.setAmenities(cursor.getString(cursor.getColumnIndexOrThrow(KEY_AMENITIES)));
                venue.setPhotoUri(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VENUE_PHOTOS)));

                // Add venue to list
                venueList.add(venue);
            } while (cursor.moveToNext());
        }

        // Close the cursor and database
        cursor.close();
        db.close();

        // Return the list
        return venueList;
    }

    // We will add getVenueDetails(), updateVenue(), and deleteVenue() methods here later.
}