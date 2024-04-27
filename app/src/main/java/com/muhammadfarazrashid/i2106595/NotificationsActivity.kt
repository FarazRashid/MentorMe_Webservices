package com.muhammadfarazrashid.i2106595

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.muhammadfarazrashid.i2106595.dataclasses.FirebaseManager
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper

class NotificationsActivity : AppCompatActivity() {

    private lateinit var notificationsRecycler: RecyclerView
    private lateinit var notificationsAdapter: RecentSearchesAdapter
    private val notificationsList = mutableListOf<String>()
    private val notificationsIdList= mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        setupViews()
        setupBottomNavigation()
        setupBackButton()
        setupNotificationsRecyclerView()
        setupClearAllButton()
        fetchAllUserNotifications() // Fetch notifications when activity is created
    }

    private fun setupViews() {
        notificationsRecycler = findViewById(R.id.notificationsRecyclerView)
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemReselectedListener { item ->
            val intent = when (item.itemId) {
                R.id.menu_search -> Intent(this, searchPageActivity::class.java)
                R.id.menu_home -> Intent(this, homePageActivity::class.java)
                R.id.menu_chat -> Intent(this, mainChatActivity::class.java)
                R.id.menu_profile -> Intent(this, MyProfileActivity::class.java)
                else -> null
            }
            intent?.let { startActivity(it) }
        }
    }

    private fun setupBackButton() {
        val backButton = findViewById<ImageView>(R.id.imageView10)
        backButton.setOnClickListener { onBackPressed() }
    }

    private fun fetchAllUserNotifications() {
        val currentUser = UserManager.getCurrentUser()
        currentUser?.let { user ->
//            val myDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.id).child("notifications")
            val webserviceHelper = WebserviceHelper(this)
            webserviceHelper.getNotifications(currentUser.id){
                notifications -> notifications.forEach {
                    notificationsList.add(it.notification)
                notificationsIdList.add(it.id)
                notificationsAdapter.notifyDataSetChanged()
                }
            }
//            myDatabase.addChildEventListener(object : ChildEventListener {
//                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                    val notification = dataSnapshot.child("notification").value.toString()
//                    val notificationId= dataSnapshot.key.toString()
//                    notification.let {
//                        Log.d("NotificationsActivity", "Adding notification to list: $it")
//                        notificationsList.add(it)
//                        notificationsIdList.add(notificationId)
//                        notificationsAdapter.notifyDataSetChanged() // Notify adapter when new notification is added
//                    }
//                }
//
//                override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                    // Handle changes to notifications
//                }
//
//                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
//                    // Handle removed notifications
//                }
//
//                override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                    // Handle moved notifications
//                }
//
//                override fun onCancelled(databaseError: DatabaseError) {
//                    // Handle database error
//                }
//            })
        }
    }

    private fun setupNotificationsRecyclerView() {
        notificationsAdapter = RecentSearchesAdapter(notificationsList, onRemoveClickListener, "notifications")
        //set up recycler view from the opposite stacking order
        notificationsRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        notificationsRecycler.adapter = notificationsAdapter
    }

    private fun setupClearAllButton() {
        val clearAllButton = findViewById<TextView>(R.id.filterSpinner)
        clearAllButton.setOnClickListener { clearAllNotifications() }
    }

    private fun clearAllNotifications() {
        notificationsList.clear()
        notificationsAdapter.notifyDataSetChanged()
        UserManager.getCurrentUser()?.let {
            val webserviceHelper = WebserviceHelper(this)
            webserviceHelper.deleteAllMessagesOfUser(it.id)
        }
    }

    private val onRemoveClickListener = RecentSearchesAdapter.OnRemoveClickListener { position ->
        notificationsAdapter.removeRecentSearch(position)
        Log.d("NotificationsActivity", "Removing notification at position $position from list ${notificationsIdList[position]}")
        UserManager.getCurrentUser()
            ?.let {
                val webserviceHelper = WebserviceHelper(this)
                webserviceHelper.deleteNotification(notificationsIdList[position])
            }

    }
}
