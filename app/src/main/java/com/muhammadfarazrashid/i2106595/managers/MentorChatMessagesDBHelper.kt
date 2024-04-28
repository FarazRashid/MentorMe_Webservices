import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.muhammadfarazrashid.i2106595.ChatMessage
import com.muhammadfarazrashid.i2106595.Mentor
import com.muhammadfarazrashid.i2106595.UserManager
import com.muhammadfarazrashid.i2106595.dataclasses.User

class MentorChatMessagesDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mentorme.db"
        private const val DATABASE_VERSION = 2

        private const val COLUMN_SENT = "sent" // New column

        // Table name
        private const val TABLE_MENTOR_CHAT_MESSAGES = "mentorchatmessages"

        // Column names
        private const val COLUMN_CHAT_ID = "chatId"
        private const val COLUMN_MESSAGE_ID = "messageId"
        private const val COLUMN_USER_ID = "userId"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_MESSAGE = "message"
        private const val COLUMN_MESSAGE_IMAGE_URL = "messageImageUrl"
        private const val COLUMN_VIDEO_IMAGE_URL = "videoImageUrl"
        private const val COLUMN_VOICE_MEMO_URL = "voiceMemoUrl"
        private const val COLUMN_DOCUMENT_URL = "documentUrl"

        // Create table query
        private const val CREATE_TABLE_MENTOR_CHAT_MESSAGES = "CREATE TABLE $TABLE_MENTOR_CHAT_MESSAGES (" +
                "$COLUMN_CHAT_ID VARCHAR(200), " +
                "$COLUMN_MESSAGE_ID VARCHAR(100) PRIMARY KEY, " +
                "$COLUMN_USER_ID VARCHAR(100), " +
                "$COLUMN_TIME VARCHAR(100), " +
                "$COLUMN_MESSAGE VARCHAR(100), " +
                "$COLUMN_MESSAGE_IMAGE_URL VARCHAR(100), " +
                "$COLUMN_VIDEO_IMAGE_URL VARCHAR(100), " +
                "$COLUMN_VOICE_MEMO_URL VARCHAR(100), " +
                "$COLUMN_DOCUMENT_URL VARCHAR(100)," +
                "$COLUMN_SENT BOOLEAN"+ // New column
                ")"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create tables
        db.execSQL(CREATE_TABLE_MENTOR_CHAT_MESSAGES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MENTOR_CHAT_MESSAGES")

        // Create tables again
        onCreate(db)
    }

    @SuppressLint("Range")
    fun fetchMessagesInMentorChat(userId: String, mentorId: String, mentorImageUrl:String, callback: (List<ChatMessage>) -> Unit) {
        val messages = mutableListOf<ChatMessage>()
        val db = this.readableDatabase
        try {
            val cursor = db.query(
                TABLE_MENTOR_CHAT_MESSAGES, arrayOf(COLUMN_CHAT_ID, COLUMN_MESSAGE_ID, COLUMN_USER_ID, COLUMN_TIME, COLUMN_MESSAGE, COLUMN_MESSAGE_IMAGE_URL, COLUMN_VIDEO_IMAGE_URL, COLUMN_VOICE_MEMO_URL, COLUMN_DOCUMENT_URL),
                "$COLUMN_CHAT_ID=?", arrayOf(mentorId+"_"+userId), null, null, null, null
            )

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val messageUserId= cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID))
                    var isUser=false
                    if(messageUserId == (userId))
                        isUser=true

                    val message = ChatMessage(
                        cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TIME)),
                        isUser,
                        mentorImageUrl,
                        cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_IMAGE_URL)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_VIDEO_IMAGE_URL)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_VOICE_MEMO_URL)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DOCUMENT_URL))
                    )
                    messages.add(message)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching messages: ${e.message}")
        } finally {
            db.close()
        }
        callback(messages)
    }

    //give me a send message function with this signature     fun sendMessageInMentorChat(message: ChatMessage,mentor:Mentor) {

    fun sendMessageInMentorChat(message: ChatMessage,mentor: Mentor) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_CHAT_ID, mentor.id+"_"+ (UserManager.getCurrentUser()?.id ?:"" ))
        contentValues.put(COLUMN_MESSAGE_ID, message.id)
        contentValues.put(COLUMN_USER_ID, UserManager.getCurrentUser()?.id ?: "")
        contentValues.put(COLUMN_TIME, message.time)
        contentValues.put(COLUMN_MESSAGE, message.message)
        contentValues.put(COLUMN_MESSAGE_IMAGE_URL, message.messageImageUrl)
        contentValues.put(COLUMN_VIDEO_IMAGE_URL, message.videoImageUrl)
        contentValues.put(COLUMN_VOICE_MEMO_URL, message.voiceMemoUrl)
        contentValues.put(COLUMN_DOCUMENT_URL, message.documentUrl)
        contentValues.put(COLUMN_SENT, true)
        db.insert(TABLE_MENTOR_CHAT_MESSAGES, null, contentValues)
        db.close()
    }

    //give me an edit message function with this signature     fun editMessageInMentorChat(messageId: String, message: String) {

    fun editMessageInMentorChat(messageId: String, message: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_MESSAGE, message)
        db.update(TABLE_MENTOR_CHAT_MESSAGES, contentValues, "$COLUMN_MESSAGE_ID=?", arrayOf(messageId))
        db.close()
    }

    //give me a delete message function with this signature     fun deleteMessageInMentorChat(messageId: String) {

    fun deleteMessageInMentorChat(messageId: String) {
        val db = this.writableDatabase
        db.delete(TABLE_MENTOR_CHAT_MESSAGES, "$COLUMN_MESSAGE_ID=?", arrayOf(messageId))
        db.close()
    }

    @SuppressLint("Range")
    fun fetchUnsentMessages(isSent: Boolean): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_MENTOR_CHAT_MESSAGES, arrayOf(COLUMN_CHAT_ID, COLUMN_MESSAGE_ID, COLUMN_USER_ID, COLUMN_TIME, COLUMN_MESSAGE, COLUMN_MESSAGE_IMAGE_URL, COLUMN_VIDEO_IMAGE_URL, COLUMN_VOICE_MEMO_URL, COLUMN_DOCUMENT_URL),
            "$COLUMN_SENT=?", arrayOf(isSent.toString()), null, null, null, null
        )
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val message = ChatMessage(
                    cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TIME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID)) == UserManager.getCurrentUser()?.id,
                    cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_IMAGE_URL)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_VIDEO_IMAGE_URL)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_VOICE_MEMO_URL)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_DOCUMENT_URL))
                )
                messages.add(message)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return messages
    }
}
