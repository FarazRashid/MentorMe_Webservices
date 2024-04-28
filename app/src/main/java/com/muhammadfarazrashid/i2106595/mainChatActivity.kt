package com.muhammadfarazrashid.i2106595

import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
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
import com.muhammadfarazrashid.i2106595.managers.NetworkChangeReceiver
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper
import com.muhammadfarazrashid.i2106595.managers.communityChatsDBHelper
import com.muhammadfarazrashid.i2106595.managers.mentorChatsDBHelper

class mainChatActivity : AppCompatActivity() {

    private lateinit var allMessagesRecyclerView: RecyclerView
    private lateinit var allMessagesAdapter: AllMessagesAdapter
    private lateinit var mentorRecyclerView: RecyclerView
    private lateinit var mentorAdapter: MentorAdapter

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkChangeReceiver)
    }

    override fun onResume() {
        super.onResume()
        networkChangeReceiver = NetworkChangeReceiver()
        registerReceiver(networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainchatpage)

        initializeBottomNavigationListener()
        networkChangeReceiver = NetworkChangeReceiver()
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
            if(networkChangeReceiver.isOnline(this)) {
                webserviceHelper.fetchMentorChats(it.id) { mentorItems ->
                    initializeAllMessagesRecyclerView(mentorItems)
                }
            }
            else {
                val mentorChatsDBHelper=mentorChatsDBHelper(this)
                mentorChatsDBHelper.fetchMentorChats(it.id) { mentorItems ->
                    initializeAllMessagesRecyclerView(mentorItems)
            }
            }
        }

    }




    private fun fetchCommunityChats() {
        val webserviceHelper = WebserviceHelper(this)
        UserManager.getCurrentUser()?.let {
            if (networkChangeReceiver.isOnline(this)) {
                webserviceHelper.fetchCommunityChats(it.id) { mentorItems ->
                    initializeMentorRecyclerView(mentorItems)
                }
            } else {
                val communityChatsDBHelper = communityChatsDBHelper(this)
                communityChatsDBHelper.fetchCommunityChats(it.id) { mentorItems ->
                    initializeMentorRecyclerView(mentorItems)
                }

            }

        }
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
