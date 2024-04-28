package com.muhammadfarazrashid.i2106595.managers


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.muhammadfarazrashid.i2106595.AllMessagesChat
import com.muhammadfarazrashid.i2106595.Mentor
import com.muhammadfarazrashid.i2106595.MentorItem
import com.muhammadfarazrashid.i2106595.UserManager


class mentorChatsDBHelper(context:Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){


    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "CommunityChatsDatabase"
        private const val TABLE_MENTOR_CHATS = "mentorChats"
        private const val KEY_ID = "id"
        private const val KEY_USER_ID = "userId"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_COMMUNITY_CHATS_TABLE = ("CREATE TABLE " + TABLE_MENTOR_CHATS + "("
                + KEY_ID + " VARCHAR(100), "
                + KEY_USER_ID + " VARCHAR(100), "
                + "PRIMARY KEY(" + KEY_ID + ", " + KEY_USER_ID + ")"
                + ")")
        db.execSQL(CREATE_COMMUNITY_CHATS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MENTOR_CHATS")
        onCreate(db)
    }

    // Add a chat to the database

    fun addChat(userId: String, mentorId: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, mentorId+"_"+userId)
        contentValues.put(KEY_USER_ID, userId)
        val success = db.insert(TABLE_MENTOR_CHATS, null, contentValues)
        db.close()
    }

    @SuppressLint("Range")
    fun fetchMentorChats(userId: String, callback: (MutableList<AllMessagesChat>) -> Unit) {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_MENTOR_CHATS INNER JOIN ${MentorDatabaseHelper.TABLE_MENTORS} ON $TABLE_MENTOR_CHATS.$KEY_ID = ${MentorDatabaseHelper.TABLE_MENTORS}.${MentorDatabaseHelper.KEY_ID} WHERE $TABLE_MENTOR_CHATS.$KEY_USER_ID = ?",
            arrayOf(userId)
        )

        val mentors = mutableListOf<AllMessagesChat>()
        if (cursor.moveToFirst()) {
            do {
                val mentor = Mentor(
                    cursor.getString(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("position")),
                    cursor.getString(cursor.getColumnIndex("availability")),
                    cursor.getString(cursor.getColumnIndex("salary")),
                    cursor.getString(cursor.getColumnIndex("description")),
                    cursor.getString(cursor.getColumnIndex("profilePictureUrl"))
                )
                val mentorItem = AllMessagesChat(mentor, mentor.id, mentor.id+"_"+ (UserManager.getCurrentUser()?.id
                    ?: ""),mentor.name, mentor.getprofilePictureUrl())
                mentors.add(mentorItem)
            } while (cursor.moveToNext())
        }
        cursor.close()
        callback(mentors)
    }


}