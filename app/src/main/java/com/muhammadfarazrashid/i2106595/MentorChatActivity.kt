package com.muhammadfarazrashid.i2106595


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akexorcist.screenshotdetection.ScreenshotDetectionDelegate
import com.devlomi.record_view.OnRecordListener
import com.devlomi.record_view.RecordButton
import com.devlomi.record_view.RecordPermissionHandler
import com.devlomi.record_view.RecordView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.muhammadfarazrashid.i2106595.dataclasses.FirebaseManager
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper
import com.muhammadfarazrashid.i2106595.managers.photoTakerManager
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class MentorChatActivity : AppCompatActivity(), ScreenshotDetectionDelegate.ScreenshotDetectionListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var mentorName: TextView
    private lateinit var currentMentor: Mentor
    private lateinit var sendButton: Button
    private lateinit var recordButton: RecordButton
    private lateinit var messageField: EditText
    private lateinit var takePhoto: Button
    private lateinit var sendImage: Button
    private lateinit var attachImage: Button
    private var chatId: String =""
    private var mentorImageUrl: String = ""
    private var selectedMessageId: String? = null
    private lateinit var selectedImageUri: Uri
    private lateinit var recordView: RecordView
    private var audioRecorder: AudioRecorder? = null
    private var recordFile: File? = null


    private val screenshotDetectionDelegate = ScreenshotDetectionDelegate(this, this)

    override fun onStart() {
        super.onStart()
        screenshotDetectionDelegate.startScreenshotDetection()
    }

    override fun onStop() {
        super.onStop()
        screenshotDetectionDelegate.stopScreenshotDetection()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mentorchat)
        checkReadExternalStoragePermission()

        currentMentor = intent.getParcelableExtra<Mentor>("mentor")!!
        setMentorDetails(currentMentor)
        initViews()

        initRecording()


        getChatId(object : ChatIdCallback {
            override fun onChatIdReceived(chatId1: String) {
                Log.d("MentorChatActivity", "Chat ID: $chatId")
                chatId = chatId1

            }

            override fun onChatIdError(errorMessage: String) {
                Log.e("MentorChatActivity", errorMessage)
                // Handle the error here
            }
        })


        setButtonClickListeners()
        setBottomNavigationListener()
        setAddMentorClickListener()


    }


    private fun setMentorDetails(mentor: Mentor) {
        mentorName = findViewById(R.id.mentorName)
        mentorName.text = currentMentor.name
        fetchUserMessages(currentMentor.getprofilePictureUrl())
    }

    private fun initViews() {
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
        chatAdapter = ChatAdapter(chatMessages, this, object : ChatAdapter.onMessageClickListener {
            override fun onMessageClick(position: Int) {
                val view = recyclerView.layoutManager?.findViewByPosition(position)
                Log.d("MentorChatActivity", "Message clicked at position $position MessageId: ${chatMessages[position].id}")
                showPopupMenu(chatMessages[position], view)
            }
        })
        recyclerView.adapter = chatAdapter
    }
    
  
    private fun initRecording(){

            audioRecorder = AudioRecorder()
            recordView = findViewById(R.id.record_view)
            recordButton = findViewById(R.id.micButton)
         //   val btnChangeOnclick = findViewById<Button>(R.id.btn_change_onclick)

            // To Enable Record Lock
//        recordView.setLockEnabled(true);
//        recordView.setRecordLockImageView(findViewById(R.id.record_lock));
            //IMPORTANT
            recordButton.setRecordView(recordView)

            // if you want to click the button (in case if you want to make the record button a Send Button for example..)
//        recordButton.setListenForRecord(false);

            //ListenForRecord must be false ,otherwise onClick will not be called
            recordButton.setOnRecordClickListener {
                Toast.makeText(this@MentorChatActivity, "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT).show()
                Log.d("RecordButton", "RECORD BUTTON CLICKED")
            }


            //Cancel Bounds is when the Slide To Cancel text gets before the timer . default is 8
            recordView.cancelBounds = 8f
            recordView.setSmallMicColor(Color.parseColor("#c2185b"))

            //prevent recording under one Second
            recordView.setLessThanSecondAllowed(false)
            recordView.setSlideToCancelText("Slide To Cancel")
            recordView.setOnRecordListener(object : OnRecordListener {
                override fun onStart() {
                    onRecordSetupVisibilities()
                    recordFile = File(filesDir, UUID.randomUUID().toString() + ".3gp")
                    try {
                        audioRecorder!!.start(recordFile!!.path)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    Log.d("RecordView", "onStart")
                    Toast.makeText(this@MentorChatActivity, "OnStartRecord", Toast.LENGTH_SHORT).show()
                }

                override fun onCancel() {
                    stopRecording(true)
                    onFinishSetupVisibilities()
                    Toast.makeText(this@MentorChatActivity, "onCancel", Toast.LENGTH_SHORT).show()
                    Log.d("RecordView", "onCancel")
                }

                override fun onFinish(recordTime: Long, limitReached: Boolean) {
                    stopRecording(false)
                    onFinishSetupVisibilities()
                    val time = getHumanTimeText(recordTime)
                    Toast.makeText(
                        this@MentorChatActivity,
                        "onFinishRecord - Recorded Time is: " + time + " File saved at " + recordFile!!.path,
                        Toast.LENGTH_SHORT
                    ).show()
                    val webserviceHelper = WebserviceHelper(this@MentorChatActivity)
                    webserviceHelper.uploadFileToServer("audio", Uri.fromFile(recordFile)){ bool,url->
                        Toast.makeText(this@MentorChatActivity, "Audio uploaded", Toast.LENGTH_SHORT).show()
                        val currentTime= SimpleDateFormat("HH:mm a").format(Date())
                        webserviceHelper.sendMessageInMentorChat(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", "", url, ""), currentMentor)
                        chatAdapter.addMessage(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", "", url, ""))

                    }

//                    FirebaseManager.sendImageToStorage(Uri.fromFile(recordFile), chatId, "mentor_chats", chatAdapter, "chat_audios")
                    Log.d("RecordView", "onFinish Limit Reached? $limitReached")
                    if (time != null) {
                        Log.d("RecordTime", time)
                    }
                }

                override fun onLessThanSecond() {
                    stopRecording(true)
                    onFinishSetupVisibilities()
                    Toast.makeText(this@MentorChatActivity, "OnLessThanSecond", Toast.LENGTH_SHORT).show()
                    Log.d("RecordView", "onLessThanSecond")
                }

                override fun onLock() {
                    onFinishSetupVisibilities()
                    Toast.makeText(this@MentorChatActivity, "onLock", Toast.LENGTH_SHORT).show()
                    Log.d("RecordView", "onLock")
                }
            })
            recordView.setOnBasketAnimationEndListener {
                Log.d(
                    "RecordView",
                    "Basket Animation Finished"
                )
            }
            recordView.setRecordPermissionHandler(RecordPermissionHandler {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return@RecordPermissionHandler true
                }
                val recordPermissionAvailable = ContextCompat.checkSelfPermission(
                    this@MentorChatActivity,
                    Manifest.permission.RECORD_AUDIO
                ) == PermissionChecker.PERMISSION_GRANTED
                if (recordPermissionAvailable) {
                    return@RecordPermissionHandler true
                }
                ActivityCompat.requestPermissions(
                    this@MentorChatActivity, arrayOf(Manifest.permission.RECORD_AUDIO),
                    0
                )
                false
            })
    }


    private fun stopRecording(deleteFile: Boolean) {
        audioRecorder!!.stop()
        if (recordFile != null && deleteFile) {
            recordFile!!.delete()
        }
    }

    private fun onRecordSetupVisibilities() {
        recordView.visibility = View.VISIBLE
        messageField.visibility = View.GONE
        attachImage.visibility = View.GONE
        sendImage.visibility = View.GONE

    }

    private fun onFinishSetupVisibilities() {
        recordView.visibility = View.GONE
        messageField.visibility = View.VISIBLE
        attachImage.visibility = View.VISIBLE
        sendImage.visibility = View.VISIBLE
    }

    @SuppressLint("DefaultLocale")
    private fun getHumanTimeText(milliseconds: Long): String? {
        return java.lang.String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(milliseconds),
            TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
        )
    }

    private fun showPopupMenu(chatMessage: ChatMessage, view: View?)
    {

        if(!chatMessage.isUser)
            return

        val currentTime = SimpleDateFormat("HH:mm a").format(Date())
        val messageTime = chatMessage.time // Assuming chatMessage.time is the time of the message

        val sdf = SimpleDateFormat("HH:mm a")
        val currentTimeDate = sdf.parse(currentTime)
        val messageTimeDate = sdf.parse(messageTime)

        // Calculate time difference in milliseconds
        val timeDifferenceMillis = abs(currentTimeDate.time - messageTimeDate.time)

        // Convert time difference to minutes
        val timeDifferenceMinutes = timeDifferenceMillis / (1000 * 60)

        // Check if the time difference is less than 5 minutes
        if (timeDifferenceMinutes > 5) {
            // Do not allow editing or deleting the message
            return
        }

        Log.d("MentorActivityMessageTime", chatMessage.time)


        view?.let {
            val contextWrapper = ContextThemeWrapper(this, R.style.MyMenuItemStyle)
            val popupMenu = PopupMenu(contextWrapper, it)
            popupMenu.menuInflater.inflate(R.menu.edit_delete_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.editItem -> {
                        messageField.setText(chatMessage.message)
                        selectedMessageId = chatMessage.id
                        Log.d("MentorChatActivity", "Chat message ID: ${chatMessage.id}")
                        Log.d("MentorChatActivity", "Selected message ID: $selectedMessageId")

                        true
                    }
                    R.id.deleteItem -> {

                        val webserviceHelper = WebserviceHelper(this)
                        webserviceHelper.deleteMessage(chatMessage.id, "mentor_chat")
                        chatAdapter.removeMessage(chatMessage.id)
                        true
//                        if(chatMessage.messageImageUrl.isNotEmpty())
//                            FirebaseManager.deleteMessageInDatabase(chatMessage.id, "mentor_chats", "chat_images", chatId, chatAdapter)
//                        else if(chatMessage.videoImageUrl.isNotEmpty())
//                            FirebaseManager.deleteMessageInDatabase(chatMessage.id, "mentor_chats", "chat_videos", chatId, chatAdapter)
//                        else if(chatMessage.voiceMemoUrl.isNotEmpty())
//                            FirebaseManager.deleteMessageInDatabase(chatMessage.id, "mentor_chats", "chat_audios", chatId, chatAdapter)
//                        else if(chatMessage.documentUrl.isNotEmpty())
//                            FirebaseManager.deleteMessageInDatabase(chatMessage.id, "mentor_chats", "chat_documents", chatId, chatAdapter)
//                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }




    private fun sendMessage() {
        val message = messageField.text.toString()
        val currentTime = java.text.SimpleDateFormat("HH:mm a").format(java.util.Date())
        if (message.isNotEmpty()) {
            if (selectedMessageId != null) {
                // Edit the selected message in the database
                val webserviceHelper = WebserviceHelper(this)
                webserviceHelper.editMessageInMentorChat(selectedMessageId!!, message)
                chatAdapter.editMessage(selectedMessageId!!, message)
                selectedMessageId = null
            } else {
                // Add a new message to the database
                //FirebaseManager.saveMessageToDatabase(message, currentTime, "mentor_chats",chatId , chatAdapter)
                val webserviceHelper = WebserviceHelper(this)
                var messageId= UUID.randomUUID().toString()
                webserviceHelper.sendMessageInMentorChat(ChatMessage(messageId,message,currentTime,true,"","","","",""), currentMentor)
                chatAdapter.addMessage(ChatMessage(messageId,message, currentTime, true, mentorImageUrl,"","","",""))
            }
            // Clear the message field
            messageField.text.clear()
        }
    }



    private val pickImageGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data ?: return@registerForActivityResult
            // Send the image to the chat

            val webserviceHelper = WebserviceHelper(this)
            webserviceHelper.uploadFileToServer("image",selectedImageUri){bool,url->
                Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
                val currentTime= SimpleDateFormat("HH:mm a").format(Date())
                webserviceHelper.sendMessageInMentorChat(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), url, "", "", ""), currentMentor)
                chatAdapter.addMessage(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), url, "", "", ""))

            }

//            FirebaseManager.sendImageToStorage(selectedImageUri, chatId, "mentor_chats",chatAdapter,"chat_images")
        }
    }



    private fun sendImage(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageGalleryLauncher.launch(galleryIntent)
    }

    private val pickFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedFileUri = result.data?.data ?: return@registerForActivityResult
            val fileType = getFileType(selectedFileUri)
            val webserviceHelper = WebserviceHelper(this)
            if (fileType != null) {

                when (fileType) {
                    FileType.IMAGE -> {
                        webserviceHelper.uploadFileToServer("image", selectedFileUri) {bool,url->
                            Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
                            val currentTime= SimpleDateFormat("HH:mm a").format(Date())
                            webserviceHelper.sendMessageInMentorChat(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), url, "", "", ""), currentMentor)
                            chatAdapter.addMessage(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), url, "", "", ""))
                        }
//                        FirebaseManager.sendImageToStorage(selectedFileUri, chatId, "mentor_chats", chatAdapter, "chat_images")
                    }

                    FileType.VIDEO -> {
                        webserviceHelper.uploadFileToServer("video", selectedFileUri) {bool,url->
                            Toast.makeText(this, "Video uploaded", Toast.LENGTH_SHORT).show()
                            val currentTime= SimpleDateFormat("HH:mm a").format(Date())
                            webserviceHelper.sendMessageInMentorChat(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", url, "", ""), currentMentor)
                            chatAdapter.addMessage(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", url, "", ""))

                        }

//                        FirebaseManager.sendImageToStorage(
//                            selectedFileUri,
//                            chatId,
//                            "mentor_chats",
//                            chatAdapter,
//                            "chat_videos"
//                        )
                    }

                    FileType.PDF ->
                    {
                        webserviceHelper.uploadFileToServer("pdf", selectedFileUri) {bool,url->
                            Toast.makeText(this, "PDF uploaded", Toast.LENGTH_SHORT).show()
                            val currentTime= SimpleDateFormat("HH:mm a").format(Date())
                            webserviceHelper.sendMessageInMentorChat(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", "", "", url), currentMentor)
                            chatAdapter.addMessage(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", "", "", url))
                        }
                    }

//                        FirebaseManager.sendImageToStorage(selectedFileUri, chatId, "mentor_chats", chatAdapter, "chat_documents")
                    FileType.AUDIO -> {
                        webserviceHelper.uploadFileToServer("audio", selectedFileUri) {bool,url->
                            Toast.makeText(this, "Audio uploaded", Toast.LENGTH_SHORT).show()
                            val currentTime= SimpleDateFormat("HH:mm a").format(Date())
                            webserviceHelper.sendMessageInMentorChat(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", "", url, ""), currentMentor)
                            chatAdapter.addMessage(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", "", url, ""))
                        }
                    }

//                        FirebaseManager.sendImageToStorage(
//                            selectedFileUri,
//                            chatId,
//                            "mentor_chats",
//                            chatAdapter,
//                            "chat_audio"
//                        )
//                    }
                    }
            } else {

                Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun attachFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        pickFile.launch(intent)
    }

    private fun getFileType(uri: Uri): FileType? {
        val contentResolver = applicationContext.contentResolver
        val mimeType = contentResolver.getType(uri)
        return when {
            mimeType?.startsWith("image") == true -> FileType.IMAGE
            mimeType?.startsWith("video") == true -> FileType.VIDEO
            mimeType?.endsWith("pdf") == true -> FileType.PDF
            mimeType?.startsWith("audio") == true -> FileType.AUDIO
            else -> null
        }
    }

    enum class FileType {
        IMAGE,
        VIDEO,
        PDF,
        AUDIO
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MentorChatActivity", "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        if (resultCode == RESULT_OK) {
            // Check if the result is from the PhotoActivity
            val photoUri = data?.getStringExtra("photoUri")
            if (photoUri != null) {
                val webserviceHelper = WebserviceHelper(this)
                webserviceHelper.uploadFileToServer("image",Uri.parse(photoUri)){ bool,url->
                    Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
                    val currentTime= SimpleDateFormat("HH:mm a").format(Date())
                    webserviceHelper.sendMessageInMentorChat(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), url, "", "", ""), currentMentor)
                }
//                FirebaseManager.sendImageToStorage(Uri.parse(photoUri), chatId, "mentor_chats", chatAdapter, "chat_images")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if(photoTakerManager.getInstance().getImageUrl()!=""){
            val photoUri = photoTakerManager.getInstance().getImageUrl()
            Log.d("MentorChatActivity", "onResume: isTakingPhoto=${photoTakerManager.getInstance().getIsTakingPhoto()}"
            )
            val webserviceHelper = WebserviceHelper(this)
            webserviceHelper.uploadFileToServer("image",Uri.parse(photoUri)){ bool,string->
                Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
                val currentTime= SimpleDateFormat("HH:mm a").format(Date())
                webserviceHelper.sendMessageInMentorChat(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, mentorImageUrl, string, "", "", ""), currentMentor)
                chatAdapter.addMessage(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, mentorImageUrl, string, "", "", ""))
            }
//            FirebaseManager.sendImageToStorage(Uri.parse(photoUri), chatId, "mentor_chats", chatAdapter, "chat_images")
            photoTakerManager.getInstance().setImageUrl("")
        }
    }


    private fun setButtonClickListeners() {

        sendButton.setOnClickListener {
            val message = messageField.text.toString()
            if (message.isNotEmpty()) {
                sendMessage()
                messageField.text.clear()
            }
        }

        recordButton.setOnClickListener {
           // recordVoiceMessage()
        }

        takePhoto.setOnClickListener {

            photoTakerManager.getInstance().setIsTakingPhoto(true)
            val intent= Intent(this, PhotoActivity::class.java)
            startActivity(intent)
        }

        sendImage.setOnClickListener {
           sendImage()
        }

        attachImage.setOnClickListener {
            attachFile()
        }

        findViewById<Button>(R.id.callButton).setOnClickListener {
            val intent = Intent(this, PhoneCallActivity::class.java)
            intent.putExtra("mentor", currentMentor)
            startActivity(intent)
        }

        findViewById<Button>(R.id.videoButton).setOnClickListener {
            val intent = Intent(this, VideoCallActivity::class.java)
            intent.putExtra("mentor", currentMentor)
            startActivity(intent)
        }

        findViewById<Button>(R.id.backbutton).setOnClickListener {
            onBackPressed()
        }

        findViewById<Button>(R.id.takePhoto).setOnClickListener {
            startActivity(Intent(this, PhotoActivity::class.java))
        }
    }

    interface ChatIdCallback {
        fun onChatIdReceived(chatId: String)
        fun onChatIdError(errorMessage: String)
    }


    private fun getChatId(callback: ChatIdCallback) {
        chatId=currentMentor.id+"_"+UserManager.getCurrentUser()?.id
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


    private fun fetchUserMessages(mentorImageUrl: String) {

        val webserviceHelper = WebserviceHelper(this)
        UserManager.getCurrentUser()?.let {
            webserviceHelper.fetchMessagesInMentorChat(it.id,currentMentor.id, currentMentor.getprofilePictureUrl()){
               //iterate through the messages and log them
                chatAdapter.addMessages(it)
                //scrollToBottom()
            }
        }

//        val database = FirebaseDatabase.getInstance()
//        val currentUser = UserManager.getCurrentUser()?.id
//        val chatRef = currentUser?.let { database.getReference("chat").child("mentor_chats").child(chatId).child("messages") }
//
//        chatRef?.addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                val message = dataSnapshot.child("message").value as String
//                val time = dataSnapshot.child("time").value as String
//                val date = dataSnapshot.child("date").value as String
//                val userId = dataSnapshot.child("userId").value as String
//                val messageId= dataSnapshot.key.toString()
//                val isCurrentUser = userId == currentUser
//
//                //if message is not by current user set "isRead" to true
//                if(!isCurrentUser){
//                    dataSnapshot.child("isRead").ref.setValue(true)
//                }
//
//                var messageImageUrl=""
//                var messageVideoUrl=""
//                var messageAudioUrl=""
//                var messageDocumentUrl=""
//
//                if(dataSnapshot.child("messageImageUrl").exists())
//                    messageImageUrl= dataSnapshot.child("messageImageUrl").value as String
//                if(dataSnapshot.child("messageVideoUrl").exists())
//                    messageVideoUrl= dataSnapshot.child("messageVideoUrl").value as String
//                if(dataSnapshot.child("messageAudioUrl").exists())
//                    messageAudioUrl= dataSnapshot.child("messageAudioUrl").value as String
//                if(dataSnapshot.child("messageDocumentUrl").exists())
//                    messageDocumentUrl= dataSnapshot.child("messageDocumentUrl").value as String
//
//                chatAdapter.addMessage(ChatMessage(messageId,message, time, isCurrentUser, mentorImageUrl,messageImageUrl,messageVideoUrl,messageAudioUrl,messageDocumentUrl))
//                scrollToBottom()
//
//            }
//
//            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                chatAdapter.editMessage(dataSnapshot.key.toString(), dataSnapshot.child("message").value as String)
//            }
//
//            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
//                chatAdapter.removeMessage(dataSnapshot.key.toString())
//            }
//
//            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                // Handle child moved event if needed
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.e("MentorChatActivity", "Failed to retrieve chat messages: ${databaseError.message}")
//            }
//        })
    }


    override fun onScreenCaptured(path: String) {
        Log.d("MentorChatActivity", "Screenshot captured: $path")

    }

    override fun onScreenCapturedWithDeniedPermission() {
        Log.d("MentorChatActivity", "Screenshot captured with denied permission")
        requestReadExternalStoragePermission()
        var firebaseManager = FirebaseManager()
        firebaseManager.addNotificationToOtherUserInMentorChat(currentMentor.id, "Mentors","Screenshot taken by ${UserManager.getCurrentUser()?.id}", "Screenshot taken",this)

    }

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 3009
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION -> {
                if (grantResults.getOrNull(0) == PackageManager.PERMISSION_DENIED) {
                    showReadExternalStoragePermissionDeniedMessage()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    private fun checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestReadExternalStoragePermission()
        }
    }

    private fun requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION)
    }

    private fun showReadExternalStoragePermissionDeniedMessage() {
        Toast.makeText(this, "Read external storage permission has denied", Toast.LENGTH_SHORT).show()
    }

    private fun scrollToBottom() {
        recyclerView.post {
            // Scroll to the last item in the adapter
            recyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
        }
    }

}
