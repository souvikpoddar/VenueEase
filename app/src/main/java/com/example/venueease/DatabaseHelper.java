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

    // --- NEW: Bookings Table ---
    public static final String TABLE_BOOKINGS = "Bookings";
    public static final String KEY_BOOKING_ID = "booking_id";
    public static final String KEY_B_VENUE_ID = "venue_id"; // Foreign key to Venues table
    public static final String KEY_B_USER_ID = "user_id"; // Foreign key to Users table (we'll add later)
    public static final String KEY_USER_NAME = "user_name"; // e.g., "John Doe"
    public static final String KEY_USER_EMAIL = "user_email"; // e.g., "john@example.com"
    public static final String KEY_EVENT_DATE = "event_date"; // e.g., "Sun, Dec 15, 2024"
    public static final String KEY_START_TIME = "start_time"; // e.g., "09:00"
    public static final String KEY_END_TIME = "end_time"; // e.g., "17:00"
    public static final String KEY_EVENT_TYPE = "event_type"; // e.g., "Corporate Seminar"
    public static final String KEY_TOTAL_PRICE = "total_price"; // e.g., 1200
    public static final String KEY_SPECIAL_REQUESTS = "special_requests"; // e.g., "Need additional mics"
    public static final String KEY_BOOKING_STATUS = "booking_status"; // "Pending", "Approved", "Rejected", "Confirmed"
    public static final String KEY_SUBMITTED_DATE = "submitted_date"; // e.g., "1/12/2024"

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

    // --- NEW: Create Bookings Table Query ---
    private static final String CREATE_TABLE_BOOKINGS =
            "CREATE TABLE " + TABLE_BOOKINGS + "("
                    + KEY_BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_B_VENUE_ID + " INTEGER," // We'll link this to the Venues table
                    + KEY_B_USER_ID + " INTEGER," // We'll link this to a Users table
                    + KEY_USER_NAME + " TEXT,"
                    + KEY_USER_EMAIL + " TEXT,"
                    + KEY_EVENT_DATE + " TEXT,"
                    + KEY_START_TIME + " TEXT,"
                    + KEY_END_TIME + " TEXT,"
                    + KEY_EVENT_TYPE + " TEXT,"
                    + KEY_TOTAL_PRICE + " REAL,"
                    + KEY_SPECIAL_REQUESTS + " TEXT,"
                    + KEY_BOOKING_STATUS + " TEXT,"
                    + KEY_SUBMITTED_DATE + " TEXT"
                    // + ", FOREIGN KEY(" + KEY_B_VENUE_ID + ") REFERENCES " + TABLE_VENUES + "(" + KEY_VENUE_ID + ")"
                    // We'll add real foreign keys later if we build the Users table
                    + ")";
    // --- END: Create Bookings Table Query ---

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_VENUES);
        db.execSQL(CREATE_TABLE_BOOKINGS);
        // You could also create a Users table here if needed
    }

    // Called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VENUES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
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

    // ... (inside DatabaseHelper.java)

    /**
     * Get all venues that match a search query and filter criteria
     */
    public List<Venue> getFilteredVenues(String nameOrLocationQuery, FilterCriteria criteria) {
        List<Venue> venueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Base query
        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_VENUES + " WHERE 1=1");
        List<String> selectionArgs = new ArrayList<>();

        // Add search query for EITHER name OR location
        if (nameOrLocationQuery != null && !nameOrLocationQuery.isEmpty()) {
            query.append(" AND (")
                    .append(KEY_VENUE_NAME).append(" LIKE ? OR ")
                    .append(KEY_LOCATION).append(" LIKE ?)");

            // Add the argument twice, once for name and once for location
            selectionArgs.add("%" + nameOrLocationQuery + "%");
            selectionArgs.add("%" + nameOrLocationQuery + "%");
        }

        // Add filter criteria
        if (criteria != null) {
            if (criteria.getVenueType() != null) {
                query.append(" AND ").append(KEY_VENUE_TYPE).append(" = ?");
                selectionArgs.add(criteria.getVenueType());
            }
            if (criteria.getMinCapacity() != FilterCriteria.ANY_CAPACITY) {
                query.append(" AND ").append(KEY_CAPACITY).append(" >= ?");
                selectionArgs.add(String.valueOf(criteria.getMinCapacity()));
            }
            if (criteria.getMaxPrice() != FilterCriteria.ANY_PRICE) {
                query.append(" AND ").append(KEY_PRICE_PER_HOUR).append(" <= ?");
                selectionArgs.add(String.valueOf(criteria.getMaxPrice()));
            }

            // TODO: Implement Date filter logic
            // This requires a check against the 'Bookings' table to see if a venue
            // is available on that date. We will add this logic later.
            if (criteria.getDate() != null) {
                Log.w("DatabaseHelper", "Date filter is not yet implemented.");
            }
        }

        // Convert List<String> to String[]
        String[] args = new String[selectionArgs.size()];
        selectionArgs.toArray(args);

        // Execute query
        Cursor cursor = db.rawQuery(query.toString(), args);

        // Loop through all rows and add to list
        if (cursor.moveToFirst()) {
            do {
                Venue venue = new Venue();
                // (Set all properties: id, name, location, etc.)
                venue.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VENUE_ID)));
                venue.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VENUE_NAME)));
                venue.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION)));
                venue.setCapacity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CAPACITY)));
                venue.setType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VENUE_TYPE)));
                venue.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PRICE_PER_HOUR)));
                venue.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
                venue.setAmenities(cursor.getString(cursor.getColumnIndexOrThrow(KEY_AMENITIES)));
                venue.setPhotoUri(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VENUE_PHOTOS)));
                venueList.add(venue);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return venueList;
    }

    public int updateVenue(Venue venue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Put all the new values
        values.put(KEY_VENUE_NAME, venue.getName());
        values.put(KEY_LOCATION, venue.getLocation());
        values.put(KEY_CAPACITY, venue.getCapacity());
        values.put(KEY_VENUE_TYPE, venue.getType());
        values.put(KEY_PRICE_PER_HOUR, venue.getPrice());
        values.put(KEY_DESCRIPTION, venue.getDescription());
        values.put(KEY_AMENITIES, venue.getAmenities());
        values.put(KEY_VENUE_PHOTOS, venue.getPhotoUri());

        // Update the row where the ID matches
        int rowsAffected = db.update(
                TABLE_VENUES,
                values,
                KEY_VENUE_ID + " = ?", // The WHERE clause
                new String[]{String.valueOf(venue.getId())} // The argument for the WHERE clause
        );

        db.close();
        return rowsAffected;
    }

    public int deleteVenue(int venueId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete the row where the ID matches
        int rowsAffected = db.delete(
                TABLE_VENUES,
                KEY_VENUE_ID + " = ?", // The WHERE clause
                new String[]{String.valueOf(venueId)} // The argument for the WHERE clause
        );

        db.close();
        return rowsAffected;
    }

    // We will add getVenueDetails(), updateVenue(), and deleteVenue() methods here later.
}