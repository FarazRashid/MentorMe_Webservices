package com.muhammadfarazrashid.i2106595

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhammadfarazrashid.i2106595.dataclasses.FirebaseManager
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper

class mainChatActivity : AppCompatActivity() {

    private lateinit var allMessagesRecyclerView: RecyclerView
    private lateinit var allMessagesAdapter: AllMessagesAdapter
    private lateinit var mentorRecyclerView: RecyclerView
    private lateinit var mentorAdapter: MentorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainchatpage)

        initializeBottomNavigationListener()
        initializeAddMentorButton()

        fetchMentorChats()
        fetchCommunityChats()

        initializeBackButton()
    }

    private fun initializeBottomNavigationListener() {
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

    private fun initializeAddMentorButton() {
        findViewById<ImageView>(R.id.addMentorButton).setOnClickListener {
            startActivity(Intent(this, AddAMentor::class.java))
        }
    }




    private fun fetchMentorChats() {

        val webserviceHelper= WebserviceHelper(this)
        UserManager.getCurrentUser()?.let {
            webserviceHelper.fetchMentorChats(it.id) { mentorItems ->
                initializeAllMessagesRecyclerView(mentorItems)
            }
        }

//        val mentorChats = mutableListOf<AllMessagesChat>()
//        val currentUser = UserManager.getCurrentUser()?.id
//        val myDatabase = FirebaseDatabase.getInstance().getReference("users/$currentUser/chats/mentor_chats")



        // Read data from Firebase Realtime Database
//        myDatabase.addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                // Get mentor ID from each child and add it to the mentorItems list
//                val mentorId = dataSnapshot.value as? String
//                mentorId?.let {
//                    mentorChats.add(AllMessagesChat(it, dataSnapshot.key.toString()))
//                    // After fetching all data, initialize mentorRecyclerView
//                    initializeAllMessagesRecyclerView(mentorChats)
//                }
//            }
//
//            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                // Handle child change if needed
//            }
//
//            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
//                // Handle child removal if needed
//            }
//
//            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                // Handle child movement if needed
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Handle database error
//                Log.e("FetchMentorChats", "Error fetching mentor chats: ${databaseError.message}")
//            }
//        })
    }




    private fun fetchCommunityChats() {
         val webserviceHelper= WebserviceHelper(this)
        UserManager.getCurrentUser()?.let {
            webserviceHelper.fetchCommunityChats(it.id) { mentorItems ->
                initializeMentorRecyclerView(mentorItems)
            }
        }

//        val communityChats = mutableListOf<MentorItem>()
//        val currentUser = UserManager.getCurrentUser()?.id
//        val myDatabase = FirebaseDatabase.getInstance().getReference("users/$currentUser/chats/community_chats")
//
//        // Read data from Firebase Realtime Database
//        myDatabase.addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                // Get mentor ID from each child and add it to the mentorItems list
//                val mentorId = dataSnapshot.value as? String
//                mentorId?.let {
//                    Log.d("FetchCommunityChats", "Mentor ID: $it")
//                    communityChats.add(MentorItem(it, false))
//                    // After fetching all data, initialize mentorRecyclerView
//                    initializeMentorRecyclerView(communityChats)
//                }
//            }
//
//            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                // Handle child change if needed
//            }
//
//            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
//                // Handle child removal if needed
//            }
//
//            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                // Handle child movement if needed
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Handle database error
//                Log.e("FetchCommunityChats", "Error fetching community chats: ${databaseError.message}")
//            }
//        })
    }

    private fun initializeAllMessagesRecyclerView(MentorItems: MutableList<AllMessagesChat>) {
        allMessagesRecyclerView = findViewById(R.id.allMessagesRecycler)
        allMessagesRecyclerView.layoutManager = LinearLayoutManager(this)

        allMessagesAdapter = AllMessagesAdapter(MentorItems)
        allMessagesRecyclerView.adapter = allMessagesAdapter
    }


    private fun initializeMentorRecyclerView(mentorItems: List<MentorItem>) {
        mentorRecyclerView = findViewById(R.id.communityRecyclerView)
        mentorRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        mentorAdapter = MentorAdapter(mentorItems)
        mentorRecyclerView.adapter = mentorAdapter
    }



    private fun initializeBackButton() {
        findViewById<ImageView>(R.id.imageView10).setOnClickListener {
            onBackPressed()
        }
    }
}
