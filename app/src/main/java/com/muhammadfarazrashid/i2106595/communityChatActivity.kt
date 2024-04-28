package com.muhammadfarazrashid.i2106595

import CommunityChatMessagesDBHelper
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
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
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.muhammadfarazrashid.i2106595.dataclasses.FirebaseManager
import com.muhammadfarazrashid.i2106595.dataclasses.User
import com.muhammadfarazrashid.i2106595.managers.NetworkChangeReceiver
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper
import com.muhammadfarazrashid.i2106595.managers.photoTakerManager
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.sql.Struct
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class communityChatActivity : AppCompatActivity(), ScreenshotDetectionDelegate.ScreenshotDetectionListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var mentorName: TextView
    private lateinit var currentMentor: Mentor
    private lateinit var mentorImage: ImageView
    private lateinit var sendButton: Button
    private lateinit var recordButton: RecordButton
    private lateinit var messageField: EditText
    private lateinit var takePhoto: Button
    private lateinit var sendImage: Button
    private lateinit var attachImage: Button
    private lateinit var listOfUsers: ArrayList<User>
    private var selectedMessageId: String? = null
    private lateinit var selectedImageUri: Uri
    private lateinit var recordView: RecordView
    private var audioRecorder: AudioRecorder? = null
    private var recordFile: File? = null

    private lateinit var dbHelper: CommunityChatMessagesDBHelper
    private lateinit var networkChangeReceiver: NetworkChangeReceiver
    private val screenshotDetectionDelegate = ScreenshotDetectionDelegate(this, this)

    override fun onStart() {
        super.onStart()
        screenshotDetectionDelegate.startScreenshotDetection()
    }

    override fun onStop() {
        super.onStop()
        screenshotDetectionDelegate.stopScreenshotDetection()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkChangeReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.communitychat)
        checkReadExternalStoragePermission()

        initRecording()


        recyclerView = findViewById(R.id.communityChatRecyclerView)

        currentMentor = intent.getParcelableExtra<Mentor>("mentor")!!
        initViews()
        setMentorDetails(currentMentor)
        dbHelper = CommunityChatMessagesDBHelper(this)
        listOfUsers= fetchAllUsersInCommunityChat()
        setButtonClickListeners()
        setBottomNavigationListener()
        setAddMentorClickListener()
    }

    private fun initRecording(){

        audioRecorder = AudioRecorder()
        recordView = findViewById(R.id.record_view)
        recordButton = findViewById(R.id.recordButton)
        
        recordButton.setRecordView(recordView)


        recordButton.setOnRecordClickListener {
            Toast.makeText(this@communityChatActivity, "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@communityChatActivity, "OnStartRecord", Toast.LENGTH_SHORT).show()
            }

            override fun onCancel() {
                stopRecording(true)
                onFinishSetupVisibilities()
                Toast.makeText(this@communityChatActivity, "onCancel", Toast.LENGTH_SHORT).show()
                Log.d("RecordView", "onCancel")
            }

            override fun onFinish(recordTime: Long, limitReached: Boolean) {
                stopRecording(false)
                onFinishSetupVisibilities()
                val time = getHumanTimeText(recordTime)
                Toast.makeText(
                    this@communityChatActivity,
                    "onFinishRecord - Recorded Time is: " + time + " File saved at " + recordFile!!.path,
                    Toast.LENGTH_SHORT
                ).show()
                val webserviceHelper = WebserviceHelper(this@communityChatActivity)
                webserviceHelper.uploadFileToServer("audio", Uri.fromFile(recordFile)){ bool,url->
                    Toast.makeText(this@communityChatActivity, "Audio uploaded", Toast.LENGTH_SHORT).show()
                    val currentTime= SimpleDateFormat("HH:mm a").format(Date())
                    webserviceHelper.sendMessageInCommunityChat(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", "", url, ""), currentMentor.id)
                    chatAdapter.addMessage(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", "", url, ""))

                }
                for(user in listOfUsers){
                    if(user.fcmToken!="")
                        FirebaseManager.sendNotification(UserManager.getCurrentUser()?.name.toString(),"Voice memo sent",currentMentor.id,user.fcmToken,"community_chats",user.name)
                }
                Log.d("RecordView", "onFinish Limit Reached? $limitReached")
                if (time != null) {
                    Log.d("RecordTime", time)
                }
            }

            override fun onLessThanSecond() {
                stopRecording(true)
                onFinishSetupVisibilities()
                Toast.makeText(this@communityChatActivity, "OnLessThanSecond", Toast.LENGTH_SHORT).show()
                Log.d("RecordView", "onLessThanSecond")
            }

            override fun onLock() {
                onFinishSetupVisibilities()
                Toast.makeText(this@communityChatActivity, "onLock", Toast.LENGTH_SHORT).show()
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
                this@communityChatActivity,
                Manifest.permission.RECORD_AUDIO
            ) == PermissionChecker.PERMISSION_GRANTED
            if (recordPermissionAvailable) {
                return@RecordPermissionHandler true
            }
            ActivityCompat.requestPermissions(
                this@communityChatActivity, arrayOf(Manifest.permission.RECORD_AUDIO),
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

    override fun onResume() {
        super.onResume()

        if(photoTakerManager.getInstance().getImageUrl()!=""){
            val photoUri = photoTakerManager.getInstance().getImageUrl()
            Log.d("MentorChatActivity", "onResume: isTakingPhoto=${photoTakerManager.getInstance().getIsTakingPhoto()}"
            )
            FirebaseManager.sendImageToStorage(Uri.parse(photoUri), currentMentor.id, "mentor_chats", chatAdapter, "chat_images")
            photoTakerManager.getInstance().setImageUrl("")
        }

        networkChangeReceiver = NetworkChangeReceiver()
        registerReceiver(networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

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
        recordButton = findViewById(R.id.recordButton)
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

        Log.d("MentorActivityTimeDiff", timeDifferenceMinutes.toString())

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
                        // Handle edit message
                        // val editMessageDialog = EditMessageDialog(chatMessage.message, chatMessage.id, this)
                        // editMessageDialog.show(supportFragmentManager, "EditMessageDialog")
                        messageField.setText(chatMessage.message)
                        selectedMessageId = chatMessage.id

                        true
                    }
                    R.id.deleteItem -> {

                        val webserviceHelper = WebserviceHelper(this)
                        webserviceHelper.deleteMessage(chatMessage.id, "community_chat")
                        chatAdapter.removeMessage(chatMessage.id)
                        true
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
                // Update the message in the database
                val webserviceHelper = WebserviceHelper(this)
                webserviceHelper.editMessageInCommunityChat(selectedMessageId!!, message)
                chatAdapter.editMessage(selectedMessageId!!, message)
                selectedMessageId = null                // Clear the selected message ID
            } else {
                // Add a new message to the database
                val currentUserName=UserManager.getCurrentUser()?.name
                var messageId= UUID.randomUUID().toString()

                if(networkChangeReceiver.isOnline(this)) {
                    val webserviceHelper = WebserviceHelper(this)
                    webserviceHelper.sendMessageInCommunityChat(
                        ChatMessage(
                            messageId,
                            message,
                            currentTime,
                            true,
                            "",
                            "",
                            "",
                            "",
                            ""
                        ), currentMentor.id
                    )

                    dbHelper.sendMessageInCommunityChat(
                        ChatMessage(
                            messageId,
                            message,
                            currentTime,
                            true,
                            "",
                            "",
                            "",
                            "",
                            ""
                        ), currentMentor.id,
                        true)
                }
                else{
                    Log.d("MentorChatActivity", "Saving message to database")
                    dbHelper.sendMessageInCommunityChat(
                        ChatMessage(
                            messageId,
                            message,
                            currentTime,
                            true,
                            "",
                            "",
                            "",
                            "",
                            ""
                        ), currentMentor.id,
                        false
                    )
                }

//                FirebaseManager.saveMessageToDatabase(message, currentTime, "community_chats",currentMentor.id , chatAdapter)
                chatAdapter.addMessage(ChatMessage(messageId,message, currentTime, true, UserManager.getCurrentUser()?.profilePictureUrl ?: ""))
                for (user in listOfUsers){
                    Log.d("SendingNotification","Sending notification to ${user.name} with token ${user.fcmToken}")
                    if (currentUserName != null && user.fcmToken != "") {

                        FirebaseManager.sendNotification(currentUserName,message,currentMentor.id,user.fcmToken,"community_chats",user.name)
                        Log.d("SendingNotification","Sending notification to ${user.name} with token ${user.fcmToken}")

                    }
                }

            }
            // Clear the message field
            messageField.text.clear()
            scrollToBottom()
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
                webserviceHelper.sendMessageInCommunityChat(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), url, "", "", ""), currentMentor.id)
                chatAdapter.addMessage(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), url, "", "", ""))

            }
            for(user in listOfUsers){
                if(user.fcmToken!="")
                    FirebaseManager.sendNotification(UserManager.getCurrentUser()?.name.toString(),"Image sent",currentMentor.id,user.fcmToken,"community_chats",user.name)
            }
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
                            webserviceHelper.sendMessageInCommunityChat(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), url, "", "", ""), currentMentor.id)
                            chatAdapter.addMessage(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), url, "", "", ""))
                        }


                    }
                    FileType.VIDEO -> {

                        webserviceHelper.uploadFileToServer("video", selectedFileUri) {bool,url->
                            Toast.makeText(this, "Video uploaded", Toast.LENGTH_SHORT).show()
                            val currentTime= SimpleDateFormat("HH:mm a").format(Date())
                            webserviceHelper.sendMessageInCommunityChat(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", url, "", ""), currentMentor.id)
                            chatAdapter.addMessage(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", url, "", ""))
                        }

                    }
                    FileType.PDF -> {

                        webserviceHelper.uploadFileToServer("document", selectedFileUri) {bool,url->
                            Toast.makeText(this, "Document uploaded", Toast.LENGTH_SHORT).show()
                            val currentTime= SimpleDateFormat("HH:mm a").format(Date())
                            webserviceHelper.sendMessageInCommunityChat(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", "", "", url), currentMentor.id)
                            chatAdapter.addMessage(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", "", "", url))
                        }

                    }
                        FileType.AUDIO -> {

                            webserviceHelper.uploadFileToServer("audio", selectedFileUri) {bool,url->
                                Toast.makeText(this, "Audio uploaded", Toast.LENGTH_SHORT).show()
                                val currentTime= SimpleDateFormat("HH:mm a").format(Date())
                                webserviceHelper.sendMessageInCommunityChat(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", "", url, ""), currentMentor.id)
                                chatAdapter.addMessage(ChatMessage(UUID.randomUUID().toString(), "",currentTime , true, currentMentor.getprofilePictureUrl(), "", "", url, ""))
                            }

                        }
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

        val webserviceHelper = WebserviceHelper(this)

        webserviceHelper.fetchAllUsersInCommunityChat(currentMentor.id){returnedUsers->
            for(user in returnedUsers){
                users.add(user)
            }
            for(user in users){
                Log.d("Fetching All Users", "User = ${user.name}")
            }

            fetchUserMessages(currentMentor.getprofilePictureUrl())

        }



        return users
    }



    private fun fetchUserMessages(mentorImageUrl: String) {

        val webserviceHelper = WebserviceHelper(this)
        if(networkChangeReceiver.isOnline(this)) {
            UserManager.getCurrentUser()?.let { it1 ->
                webserviceHelper.fetchMessagesInCommunityChat(
                    it1.id,
                    currentMentor.id,
                    currentMentor.getprofilePictureUrl(),
                    listOfUsers
                ) {
                    //iterate through the messages and log them
                    chatAdapter.addMessages(it)
                    scrollToBottom()
                }
            }
        }
        else{
            val messages = UserManager.getCurrentUser()?.let {
                dbHelper.fetchMessagesInCommunityChat(
                    it.id,
                    currentMentor.id,
                    currentMentor.getprofilePictureUrl(),
                    listOfUsers
                ) {
                    //iterate through the messages and log them
                    chatAdapter.addMessages(it)
                    scrollToBottom()
                }
            }
        }
    }




    override fun onScreenCaptured(path: String) {
        Log.d("MentorChatActivity", "Screenshot captured: $path")
        Toast.makeText(this, path, Toast.LENGTH_SHORT).show();

    }

    override fun onScreenCapturedWithDeniedPermission() {
        Log.d("MentorChatActivity", "Screenshot captured with denied permission")
        requestReadExternalStoragePermission()
        var firebaseManager = FirebaseManager()
        firebaseManager.addNotificationToOtherUserInMentorChat(currentMentor.id, "Mentors","Screenshot taken by ${UserManager.getCurrentUser()?.name}", "Screenshot taken",this)
        //get list of user ids
        val userIds = ArrayList<String>()
        for(user in listOfUsers){
            userIds.add(user.id)
        }
        firebaseManager.sendNotificationsToListOfUsers(userIds, "Screenshot taken by ${UserManager.getCurrentUser()?.name}", "Screenshot taken",this)

        for(user in listOfUsers){
            FirebaseManager.sendNotification(UserManager.getCurrentUser()?.name.toString(),"Screenshot taken",currentMentor.id,user.fcmToken,"community_chats",user.name)
        }
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
//           Toast.makeText(this, "Read external storage permission has denied", Toast.LENGTH_SHORT)
    }

    private fun scrollToBottom() {
        recyclerView.post {
            // Scroll to the last item in the adapter
            recyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
        }
    }


}
