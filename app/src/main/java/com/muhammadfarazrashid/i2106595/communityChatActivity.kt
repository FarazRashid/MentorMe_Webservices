package com.muhammadfarazrashid.i2106595

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.muhammadfarazrashid.i2106595.dataclasses.FirebaseManager
import com.muhammadfarazrashid.i2106595.dataclasses.User
import com.squareup.picasso.Picasso

class communityChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var mentorName: TextView
    private lateinit var currentMentor: Mentor
    private lateinit var mentorImage: ImageView
    private lateinit var sendButton: Button
    private lateinit var micButton: Button
    private lateinit var messageField: EditText
    private lateinit var takePhoto: Button
    private lateinit var sendImage: Button
    private lateinit var attachImage: Button
    private lateinit var listOfUsers: ArrayList<User>
    private var selectedMessageId: String? = null
    private lateinit var selectedImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.communitychat)

        recyclerView = findViewById(R.id.communityChatRecyclerView)

        currentMentor = intent.getParcelableExtra<Mentor>("mentor")!!
        initViews()
        setMentorDetails(currentMentor)
        listOfUsers= fetchAllUsersInCommunityChat()
        setButtonClickListeners()
        setBottomNavigationListener()
        setAddMentorClickListener()
    }

    private fun setMentorDetails(mentor: Mentor) {
        mentorName = findViewById(R.id.mentorName)
        mentorName.text = currentMentor.name
        mentorImage = findViewById(R.id.imageView9)

        if (!mentor.getprofilePictureUrl().isEmpty()) {
            Picasso.get().load(mentor.getprofilePictureUrl()).into(mentorImage)
        }
    }

    private fun initViews() {
        micButton = findViewById(R.id.micButton)
        messageField = findViewById(R.id.reviewText)
        takePhoto = findViewById(R.id.takePhoto)
        sendImage = findViewById(R.id.sendImage)
        attachImage = findViewById(R.id.sendFile)
        sendButton = findViewById(R.id.sendButton)

        recyclerView = findViewById(R.id.communityChatRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }

        val chatMessages = ArrayList<ChatMessage>()
        chatAdapter = ChatAdapter(chatMessages,this, object : ChatAdapter.onMessageClickListener {
            override fun onMessageClick(position: Int) {
                val view = recyclerView.layoutManager?.findViewByPosition(position)
                showPopupMenu(chatMessages[position], view)
            }
        }
        )
        recyclerView.adapter = chatAdapter
    }

    private fun showPopupMenu(chatMessage: ChatMessage, view: View?)
    {
        view?.let {
            val contextWrapper = ContextThemeWrapper(this, R.style.MyMenuItemStyle)
            val popupMenu = PopupMenu(contextWrapper, it)
            popupMenu.menuInflater.inflate(R.menu.edit_delete_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.editItem -> {
                        // Handle edit message
                        // val editMessageDialog = EditMessageDialog(chatMessage.message, chatMessage.id, this)
                        // editMessageDialog.show(supportFragmentManager, "EditMessageDialog")
                        messageField.setText(chatMessage.message)
                        selectedMessageId = chatMessage.id

                        true
                    }
                    R.id.deleteItem -> {
                        deleteMessageInDatabase(chatMessage.id)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }


    private fun saveMessageToDatabase(message: String, time: String) {
        val database = FirebaseDatabase.getInstance()
        val currentUser = UserManager.getCurrentUser()?.id
        val chatRef = currentUser?.let { database.getReference("chat").child("community_chats").child(currentMentor.id).child("messages").push() }
        if (chatRef != null) {
            val date = java.text.SimpleDateFormat("dd MMMM").format(java.util.Date())
            chatRef.setValue(mapOf("message" to message, "time" to time, "date" to date, "userId" to currentUser))
                .addOnSuccessListener {
                    Log.d(TAG, "Message saved successfully")
                    Log.d(TAG, "Message: ${chatRef.key}, Time: $time")
                    chatAdapter.addMessage(ChatMessage(chatRef.key.toString(),message, time, true, ""))

                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to save message: ${e.message}")
                }
        } else {
            Log.e(TAG, "Failed to get chat reference")
        }
    }

    private fun deleteMessageInDatabase(messageId: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("chat/community_chats/${currentMentor.id}/messages/$messageId")
        databaseRef.removeValue()
            .addOnSuccessListener {
                // Handle success
                Log.d(TAG, "Message deleted successfully")
                if(chatAdapter.getMessage(messageId).messageImageUrl.isNotEmpty())
                {
                    val storage = FirebaseStorage.getInstance()
                    val currentUser = UserManager.getCurrentUser()?.id
                    val storageRef = currentUser?.let { storage.reference.child("chat_images").child(it).child(messageId) }
                    Log.d("Deleting Message In Database", "Image URL: ${storageRef.toString()}")
                    if (storageRef != null) {
                        storageRef.delete().addOnSuccessListener {
                            Log.d(TAG, "Image deleted successfully")
                        }.addOnFailureListener { e ->
                            Log.e(TAG, "Failed to delete image: ${e.message}")
                        }
                    }
                }
                //remove from adapter
                chatAdapter.removeMessage(messageId)
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e(TAG, "Failed to delete message: ${e.message}")
            }
    }

    private fun editMessageInDatabase(newMessage: String, messageId: String) {
        // Update the message in the database using Firebase
        // Example code:
        val databaseRef = FirebaseDatabase.getInstance().getReference("chat/community_chats/${currentMentor.id}/messages/$messageId")
        databaseRef.child("message").setValue(newMessage)
            .addOnSuccessListener {
                // Handle success
                Log.d(TAG, "Message edited successfully")
                //update adapter
                chatAdapter.editMessage(messageId, newMessage)
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e(TAG, "Failed to edit message: ${e.message}")
            }
    }

    private fun sendMessage() {
        val message = messageField.text.toString()
        val currentTime = java.text.SimpleDateFormat("HH:mm a").format(java.util.Date())
        if (message.isNotEmpty()) {
            if (selectedMessageId != null) {
                // Update the message in the database
                editMessageInDatabase(message, selectedMessageId!!)
                // Clear the selected message ID
                selectedMessageId = null
            } else {
                // Add a new message to the database
                saveMessageToDatabase(message, currentTime)
            }
            // Clear the message field
            messageField.text.clear()
        }
    }

    private val pickImageGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data ?: return@registerForActivityResult
            // Send the image to the chat
            FirebaseManager.sendImageToStorage(selectedImageUri, currentMentor.id, "community_chats",chatAdapter)
        }
    }
    private fun sendImage(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageGalleryLauncher.launch(galleryIntent)
    }


    private fun setButtonClickListeners() {

        sendButton.setOnClickListener {
            val message = messageField.text.toString()
            if (message.isNotEmpty()) {
                sendMessage()
                messageField.text.clear()
            }
        }

        micButton.setOnClickListener {
            // recordVoiceMessage()
        }

        takePhoto.setOnClickListener {
            //takePhoto()
        }

        sendImage.setOnClickListener {
            sendImage()
        }

        attachImage.setOnClickListener {
            //attachImage()
        }


        findViewById<Button>(R.id.callButton).setOnClickListener {
            startActivity(Intent(this, PhoneCallActivity::class.java))
        }

        findViewById<Button>(R.id.videoButton).setOnClickListener {
            startActivity(Intent(this, VideoCallActivity::class.java))
        }

        findViewById<Button>(R.id.backbutton).setOnClickListener {
            onBackPressed()
        }

        findViewById<Button>(R.id.takePhoto).setOnClickListener {
            startActivity(Intent(this, PhotoActivity::class.java))
        }
    }

    private fun setBottomNavigationListener() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemReselectedListener { item ->
            val intent = when (item.itemId) {
                R.id.menu_search -> Intent(this, searchPageActivity::class.java)
                R.id.menu_home -> Intent(this, homePageActivity::class.java)
                R.id.menu_chat -> Intent(this, mainChatActivity::class.java)
                R.id.menu_profile -> Intent(this, MyProfileActivity::class.java)
                else -> null
            }
            intent?.let {
                startActivity(it)
            }
        }
    }

    private fun setAddMentorClickListener() {
        findViewById<ImageView>(R.id.addMentorButton).setOnClickListener {
            startActivity(Intent(this, AddAMentor::class.java))
        }
    }


    private fun fetchAllUsersInCommunityChat(): ArrayList<User>{
        val database = FirebaseDatabase.getInstance()
        val chatRef = database.getReference("chat").child("community_chats").child(currentMentor.id).child("users")

        //create a list of users
        val users = ArrayList<User>()
        val userIds= ArrayList<String>()

        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (messageSnapshot in dataSnapshot.children) {
                    Log.d("MentorChatActivity", "Value = ${messageSnapshot.value}")
                    val userId = messageSnapshot.value
                    userIds.add(userId.toString())

                }
                // Once user IDs are fetched, fetch user details
                fetchUserDetails(userIds, users)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MentorChatActivity", "Failed to retrieve chat messages: ${databaseError.message}")
            }
        })

        return users
    }




    private fun fetchUserDetails(userIds: List<String>, users: ArrayList<User>) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (messageSnapshot in dataSnapshot.children) {
                    val userId = messageSnapshot.key.toString()
                    Log.d("MentorChatActivity2", "Value = ${messageSnapshot.value}")
                    if(userIds.contains(userId)){
                        val name = messageSnapshot.child("name").value as String
                        val email = messageSnapshot.child("email").value as String
                        if(messageSnapshot.child("profilePictureUrl").value==null){
                            val profilePictureUrl = ""
                            val user = User(userId, name, email,"","","","", profilePictureUrl,"")
                            users.add(user)
                        }
                        else{
                            val profilePictureUrl = messageSnapshot.child("profilePictureUrl").value as String
                            val user = User(userId, name, email,"","","","", profilePictureUrl,"")
                            users.add(user)
                        }

                    }
                }
                for(user in users){
                    Log.d("MentorChatActivity3", "User = ${user.name}")
                }

                fetchUserMessages(currentMentor.getprofilePictureUrl())

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MentorChatActivity", "Failed to retrieve chat messages: ${databaseError.message}")
            }
        })
    }

    private fun fetchUserMessages(mentorImageUrl: String) {
        val database = FirebaseDatabase.getInstance()
        val currentUser = UserManager.getCurrentUser()?.id
        val chatRef = currentUser?.let { database.getReference("chat").child("community_chats").child(currentMentor.id).child("messages") }

        for(user in listOfUsers){
            Log.d("MentorChatActivity4", "User = ${user.profilePictureUrl}")
        }

        chatRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (messageSnapshot in dataSnapshot.children) {
                    val message = messageSnapshot.child("message").value as String
                    val time = messageSnapshot.child("time").value as String
                    val date = messageSnapshot.child("date").value as String
                    val userId = messageSnapshot.child("userId").value as String
                    val messageId= messageSnapshot.key.toString()
                    val isCurrentUser = userId == currentUser
                    var messageImageUrl=""
                    if(messageSnapshot.child("messageImageUrl").exists())
                        messageImageUrl= messageSnapshot.child("messageImageUrl").value as String
                    if(!isCurrentUser&& userId!=currentMentor.id){
                        val user = listOfUsers.find { it.id==userId }
                        if(user!=null){
                            chatAdapter.addMessage(ChatMessage(messageId,message, time, isCurrentUser, user.profilePictureUrl,messageImageUrl))
                        }
                    }
                    else
                        chatAdapter.addMessage(ChatMessage(messageId,message, time, isCurrentUser, mentorImageUrl,messageImageUrl))
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MentorChatActivity", "Failed to retrieve chat messages: ${databaseError.message}")
            }
        })
    }


}
