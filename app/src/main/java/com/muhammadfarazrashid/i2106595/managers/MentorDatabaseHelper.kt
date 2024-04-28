package com.muhammadfarazrashid.i2106595.managers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.muhammadfarazrashid.i2106595.Mentor

class MentorDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "MentorDatabase"
        private const val TABLE_MENTORS = "Mentors"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_POSITION = "position"
        private const val KEY_AVAILABILITY = "availability"
        private const val KEY_SALARY = "salary"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_PROFILE_PICTURE_URL = "profilePictureUrl"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_MENTORS_TABLE = ("CREATE TABLE " + TABLE_MENTORS + "("
                + KEY_ID + " TEXT PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_POSITION + " TEXT," + KEY_AVAILABILITY + " TEXT,"
                + KEY_SALARY + " TEXT," + KEY_DESCRIPTION + " TEXT,"
                + KEY_PROFILE_PICTURE_URL + " TEXT" + ")")
        db.execSQL(CREATE_MENTORS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MENTORS")
        onCreate(db)
    }

    fun addMentor(mentor: Mentor): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, mentor.id)
        contentValues.put(KEY_NAME, mentor.name)
        contentValues.put(KEY_POSITION, mentor.position)
        contentValues.put(KEY_AVAILABILITY, mentor.availability)
        contentValues.put(KEY_SALARY, mentor.salary)
        contentValues.put(KEY_DESCRIPTION, mentor.description)
        contentValues.put(KEY_PROFILE_PICTURE_URL, mentor.getprofilePictureUrl())
        val success = db.insert(TABLE_MENTORS, null, contentValues)
        db.close()
        return success
    }

    fun getMentor(id: String): Mentor? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_MENTORS, arrayOf(KEY_ID, KEY_NAME, KEY_POSITION, KEY_AVAILABILITY, KEY_SALARY, KEY_DESCRIPTION, KEY_PROFILE_PICTURE_URL),
            "$KEY_ID=?", arrayOf(id), null, null, null, null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val mentor = Mentor(
                cursor.getString(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6)
            )
            cursor.close()
            return mentor
        }
        return null
    }

    fun getAllMentors(): List<Mentor> {
        val mentors = ArrayList<Mentor>()
        val selectQuery = "SELECT  * FROM $TABLE_MENTORS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val mentor = Mentor(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6)
                )
                mentors.add(mentor)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return mentors
    }

    fun updateMentor(mentor: Mentor): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_NAME, mentor.name)
        contentValues.put(KEY_POSITION, mentor.position)
        contentValues.put(KEY_AVAILABILITY, mentor.availability)
        contentValues.put(KEY_SALARY, mentor.salary)
        contentValues.put(KEY_DESCRIPTION, mentor.description)
        contentValues.put(KEY_PROFILE_PICTURE_URL, mentor.getprofilePictureUrl())
        return db.update(TABLE_MENTORS, contentValues, "$KEY_ID = ?", arrayOf(mentor.id))
    }

    fun deleteMentor(id: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_MENTORS, "$KEY_ID = ?", arrayOf(id))
    }
}