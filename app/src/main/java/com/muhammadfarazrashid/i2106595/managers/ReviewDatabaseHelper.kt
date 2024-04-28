package com.muhammadfarazrashid.i2106595.managers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.muhammadfarazrashid.i2106595.ReviewItem
import java.util.UUID

class ReviewDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "reviews.db"
        private const val DATABASE_VERSION = 1

        // Table name
        private const val TABLE_REVIEWS = "reviews"

        // Column names
        private const val COLUMN_ID = "id"
        private const val COLUMN_USER_ID = "userId"
        private const val COLUMN_MENTOR_NAME = "mentorName"
        private const val COLUMN_RATING = "rating"
        private const val COLUMN_REVIEW_TEXT = "reviewText"

        // Create table query
        private const val CREATE_TABLE_REVIEWS =
            "CREATE TABLE $TABLE_REVIEWS (" +
                    "$COLUMN_ID VARCHAR(100) PRIMARY KEY, " +
                    "$COLUMN_USER_ID VARCHAR(100), " +
                    "$COLUMN_MENTOR_NAME VARCHAR(100), " +
                    "$COLUMN_RATING FLOAT, " +
                    "$COLUMN_REVIEW_TEXT VARCHAR(200)" +
                    ")"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create tables
        db.execSQL(CREATE_TABLE_REVIEWS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REVIEWS")

        // Create tables again
        onCreate(db)
    }
    fun addReview(userId: String, mentorName: String, rating: Float, reviewText: String) {
        val db = this.writableDatabase
        var id = UUID.randomUUID().toString()
        val contentValues = ContentValues().apply {
            put(COLUMN_ID, id)
            put(COLUMN_USER_ID, userId)
            put(COLUMN_MENTOR_NAME, mentorName)
            put(COLUMN_RATING, rating)
            put(COLUMN_REVIEW_TEXT, reviewText)
        }
        db.insert(TABLE_REVIEWS, null, contentValues)
        db.close()
    }

    //fetch reviews with this signature     fun getReviews(userId: String, callback: (MutableList<ReviewItem>?) -> Unit){

    fun getReviews(userId: String, callback: (MutableList<ReviewItem>?) -> Unit) {
        val reviews = mutableListOf<ReviewItem>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_REVIEWS,
            arrayOf(COLUMN_ID, COLUMN_USER_ID, COLUMN_MENTOR_NAME, COLUMN_RATING, COLUMN_REVIEW_TEXT),
            "$COLUMN_USER_ID = ?",
            arrayOf(userId),
            null,
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val review = ReviewItem(
                    getString(getColumnIndexOrThrow(COLUMN_MENTOR_NAME)),
                    getString(getColumnIndexOrThrow(COLUMN_REVIEW_TEXT)),
                    getFloat(getColumnIndexOrThrow(COLUMN_RATING))
                )
                reviews.add(review)
            }
        }

        callback(reviews)
    }

}