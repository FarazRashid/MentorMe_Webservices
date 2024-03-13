package com.muhammadfarazrashid.i2106595.dataclasses

import android.app.Activity
import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.muhammadfarazrashid.i2106595.ChatAdapter
import com.muhammadfarazrashid.i2106595.ChatMessage

class FirebaseManager {


    companion object {
        fun sendImageToStorage(
            selectedImageUri: Uri,
            chatId: String,
            chat_type: String,
            chatAdapter: ChatAdapter
        ) {
            val storage = FirebaseStorage.getInstance()
            val currentUser = UserManager.getCurrentUser()?.id
            val database = FirebaseDatabase.getInstance()
            val chatRef = currentUser?.let {
                database.getReference("chat").child(chat_type).child(chatId).child("messages")
                    .push()
            }
            val storageRef = currentUser?.let {
                storage.reference.child("chat_images").child(it).child(
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
                        chatAdapter
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
            chatAdapter: ChatAdapter
        ) {
            val database = FirebaseDatabase.getInstance()
            val currentUser = UserManager.getCurrentUser()?.id
            val chatRef = currentUser?.let {
                database.getReference("chat").child(chat_type).child(chatId).child("messages")
                    .child(chatRef)
            }

            if (chatRef != null) {
                val date = java.text.SimpleDateFormat("dd MMMM").format(java.util.Date())
                chatRef.setValue(
                    mapOf(
                        "message" to "",
                        "time" to "",
                        "date" to date,
                        "userId" to currentUser,
                        "messageImageUrl" to selectedImageUri.toString()
                    )
                )
                    .addOnSuccessListener {
                        Log.d(ContentValues.TAG, "Image saved successfully")
                        Log.d(ContentValues.TAG, "Image: ${chatRef.key}, Time: ")
                        chatAdapter.addMessage(
                            ChatMessage(
                                chatRef.key.toString(),
                                "",
                                "",
                                true,
                                mentorImageUrl,
                                selectedImageUri.toString()
                            )
                        )

                    }
                    .addOnFailureListener { e ->
                        Log.e(ContentValues.TAG, "Failed to save image: ${e.message}")
                    }
            } else {
                Log.e(ContentValues.TAG, "Failed to get chat reference")
            }

        }

        fun saveMessageToDatabase(message: String, time: String, chat_type: String, chatId: String, chatAdapter: ChatAdapter) {
            val database = FirebaseDatabase.getInstance()
            val currentUser = UserManager.getCurrentUser()?.id
            val chatRef = currentUser?.let {
                database.getReference("chat").child(chat_type).child(chatId).child("messages").push()
            }

            if (chatRef != null) {
                val date = java.text.SimpleDateFormat("dd MMMM").format(java.util.Date())
                chatRef.setValue(mapOf("message" to message, "time" to time, "date" to date, "userId" to currentUser))
                    .addOnSuccessListener {
                        Log.d(ContentValues.TAG, "Message saved successfully")
                        Log.d(ContentValues.TAG, "Message: ${chatRef.key}, Time: $time")
                        chatAdapter.addMessage(ChatMessage(chatRef.key.toString(),message, time, true, ""))

                    }
                    .addOnFailureListener { e ->
                        Log.e(ContentValues.TAG, "Failed to save message: ${e.message}")
                    }
            } else {
                Log.e(ContentValues.TAG, "Failed to get chat reference")
            }
        }

        fun deleteMessageInDatabase(messageId: String, chat_type: String, chatId: String, chatAdapter: ChatAdapter) {
            // Delete the message from the database using Firebase
            // Example code:
            val databaseRef = FirebaseDatabase.getInstance().getReference("chat/$chat_type/$chatId/messages/$messageId")
            databaseRef.removeValue()
                .addOnSuccessListener {
                    // Handle success
                    Log.d(ContentValues.TAG, "Message deleted successfully")
                    //if message has an image we will delete the image from storage too
                    if(chatAdapter.getMessage(messageId).messageImageUrl.isNotEmpty())
                    {
                        val storage = FirebaseStorage.getInstance()
                        val currentUser = UserManager.getCurrentUser()?.id
                        val storageRef = currentUser?.let { storage.reference.child("chat_images").child(it).child(messageId) }
                        Log.d("Deleting Message In Database", "Image URL: ${storageRef.toString()}")
                        if (storageRef != null) {
                            storageRef.delete().addOnSuccessListener {
                                Log.d(ContentValues.TAG, "Image deleted successfully")
                            }.addOnFailureListener { e ->
                                Log.e(ContentValues.TAG, "Failed to delete image: ${e.message}")
                            }
                        }
                    }
                    //remove from adapter
                    chatAdapter.removeMessage(messageId)
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Log.e(ContentValues.TAG, "Failed to delete message: ${e.message}")
                }
        }

        fun editMessageInDatabase(newMessage: String, messageId: String, chat_type: String, chatId: String, chatAdapter: ChatAdapter) {
            // Update the message in the database using Firebase
            // Example code:
            val databaseRef = FirebaseDatabase.getInstance().getReference("chat/$chat_type/$chatId/messages/$messageId")
            databaseRef.child("message").setValue(newMessage)
                .addOnSuccessListener {
                    // Handle success
                    Log.d(ContentValues.TAG, "Message edited successfully")
                    //update adapter
                    chatAdapter.editMessage(messageId, newMessage)
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Log.e(ContentValues.TAG, "Failed to edit message: ${e.message}")
                }
        }

    }
}

