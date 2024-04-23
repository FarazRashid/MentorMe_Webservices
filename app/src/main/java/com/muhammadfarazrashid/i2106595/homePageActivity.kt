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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhammadfarazrashid.i2106595.dataclasses.NotificationsManager
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper

class homePageActivity : AppCompatActivity() {

    private lateinit var topMentorsRecycler: RecyclerView
    private lateinit var recentMentorsRecycler: RecyclerView
    private lateinit var educationMentorsRecycler: RecyclerView
    private lateinit var badgesRecycler: RecyclerView
    private lateinit var notifications:ImageView

    private val topMentors = ArrayList<Mentor>()
    private val recentMentors = ArrayList<Mentor>()
    private val educationMentors = ArrayList<Mentor>()
    private val badges = ArrayList<Badge>()
    private lateinit var  nameText: TextView

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)

        initViews()
        initMentors()
        initBadges()
        initBottomNavigation()
        fetchAllMentors()

        val addMentor = findViewById<ImageView>(R.id.addMentorButton)
        addMentor.setOnClickListener {
            startActivity(Intent(this, AddAMentor::class.java))
        }

        notifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

    }


    private fun initViews() {
        topMentorsRecycler = findViewById(R.id.topMentorsRecycler)
        recentMentorsRecycler = findViewById(R.id.recentMentorsRecycler)
        educationMentorsRecycler = findViewById(R.id.educationMentorsRecycler)
        badgesRecycler = findViewById(R.id.badgesRecycler)
        nameText = findViewById(R.id.nameText)
        nameText.text= UserManager.getCurrentUser()?.name ?: "User"
        notifications=findViewById(R.id.bellIcon)
    }

    private fun fetchAllMentors() {

        val webserviceHelper = WebserviceHelper(this)

        webserviceHelper.getMentors { mentors ->
            if (mentors != null) {
                Log.d("homePageActivity", "Fetched mentors: ${mentors.size}")
                setupMentorsRecycler(topMentorsRecycler, mentors)
                setupMentorsRecycler(educationMentorsRecycler, mentors)
                setupMentorsRecycler(recentMentorsRecycler, mentors)

            }
            else{
                Log.d("homePageActivity", "Failed to fetch mentors")
            }

        }
    }

    private fun initMentors() {
        // Initialize top mentors
        // Setup RecyclerViews
        setupMentorsRecycler(topMentorsRecycler, topMentors)
        setupMentorsRecycler(recentMentorsRecycler, recentMentors)
        setupMentorsRecycler(educationMentorsRecycler, educationMentors)
    }

    private fun setupMentorsRecycler(recyclerView: RecyclerView, mentors: List<Mentor>) {
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val mentorAdapter = MentorCardAdapter(this, mentors, R.layout.mentorcard)
        mentorAdapter.setOnItemClickListener { mentor ->
            // Handle click event
            navigateToMentorAbout(mentor)
        }
        recyclerView.adapter = mentorAdapter
    }

    private fun navigateToMentorAbout(mentor: Mentor) {
        val intent = Intent(this, aboutMentorPage::class.java)
        Log.d("homePageActivity", "Navigating to aboutMentorPage with mentor: ${mentor.id}")
        Log.d("homePageActivity", "Navigating to aboutMentorPage with mentor: ${mentor.getprofilePictureUrl()}")
        Log.d("homePageActivity", "Navigating to aboutMentorPage with mentor: ${mentor.rating}")
        intent.putExtra("mentor", mentor) // Pass the mentor data to the aboutMentorPage
        startActivity(intent)
    }

    private fun initBadges() {
        badges.add(Badge("All", false))
        badges.add(Badge("Education", false))
        badges.add(Badge("Tech", true)) // This badge is selected
        badges.add(Badge("Finance", false))
        badges.add(Badge("Health", false))
        badges.add(Badge("Business", false))
        badges.add(Badge("Art", false))
        badges.add(Badge("Science", false))
        badges.add(Badge("Sports", false))


        badgesRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        badgesRecycler.adapter = BadgeAdapter(badges).apply {
            setOnBadgeClickListener(object : BadgeAdapter.OnBadgeClickListener {
                override fun onBadgeClick(position: Int) {
                    Log.d("MainActivity", "Badge clicked at position: $position")
                }
            })
        }
    }

    private fun initBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemReselectedListener { item ->
            when (item.itemId) {
                R.id.menu_search -> startActivity(Intent(this, searchPageActivity::class.java))
                R.id.menu_home -> startActivity(Intent(this, homePageActivity::class.java))
                R.id.menu_chat -> startActivity(Intent(this, mainChatActivity::class.java))
                R.id.menu_profile -> startActivity(Intent(this, MyProfileActivity::class.java))
            }
        }
    }
}
