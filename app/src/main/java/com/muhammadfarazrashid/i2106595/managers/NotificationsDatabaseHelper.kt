package com.muhammadfarazrashid.i2106595.managers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NotificationsDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "NotificationsDB"
        private const val TABLE_NAME = "notifications"
        private const val KEY_ID = "id"
        private const val KEY_NOTIFICATION = "notification"
        //notification type, and user id columns
        private const val KEY_notificationType = "notificationType"
        private const val KEY_userId = "userId"



    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_NOTIFICATIONS_TABLE = ("CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " VARCHAR(100) PRIMARY KEY," + KEY_NOTIFICATION + " VARCHAR(1000,"
                + KEY_notificationType + " VARCHAR(100)," + KEY_userId + " VARCHAR(1000)"+
                ")")
        db.execSQL(CREATE_NOTIFICATIONS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    //give me an add notificaiton with this signature     fun addNotification(userId: String, notificationMessage: String,notificationType:String) {

    fun addNotification(userId: String, notificationMessage: String,notificationType:String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_ID, userId)
        values.put(KEY_NOTIFICATION, notificationMessage)
        values.put(KEY_notificationType, notificationType)
        values.put(KEY_userId, userId)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    //give me a get notificaiton with this signature     fun getNotifications(userId: String, callback: (List<Notification>) -> Unit) {

    @SuppressLint("Range")
    fun getNotifications(userId: String, callback: (List<WebserviceHelper.Notification>) -> Unit) {
        val notifications = mutableListOf<WebserviceHelper.Notification>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $KEY_userId = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(userId))
        if (cursor.moveToFirst()) {
            do {
                val notification = WebserviceHelper.Notification(
                    cursor.getString(cursor.getColumnIndex(KEY_ID)),
                    cursor.getString(cursor.getColumnIndex(KEY_userId)),
                    cursor.getString(cursor.getColumnIndex(KEY_NOTIFICATION)),
                    cursor.getString(cursor.getColumnIndex(KEY_notificationType)),
                )
                notifications.add(notification)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        callback(notifications)
    }
}