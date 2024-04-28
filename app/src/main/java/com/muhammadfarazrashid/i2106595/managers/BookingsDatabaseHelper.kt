package com.muhammadfarazrashid.i2106595.managers


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.muhammadfarazrashid.i2106595.Session
import com.muhammadfarazrashid.i2106595.UserManager

class BookingsDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "BookingsDatabase"
        private const val TABLE_BOOKINGS = "bookings"
        private const val KEY_ID = "id"
        private const val KEY_USER_ID = "userId"
        private const val KEY_MENTOR_ID = "mentorId"
        private const val KEY_BOOKING_DATE = "bookingDate"
        private const val KEY_BOOKING_TIME = "bookingTime"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_BOOKINGS_TABLE = ("CREATE TABLE " + TABLE_BOOKINGS + "("
                + KEY_ID + "VARCHAR(100) PRIMARY KEY," + KEY_USER_ID + " VARCHAR(100),"
                + KEY_MENTOR_ID + " VARCHAR(100)," + KEY_BOOKING_DATE + " VARCHAR(50),"
                + KEY_BOOKING_TIME + " VARCHAR(50)" + ")")
        db.execSQL(CREATE_BOOKINGS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKINGS")
        onCreate(db)
    }

    //give me add booking function with this signature     fun addBooking(userId: String, mentorId: String, bookingDate: String, bookingTime: String) {

    fun addBooking(userId: String, mentorId: String, bookingDate: String, bookingTime: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_USER_ID, userId)
        values.put(KEY_MENTOR_ID, mentorId)
        values.put(KEY_BOOKING_DATE, bookingDate)
        values.put(KEY_BOOKING_TIME, bookingTime)
        db.insert(TABLE_BOOKINGS, null, values)
        db.close()
    }

    @SuppressLint("Range")
    fun getBookings(userId: String): MutableList<Session> {
        val bookings = mutableListOf<Session>()
        val selectQuery = "SELECT  * FROM $TABLE_BOOKINGS WHERE $KEY_USER_ID = ?"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(userId))
        if (cursor.moveToFirst()) {
            do {
                val booking = Session(
                    cursor.getString(cursor.getColumnIndex(KEY_BOOKING_DATE)),
                    cursor.getString(cursor.getColumnIndex(KEY_BOOKING_TIME)),
                    cursor.getString(cursor.getColumnIndex(KEY_MENTOR_ID))
                )
                bookings.add(booking)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bookings
    }
}