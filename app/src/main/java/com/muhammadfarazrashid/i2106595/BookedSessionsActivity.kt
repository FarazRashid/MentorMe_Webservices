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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper

class BookedSessionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SessionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booked_sessions)

        recyclerView = findViewById(R.id.bookedSessionsRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupSessions()
        setupBottomNavigation()
        setupAddMentorButton()
        setupBackButton()
    }

    //call the firebase database and fetch all the bookings underneath the user's id with booking date, time, and id of the mentor
    private fun setupSessions() {
        val webserviceHelper = WebserviceHelper(this)
        val userId = UserManager.getCurrentUser()?.id

        userId?.let {
            webserviceHelper.getBookings(it) { sessions ->
                if (sessions != null) {
                    // Get list of mentor IDs from sessions
                    val mentorIds = sessions.map { session -> session.mentorId }
                    setupMentors(sessions, mentorIds.toMutableList())
                } else {
                    Log.e("BookedSessionsActivity", "Error getting sessions")
                }
            }
        }
    }

    private fun setupMentors(sessions: MutableList<Session>, listOfIdsOfMentors: MutableList<String>) {
        val webserviceHelper = WebserviceHelper(this)

        webserviceHelper.getMentors { mentors ->
            if (mentors != null) {
                for (mentor in mentors) {
                    if (listOfIdsOfMentors.contains(mentor.id)) {
                        // Find the session with the mentor id and add the mentor to the session
                        val index = listOfIdsOfMentors.indexOf(mentor.id)
                        sessions[index].mentor = mentor
                    }
                }
                setupAdapter(sessions)
            } else {
                Log.e("BookedSessionsActivity", "Error getting mentors")
            }
        }
    }

    private fun setupAdapter(sessions: MutableList<Session>) {
        adapter = SessionAdapter(sessions)
        recyclerView.adapter = adapter
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
            intent?.let {
                startActivity(it)
            }
        }
    }

    private fun setupAddMentorButton() {
        val addMentor = findViewById<ImageView>(R.id.addMentorButton)
        addMentor.setOnClickListener {
            val intent = Intent(this, AddAMentor::class.java)
            startActivity(intent)
        }
    }

    private fun setupBackButton() {
        val imageView10 = findViewById<ImageView>(R.id.imageView10)
        imageView10.setOnClickListener {
            onBackPressed()
        }
    }
}
