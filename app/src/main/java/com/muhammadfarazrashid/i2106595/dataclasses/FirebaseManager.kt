package com.muhammadfarazrashid.i2106595.dataclasses

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.muhammadfarazrashid.i2106595.ChatAdapter
import com.muhammadfarazrashid.i2106595.ChatMessage
import com.muhammadfarazrashid.i2106595.Mentor
import com.muhammadfarazrashid.i2106595.UserManager
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper
import io.agora.rtc2.Constants.MediaType
import org.json.JSONObject
import okhttp3.*
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Headers.Companion.toHeaders
class FirebaseManager {




    //get unread chat messages by calling above function

    fun getUnreadChatMessagesCount(
        chat_type: String,
        chatId: String,
        callback: (unreadCount: Int) -> Unit
    ) {
        val currentUser = UserManager.getCurrentUser()?.id
        val myDatabase = FirebaseDatabase.getInstance().getReference("chat/$chat_type/$chatId/messages")
        Log.d("FetchUnreadMessages", "Chat Type: $chat_type, Chat ID: $chatId")
        var unreadMessagesCount: Int = 0

        // Use addChildEventListener to listen for changes in the data
        myDatabase.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.value as? Map<String, Any>
                message?.let {
                    if (message["userId"] != currentUser && message["isRead"] == false) {
                        unreadMessagesCount++
                        // Update UI with the new unread message count
                        callback(unreadMessagesCount)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.value as? Map<String, Any>
                message?.let {
                    if (message["userId"] != currentUser && message["isRead"] == false) {
                        unreadMessagesCount++
                        // Update UI with the new unread message count
                        callback(unreadMessagesCount)
                    }

                    if (message["userId"] != currentUser && message["isRead"] == true) {
                        unreadMessagesCount--
                        // Update UI with the new unread message count
                        callback(unreadMessagesCount)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle deletion of messages
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle moving of messages (if applicable)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Log.e("FetchUnreadMessages", "Error fetching unread messages: ${error.message}")
                // If there's a failure, return -1 or any other error indicator via the callback
                callback(-1)
            }
        })
    }

    fun addNotificationToUser(
        userId: String,
        notification: String,
        notificationType: String,
        context:Context
    ) {
        val webserviceHelper = WebserviceHelper(context)
        webserviceHelper.addNotification(userId, notification, notificationType)


    }




    fun addNotificationToOtherUserInMentorChat(
        userId: String,
        user_type: String,
        notification: String,
        notificationType: String,
        context:Context
    ) {
        val webserviceHelper = WebserviceHelper(context)
        webserviceHelper.addNotification(userId, notification, notificationType)

    }

    fun sendNotificationsToListOfUsers(
        userIds: List<String>,
        notification: String,
        notificationType: String,
        context:Context
    ) {
        userIds.forEach { userId ->
            addNotificationToUser(userId, notification, notificationType,context)
        }
    }

    fun removeNotificationFromUser(
        userId: String,
        notificationId: Int
    ) {
        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("users/$userId/notifications/$notificationId")
        databaseRef.removeValue()
            .addOnSuccessListener {
                // Handle success
                Log.d(ContentValues.TAG, "Notification deleted successfully")
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e(ContentValues.TAG, "Failed to delete notification: ${e.message}")
            }
    }





    companion object {
        fun sendImageToStorage(
            selectedImageUri: Uri,
            chatId: String,
            chat_type: String,
            chatAdapter: ChatAdapter,
            fileType: String
        ) {
            val storage = FirebaseStorage.getInstance()
            val currentUser = UserManager.getCurrentUser()?.id
            val database = FirebaseDatabase.getInstance()
            val chatRef = currentUser?.let {
                database.getReference("chat").child(chat_type).child(chatId).child("messages")
                    .push()
            }
            val storageRef = currentUser?.let {
                storage.reference.child(fileType).child(it).child(
                    chatRef?.key.toString()
                )
            }
            val uploadTask = storageRef?.putFile(selectedImageUri)

            uploadTask?.addOnSuccessListener {
                Log.d(ContentValues.TAG, "Image uploaded successfully")
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    sendImageToChat(
                        uri,
                        chatRef?.key.toString(),
                        chatId,
                        chat_type,
                        "",
                        chatAdapter,
                        fileType
                    )
                }
            }?.addOnFailureListener { e ->
                Log.e(ContentValues.TAG, "Failed to upload image: ${e.message}")
            }
        }

        fun sendImageToChat(
            selectedImageUri: Uri,
            chatRef: String = "",
            chatId: String,
            chat_type: String,
            mentorImageUrl: String,
            chatAdapter: ChatAdapter,
            fileType: String
        ) {
            val database = FirebaseDatabase.getInstance()
            val currentUser = UserManager.getCurrentUser()?.id
            val currentTime = java.text.SimpleDateFormat("HH:mm a").format(java.util.Date())
            val chatRef = currentUser?.let {
                database.getReference("chat").child(chat_type).child(chatId).child("messages")
                    .child(chatRef)
            }
            if (fileType == "chat_images") {

                if (chatRef != null) {
                    val date = java.text.SimpleDateFormat("dd MMMM").format(java.util.Date())
                    chatRef.setValue(
                        mapOf(
                            "message" to "",
                            "time" to currentTime,
                            "date" to date,
                            "userId" to currentUser,
                            "messageImageUrl" to selectedImageUri.toString()
                        )
                    )
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "Image saved successfully")
                            Log.d(ContentValues.TAG, "Image: ${chatRef.key}, Time: ")

                        }
                        .addOnFailureListener { e ->
                            Log.e(ContentValues.TAG, "Failed to save image: ${e.message}")
                        }
                } else {
                    Log.e(ContentValues.TAG, "Failed to get chat reference")
                }
            } else if (fileType == "chat_videos") {
                if (chatRef != null) {
                    val date = java.text.SimpleDateFormat("dd MMMM").format(java.util.Date())
                    chatRef.setValue(
                        mapOf(
                            "message" to "",
                            "time" to currentTime,
                            "date" to date,
                            "userId" to currentUser,
                            "messageVideoUrl" to selectedImageUri.toString()
                        )
                    )
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "File saved successfully")
                            Log.d(ContentValues.TAG, "File: ${chatRef.key}, Time: ")



                        }
                        .addOnFailureListener { e ->
                            Log.e(ContentValues.TAG, "Failed to save file: ${e.message}")
                        }
                } else {
                    Log.e(ContentValues.TAG, "Failed to get chat reference")
                }
            } else if (fileType == "chat_audios") {
                if (chatRef != null) {
                    val date = java.text.SimpleDateFormat("dd MMMM").format(java.util.Date())
                    chatRef.setValue(
                        mapOf(
                            "message" to "",
                            "time" to currentTime,
                            "date" to date,
                            "userId" to currentUser,
                            "messageAudioUrl" to selectedImageUri.toString()
                        )
                    )
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "File saved successfully")
                            Log.d(ContentValues.TAG, "File: ${chatRef.key}, Time: ")
                            Log.d(ContentValues.TAG, "File: ${selectedImageUri.toString()}, Time: ")


                        }
                        .addOnFailureListener { e ->
                            Log.e(ContentValues.TAG, "Failed to save file: ${e.message}")
                        }
                } else {
                    Log.e(ContentValues.TAG, "Failed to get chat reference")
                }
            } else if (fileType == "chat_documents") {
                if (chatRef != null) {
                    val date = java.text.SimpleDateFormat("dd MMMM").format(java.util.Date())
                    chatRef.setValue(
                        mapOf(
                            "message" to "",
                            "time" to currentTime,
                            "date" to date,
                            "userId" to currentUser,
                            "messageDocumentUrl" to selectedImageUri.toString()
                        )
                    )
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "File saved successfully")
                            Log.d(ContentValues.TAG, "File: ${chatRef.key}, Time: ")


                        }
                        .addOnFailureListener { e ->
                            Log.e(ContentValues.TAG, "Failed to save file: ${e.message}")
                        }
                } else {
                    Log.e(ContentValues.TAG, "Failed to get chat reference")
                }
            }


        }

        fun saveMessageToDatabase(
            message: String,
            time: String,
            chat_type: String,
            chatId: String,
            chatAdapter: ChatAdapter
        ) {
            val database = FirebaseDatabase.getInstance()
            val currentUser = UserManager.getCurrentUser()?.id
            val chatRef = currentUser?.let {
                database.getReference("chat").child(chat_type).child(chatId).child("messages")
                    .push()
            }

            if (chatRef != null) {
                val date = java.text.SimpleDateFormat("dd MMMM").format(java.util.Date())
                chatRef.setValue(
                    mapOf(
                        "message" to message,
                        "time" to time,
                        "date" to date,
                        "userId" to currentUser,
                        "isRead" to false
                    )
                )
                    .addOnSuccessListener {
                        Log.d(ContentValues.TAG, "Message saved successfully")
                        Log.d(ContentValues.TAG, "Message: ${chatRef.key}, Time: $time")


                    }
                    .addOnFailureListener { e ->
                        Log.e(ContentValues.TAG, "Failed to save message: ${e.message}")
                    }
            } else {
                Log.e(ContentValues.TAG, "Failed to get chat reference")
            }
        }


        fun deleteMessageInDatabase(
            messageId: String,
            chat_type: String,
            fileType: String,
            chatId: String,
            chatAdapter: ChatAdapter
        ) {
            // Delete the message from the database using Firebase
            // Example code:
            val messageImageUrl=chatAdapter.getMessage(messageId).messageImageUrl
            val messageVideoUrl=chatAdapter.getMessage(messageId).videoImageUrl
            val messageVoiceMemoUrl=chatAdapter.getMessage(messageId).voiceMemoUrl
            val messageDocumentUrl=chatAdapter.getMessage(messageId).documentUrl


            val databaseRef = FirebaseDatabase.getInstance()
                .getReference("chat/$chat_type/$chatId/messages/$messageId")
            databaseRef.removeValue()
                .addOnSuccessListener {
                    // Handle success
                    Log.d(ContentValues.TAG, "Message deleted successfully")
                    //if message has an image we will delete the image from storage too

                    if (messageImageUrl!="" || messageVideoUrl!="" || messageVoiceMemoUrl!="" || messageDocumentUrl!=""
                    ) {
                        if (fileType != "chat_messages") {
                            val storage = FirebaseStorage.getInstance()
                            val currentUser = UserManager.getCurrentUser()?.id
                            val storageRef = currentUser?.let {
                                storage.reference.child(fileType).child(it).child(messageId)
                            }
                            Log.d(
                                "Deleting Message In Database",
                                "Image URL: ${storageRef.toString()}"
                            )
                            if (storageRef != null) {
                                storageRef.delete().addOnSuccessListener {
                                    Log.d(ContentValues.TAG, "Image deleted successfully")
                                }.addOnFailureListener { e ->
                                    Log.e(ContentValues.TAG, "Failed to delete image: ${e.message}")
                                }
                            }
                        }
                    }
                    //remove from adapter
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Log.e(ContentValues.TAG, "Failed to delete message: ${e.message}")
                }
        }

        fun editMessageInDatabase(
            newMessage: String,
            messageId: String,
            chat_type: String,
            chatId: String,
            chatAdapter: ChatAdapter
        ) {
            // Update the message in the database using Firebase
            // Example code:
            val databaseRef = FirebaseDatabase.getInstance()
                .getReference("chat/$chat_type/$chatId/messages/$messageId")
            databaseRef.child("message").setValue(newMessage)
                .addOnSuccessListener {
                    // Handle success
                    Log.d(ContentValues.TAG, "Message edited successfully")
                    //update adapter
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Log.e(ContentValues.TAG, "Failed to edit message: ${e.message}")
                }
        }

        fun removeAllNotificationsFromUser(userId: String) {
            val databaseRef = FirebaseDatabase.getInstance()
                .getReference("users/$userId/notifications")
            databaseRef.removeValue()
                .addOnSuccessListener {
                    // Handle success
                    Log.d(ContentValues.TAG, "All notifications deleted successfully")
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Log.e(ContentValues.TAG, "Failed to delete all notifications: ${e.message}")
                }
        }

        fun removeNotificationFromDatabase(userId:String, notificationId: String) {

            val databaseRef = FirebaseDatabase.getInstance()
                .getReference("users/$userId/notifications/$notificationId")
            databaseRef.removeValue()
                .addOnSuccessListener {
                    // Handle success
                    Log.d(ContentValues.TAG, "Notification deleted successfully")
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Log.e(ContentValues.TAG, "Failed to delete notification: ${e.message}")
                }


        }

        fun addBookingToUser(
            userId: String,
            bookingDate: String,
            bookingTime: String,
            mentorId:String
        ) {
            val database = FirebaseDatabase.getInstance()
            val bookingRef = database.getReference("users").child(userId).child("bookings").push()

            bookingRef.setValue(
                mapOf(
                    "bookingDate" to bookingDate,
                    "bookingTime" to bookingTime,
                    "mentorId" to mentorId
                )
            )
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "Booking added successfully")
                    addBookingToMentor(mentorId,bookingDate,bookingTime,userId)
                }
                .addOnFailureListener { e ->
                    Log.e(ContentValues.TAG, "Failed to add booking: ${e.message}")
                }
        }

        fun addBookingToMentor(
            mentorId: String,
            bookingDate: String,
            bookingTime: String,
            userId:String
        ) {
            val database = FirebaseDatabase.getInstance()
            val bookingRef = database.getReference("Mentors").child(mentorId).child("bookings").push()

            bookingRef.setValue(
                mapOf(
                    "bookingDate" to bookingDate,
                    "bookingTime" to bookingTime,
                    "userId" to userId
                )
            )
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "Booking added successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(ContentValues.TAG, "Failed to add booking: ${e.message}")
                }
        }

        fun addFcmTokenToUser(
            userId: String,
            userType: String,
            fcmToken: String
        ) {
            val database = FirebaseDatabase.getInstance()
            val fcmTokenRef = database.getReference(userType).child(userId).child("fcmToken")

            fcmTokenRef.setValue(fcmToken)
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "FCM token added successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(ContentValues.TAG, "Failed to add FCM token: ${e.message}")
                }
        }

        fun fetchMentor(
            mentorId: String,
            callback: (mentor: Mentor?) -> Unit
        ) {
            val database = FirebaseDatabase.getInstance()
            val mentorRef = database.getReference("Mentors").child(mentorId)

            mentorRef.get().addOnSuccessListener { dataSnapshot ->
                val mentor = dataSnapshot.getValue(Mentor::class.java)
                if (mentor != null) {
                    callback(mentor)
                } else {
                    callback(null)
                }
            }
                .addOnFailureListener { databaseError ->
                    Log.e(ContentValues.TAG, "Error fetching mentor: ${databaseError.message}")
                    callback(null)
                }
        }

        fun sendNotification(senderName:String, message: String, chatId: String,otherUserToken:String,chatType:String, otherUserId:String)
        {
            //userId, chatType, chatId, message, otherusertoken
            UserManager.getCurrentUser()?.id?.let { it1 ->
                val jsonObject = JSONObject()
                val jsonNotificationObject=JSONObject()
                val jsonDataObject=JSONObject()
                jsonNotificationObject.put("title",senderName )
                jsonNotificationObject.put("body", message)

                jsonDataObject.put("userId",otherUserId )
                jsonDataObject.put("chatType", chatType)
                jsonDataObject.put("chatId", chatId)

                jsonObject.put("notification", jsonNotificationObject)
                jsonObject.put("data", jsonDataObject)
                jsonObject.put("to", otherUserToken)

                callApi(jsonObject)

            }


        }

        fun callApi(jsonObject: JSONObject) {
            val JSON: okhttp3.MediaType = "application/json; charset=utf-8".toMediaType()
            val client = OkHttpClient()
            val url = "https://fcm.googleapis.com/fcm/send"
            val body: RequestBody = RequestBody.create(JSON, jsonObject.toString())
            val request: Request = Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAAP4pCMRk:APA91bHgnvBenLUOLfGYbKTuXRjn6OvUIerJ0RHP92DoEUNqQa49Lrg55qkw-zqGpqwVn7k-AWTGiYdz-AlpoCtoYNgRbuzctPAv5ESP7rZPn3fyv-PsPzPESbSmKHaSjZj1rvWYS0RG")
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle failure
                }

                override fun onResponse(call: Call, response: Response) {
                    // Handle response
                }
            })

        }



    }
}


