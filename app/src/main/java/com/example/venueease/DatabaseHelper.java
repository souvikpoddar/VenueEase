package com.example.venueease;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    public static final String TABLE_RATINGS = "Ratings";
    public static final String KEY_RATING_ID = "rating_id";
    public static final String KEY_R_VENUE_ID = "venue_id"; // Foreign key to Venues
    public static final String KEY_R_BOOKING_ID = "booking_id"; // Foreign key to Bookings
    public static final String KEY_R_USER_ID = "user_id"; // Foreign key to Users
    public static final String KEY_RATING_VALUE = "rating_value"; // Float (1.0 to 5.0)
    public static final String KEY_RATING_COMMENT = "rating_comment"; // Text
    public static final String KEY_RATING_DATE = "rating_date"; // Text

    // --- Notifications Table ---
    public static final String TABLE_NOTIFICATIONS = "Notifications";
    public static final String KEY_NOTIFICATION_ID = "notification_id";
    public static final String KEY_N_USER_EMAIL = "user_email"; // Email of the user this notification is for (or "admin")
    public static final String KEY_NOTIFICATION_TYPE = "notification_type"; // e.g., "NEW_BOOKING", "BOOKING_APPROVED", "PAYMENT_RECEIVED", "RATING_SUBMITTED"
    public static final String KEY_NOTIFICATION_TITLE = "notification_title";
    public static final String KEY_NOTIFICATION_MESSAGE = "notification_message";
    public static final String KEY_N_BOOKING_ID = "booking_id"; // Optional: Link to relevant booking
    public static final String KEY_N_VENUE_ID = "venue_id"; // Optional: Link to relevant venue
    public static final String KEY_IS_READ = "is_read"; // INTEGER (0 = false, 1 = true)
    public static final String KEY_NOTIFICATION_TIMESTAMP = "notification_timestamp"; // TEXT (e.g., ISO 8601 format or similar for sorting)

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

    private static final String CREATE_TABLE_RATINGS =
            "CREATE TABLE " + TABLE_RATINGS + "("
                    + KEY_RATING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_R_VENUE_ID + " INTEGER,"
                    + KEY_R_BOOKING_ID + " INTEGER,"
                    + KEY_R_USER_ID + " INTEGER,"
                    + KEY_RATING_VALUE + " REAL,"
                    + KEY_RATING_COMMENT + " TEXT,"
                    + KEY_RATING_DATE + " TEXT"
                    // + ", FOREIGN KEY(" + KEY_R_VENUE_ID + ") REFERENCES " + TABLE_VENUES + "(" + KEY_VENUE_ID + ")"
                    // + ", FOREIGN KEY(" + KEY_R_BOOKING_ID + ") REFERENCES " + TABLE_BOOKINGS + "(" + KEY_BOOKING_ID + ")"
                    + ")";

    private static final String CREATE_TABLE_NOTIFICATIONS =
            "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                    + KEY_NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_N_USER_EMAIL + " TEXT," // Target user or "admin"
                    + KEY_NOTIFICATION_TYPE + " TEXT,"
                    + KEY_NOTIFICATION_TITLE + " TEXT,"
                    + KEY_NOTIFICATION_MESSAGE + " TEXT,"
                    + KEY_N_BOOKING_ID + " INTEGER,"
                    + KEY_N_VENUE_ID + " INTEGER,"
                    + KEY_IS_READ + " INTEGER DEFAULT 0,"
                    + KEY_NOTIFICATION_TIMESTAMP + " TEXT"
                    + ")";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_VENUES);
        db.execSQL(CREATE_TABLE_BOOKINGS);
        db.execSQL(CREATE_TABLE_RATINGS);
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);
        // You could also create a Users table here if needed
    }

    // Called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VENUES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RATINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
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

    /**
     * ----------------------------------------------------------------
     * "Bookings" table methods
     * ----------------------------------------------------------------
     */

    /**
     * Fetches bookings that match a specific status and/or date.
     * We also join with the Venues table to get venue details.
     */
    public List<Booking> getBookings(String userEmail, String status, String date) {
        List<Booking> bookingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Start base query with JOIN
        String query = "SELECT b.*, v." + KEY_VENUE_NAME + ", v." + KEY_LOCATION + ", v." + KEY_CAPACITY +
                " FROM " + TABLE_BOOKINGS + " b" +
                " LEFT JOIN " + TABLE_VENUES + " v ON b." + KEY_B_VENUE_ID + " = v." + KEY_VENUE_ID +
                " WHERE 1=1"; // Start with a condition that's always true

        List<String> selectionArgs = new ArrayList<>();

        // Add user email filter ONLY if it's provided
        if (userEmail != null && !userEmail.isEmpty()) {
            query += " AND b." + KEY_USER_EMAIL + " = ?";
            selectionArgs.add(userEmail);
        }

        // Add optional status and date filters (these remain the same)
        if (status != null && !status.isEmpty()) {
            query += " AND b." + KEY_BOOKING_STATUS + " = ?";
            selectionArgs.add(status);
        }
        if (date != null && !date.isEmpty()) {
            query += " AND b." + KEY_EVENT_DATE + " = ?";
            selectionArgs.add(date);
        }

        query += " ORDER BY b." + KEY_BOOKING_ID + " DESC";

        // Convert List<String> to String[] for rawQuery
        String[] args = selectionArgs.toArray(new String[0]);

        Cursor cursor = db.rawQuery(query.toString(), args);

        // --- Cursor processing remains the same ---
        if (cursor.moveToFirst()) {
            do {
                Booking booking = new Booking();
                // ... (Set all booking properties: bookingId, venueId, userName, etc.) ...
                booking.setBookingId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BOOKING_ID)));
                booking.setVenueId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_B_VENUE_ID)));
                booking.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_B_USER_ID)));
                booking.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME)));
                booking.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL)));
                booking.setEventDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EVENT_DATE)));
                booking.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_START_TIME)));
                booking.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_END_TIME)));
                booking.setEventType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EVENT_TYPE)));
                booking.setTotalPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_TOTAL_PRICE)));
                booking.setSpecialRequests(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SPECIAL_REQUESTS)));
                booking.setBookingStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKING_STATUS)));
                booking.setSubmittedDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBMITTED_DATE)));


                // ... (Set joined Venue properties, handling nulls) ...
                int venueNameCol = cursor.getColumnIndex(KEY_VENUE_NAME);
                int locationCol = cursor.getColumnIndex(KEY_LOCATION);
                int capacityCol = cursor.getColumnIndex(KEY_CAPACITY);

                if (venueNameCol != -1 && !cursor.isNull(venueNameCol)) {
                    Venue venue = new Venue();
                    venue.setId(booking.getVenueId());
                    venue.setName(cursor.getString(venueNameCol));
                    venue.setLocation(cursor.getString(locationCol));
                    venue.setCapacity(cursor.getInt(capacityCol));
                    booking.setVenue(venue);
                } else {
                    booking.setVenue(null);
                }


                bookingList.add(booking);
            } while (cursor.moveToNext());
        }
        // --- End cursor processing ---

        cursor.close();
        db.close();
        return bookingList;
    }

    /**
     * Updates the status of a specific booking.
     */
    public boolean updateBookingStatus(int bookingId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BOOKING_STATUS, newStatus);

        int rowsAffected = db.update(
                TABLE_BOOKINGS,
                values,
                KEY_BOOKING_ID + " = ?",
                new String[]{String.valueOf(bookingId)}
        );
        db.close();
        return rowsAffected > 0;
    }

    /**
     * Gets the count of bookings for a specific status.
     * If status is null, gets the total count.
     */
    public int getBookingCount(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_BOOKINGS;
        String[] selectionArgs = null;

        if (status != null && !status.isEmpty()) {
            query += " WHERE " + KEY_BOOKING_STATUS + " = ?";
            selectionArgs = new String[]{status};
        }

        Cursor cursor = db.rawQuery(query, selectionArgs);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public boolean addBooking(int venueId, int userId, String userName, String userEmail,
                              String eventDate, String startTime, String endTime,
                              String eventType, double totalPrice, String specialRequests,
                              String submittedDate) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_B_VENUE_ID, venueId);
        values.put(KEY_B_USER_ID, userId); // Assuming userId is available, else use 0 or fetch later
        values.put(KEY_USER_NAME, userName);
        values.put(KEY_USER_EMAIL, userEmail);
        values.put(KEY_EVENT_DATE, eventDate);
        values.put(KEY_START_TIME, startTime);
        values.put(KEY_END_TIME, endTime);
        values.put(KEY_EVENT_TYPE, eventType);
        values.put(KEY_TOTAL_PRICE, totalPrice);
        values.put(KEY_SPECIAL_REQUESTS, specialRequests);
        values.put(KEY_BOOKING_STATUS, BookingsAdapter.STATUS_PENDING); // Default status
        values.put(KEY_SUBMITTED_DATE, submittedDate);

        long result = db.insert(TABLE_BOOKINGS, null, values);
        db.close();
        return result != -1;
    }

    // --- We need a way to add test data ---
    // --- Add this method temporarily to test ---
    public void addTestData() {
        if (getBookingCount(null) == 0) { // Only add if table is empty
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_B_VENUE_ID, 1);
            values.put(KEY_USER_NAME, "John Doe");
            values.put(KEY_USER_EMAIL, "john@example.com");
            values.put(KEY_EVENT_DATE, "Sun, Dec 15, 2024");
            values.put(KEY_START_TIME, "09:00");
            values.put(KEY_END_TIME, "17:00");
            values.put(KEY_EVENT_TYPE, "Corporate Seminar");
            values.put(KEY_TOTAL_PRICE, 1200);
            values.put(KEY_SPECIAL_REQUESTS, "Need additional microphones");
            values.put(KEY_BOOKING_STATUS, BookingsAdapter.STATUS_APPROVED);
            values.put(KEY_SUBMITTED_DATE, "1/12/2024");
            db.insert(TABLE_BOOKINGS, null, values);

            values.clear();
            values.put(KEY_B_VENUE_ID, 2);
            values.put(KEY_USER_NAME, "Jane Smith");
            values.put(KEY_USER_EMAIL, "jane@example.com");
            values.put(KEY_EVENT_DATE, "Fri, Dec 20, 2024");
            values.put(KEY_START_TIME, "18:00");
            values.put(KEY_END_TIME, "23:00");
            values.put(KEY_EVENT_TYPE, "Wedding Reception");
            values.put(KEY_TOTAL_PRICE, 1000);
            values.put(KEY_SPECIAL_REQUESTS, "Floral decoration required");
            values.put(KEY_BOOKING_STATUS, BookingsAdapter.STATUS_PENDING);
            values.put(KEY_SUBMITTED_DATE, "2/12/2024");
            db.insert(TABLE_BOOKINGS, null, values);

            db.close();
        }
    }

    public boolean addRating(int venueId, int bookingId, int userId, float rating, String comment, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_R_VENUE_ID, venueId);
        values.put(KEY_R_BOOKING_ID, bookingId);
        values.put(KEY_R_USER_ID, userId);
        values.put(KEY_RATING_VALUE, rating);
        values.put(KEY_RATING_COMMENT, comment);
        values.put(KEY_RATING_DATE, date);

        long result = db.insert(TABLE_RATINGS, null, values);
        db.close();
        return result != -1;
    }

    public double getAverageRating(int venueId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT AVG(" + KEY_RATING_VALUE + ") FROM " + TABLE_RATINGS +
                " WHERE " + KEY_R_VENUE_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(venueId)});
        double average = 0.0;
        if (cursor.moveToFirst()) {
            average = cursor.getDouble(0); // AVG result is in the first column
        }
        cursor.close();
        db.close();
        return average;
    }

    public int getRatingCount(int venueId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_RATINGS +
                " WHERE " + KEY_R_VENUE_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(venueId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    /**
     * Adds a new notification to the database.
     */
    public boolean addNotification(String userEmailOrAdmin, String type, String title, String message, int bookingId, int venueId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = sdf.format(new Date()); // Use current time

        values.put(KEY_N_USER_EMAIL, userEmailOrAdmin); // "admin" or user's email
        values.put(KEY_NOTIFICATION_TYPE, type);
        values.put(KEY_NOTIFICATION_TITLE, title);
        values.put(KEY_NOTIFICATION_MESSAGE, message);
        if (bookingId > 0) values.put(KEY_N_BOOKING_ID, bookingId); // Only add if valid
        if (venueId > 0) values.put(KEY_N_VENUE_ID, venueId);     // Only add if valid
        values.put(KEY_IS_READ, 0); // Default to unread
        values.put(KEY_NOTIFICATION_TIMESTAMP, timestamp);

        long result = db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();
        return result != -1;
    }

    /**
     * Gets notifications for a specific user (or admin), optionally filtering by read status.
     */
    public List<Notification> getNotifications(String userEmailOrAdmin, boolean unreadOnly) {
        List<Notification> notificationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NOTIFICATIONS + " WHERE " + KEY_N_USER_EMAIL + " = ?";
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(userEmailOrAdmin);

        if (unreadOnly) {
            query += " AND " + KEY_IS_READ + " = 0";
        }

        query += " ORDER BY " + KEY_NOTIFICATION_TIMESTAMP + " DESC"; // Show newest first

        Cursor cursor = db.rawQuery(query, selectionArgs.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                Notification notification = new Notification();
                notification.setNotificationId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTIFICATION_ID)));
                notification.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_N_USER_EMAIL)));
                notification.setNotificationType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTIFICATION_TYPE)));
                notification.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTIFICATION_TITLE)));
                notification.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTIFICATION_MESSAGE)));
                notification.setBookingId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_N_BOOKING_ID)));
                notification.setVenueId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_N_VENUE_ID)));
                notification.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_READ)) == 1);
                notification.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTIFICATION_TIMESTAMP)));
                notificationList.add(notification);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notificationList;
    }

    /**
     * Updates the read status of a single notification.
     */
    public boolean updateNotificationReadStatus(int notificationId, boolean isRead) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_IS_READ, isRead ? 1 : 0);

        int rowsAffected = db.update(
                TABLE_NOTIFICATIONS,
                values,
                KEY_NOTIFICATION_ID + " = ?",
                new String[]{String.valueOf(notificationId)}
        );
        db.close();
        return rowsAffected > 0;
    }

    /**
     * Marks all notifications for a user/admin as read.
     */
    public int markAllNotificationsAsRead(String userEmailOrAdmin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_IS_READ, 1); // Mark as read

        int rowsAffected = db.update(
                TABLE_NOTIFICATIONS,
                values,
                KEY_N_USER_EMAIL + " = ? AND " + KEY_IS_READ + " = 0", // Only update unread ones
                new String[]{userEmailOrAdmin}
        );
        db.close();
        return rowsAffected;
    }

    /**
     * Deletes a single notification by its ID.
     */
    public boolean deleteNotification(int notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(
                TABLE_NOTIFICATIONS,
                KEY_NOTIFICATION_ID + " = ?",
                new String[]{String.valueOf(notificationId)}
        );
        db.close();
        return rowsAffected > 0;
    }
}