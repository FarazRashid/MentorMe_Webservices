package com.muhammadfarazrashid.i2106595

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhammadfarazrashid.i2106595.managers.NetworkChangeReceiver
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper
import com.muhammadfarazrashid.i2106595.managers.communityChatsDBHelper
import com.squareup.picasso.Picasso

class aboutMentorPage : AppCompatActivity() {

    private lateinit var mentorName: TextView
    private lateinit var mentorPosition: TextView
    private lateinit var aboutMe: TextView
    private lateinit var mentorImageView: ImageView
    private lateinit var currentMentor: Mentor
    private lateinit var rating: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aboutmentorpage)


        // Retrieve Mentor object from intent extras
        val mentor = intent.getParcelableExtra<Mentor>("mentor")

        // Log mentor details to verify if data is received correctly
        mentor?.let {
            with(it) {
                logMentorDetails(id, name, position, availability, salary, description, isFavorite, getprofilePictureUrl())
            }
        }

        // Initialize views
        initViews()

        // Set mentor details
        mentor?.let { setMentorDetails(it) }


        // Set up onClickListeners
        setUpOnClickListeners()
    }

    private fun initViews() {
        mentorName = findViewById(R.id.mentorName)
        mentorPosition = findViewById(R.id.position)
        aboutMe = findViewById(R.id.aboutMe)
        mentorImageView = findViewById(R.id.imageView9)
        rating = findViewById(R.id.rating)
    }

    private fun setMentorDetails(mentor: Mentor) {
        mentorName.text = mentor.name
        mentorPosition.text = mentor.position
        aboutMe.text = mentor.description
        currentMentor = mentor

        Log.d("AboutMentorPage", "Mentor Rating: ${mentor.rating}")

        if (mentor.rating != null) {
            rating.text = mentor.rating.toString()
        } else {
            rating.text = "0 (No Reviews So Far)"
        }


        if (mentor.getprofilePictureUrl().isNotEmpty()) {
            Picasso.get().load(mentor.getprofilePictureUrl()).into(mentorImageView)
        }
    }

    private fun Mentor.logMentorDetails(
        id: String?,
        name: String?,
        position: String?,
        availability: String?,
        salary: String?,
        description: String?,
        isFavorite: Boolean,
        profilePictureUrl: String?
    ) {
        id?.let { Log.d("AboutMentorPage", "Mentor ID: $it") }
        name?.let { Log.d("AboutMentorPage", "Mentor Name: $it") }
        position?.let { Log.d("AboutMentorPage", "Mentor Position: $it") }
        availability?.let { Log.d("AboutMentorPage", "Mentor Availability: $it") }
        salary?.let { Log.d("AboutMentorPage", "Mentor Salary: $it") }
        description?.let { Log.d("AboutMentorPage", "Mentor Description: $it") }
        Log.d("AboutMentorPage", "Mentor isFavorite: $isFavorite")
        profilePictureUrl?.let { Log.d("AboutMentorPage", "Mentor Profile Picture URL: $it") }
    }





    private fun registerForCommunityChat() {
        val webserviceHelper= WebserviceHelper(this)
        UserManager.getCurrentUser()
            ?.let { webserviceHelper.registerForCommunityChat(it.id,currentMentor.id) }
        val communityChats= communityChatsDBHelper(this)
        communityChats.addChat(UserManager.getCurrentUser()!!.id,currentMentor.id)
    }


    private fun navigateToMentorReviewPage(mentor: Mentor) {
        val intent = Intent(this, reviewpage::class.java)
        intent.putExtra("mentor", mentor)
        startActivity(intent)
    }

    private fun navigateToCommunityChatPage(mentor: Mentor) {
        val intent = Intent(this, communityChatActivity::class.java)
        registerForCommunityChat()
        intent.putExtra("mentor",mentor)
        startActivity(intent)
    }

    private fun navigateToSignUpPage(mentor: Mentor) {
        val intent = Intent(this, calendarPage::class.java)
        intent.putExtra("mentor", mentor)
        startActivity(intent)
    }

    private fun setUpOnClickListeners() {
        findViewById<ImageView>(R.id.imageView4).setOnClickListener { onBackPressed() }
        findViewById<Button>(R.id.reviewButton).setOnClickListener {
            navigateToMentorReviewPage(currentMentor)
        }
        findViewById<Button>(R.id.communityButton).setOnClickListener {
            navigateToCommunityChatPage(currentMentor)
        }
        findViewById<Button>(R.id.signupButton).setOnClickListener {
            navigateToSignUpPage(currentMentor)
        }
    }
}
