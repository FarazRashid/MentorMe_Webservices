package com.muhammadfarazrashid.i2106595


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhammadfarazrashid.i2106595.UserManager.getInstance
import com.muhammadfarazrashid.i2106595.dataclasses.FirebaseManager
import com.muhammadfarazrashid.i2106595.dataclasses.NotificationsManager.showNotification
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class calendarPage : AppCompatActivity() {

    private lateinit var badgesRecycler: RecyclerView
    private lateinit var badgeAdapter: BadgeAdapter
    private lateinit var mentorName: TextView
    private lateinit var mentorImage: ImageView
    private lateinit var salary: TextView
    private lateinit var currentMentor: Mentor
    private lateinit var calenderView: CalendarView
    private var selectedTime: String = ""
    private var dateString: String= ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calenderpage)
        currentMentor = intent.getParcelableExtra<Mentor>("mentor")!!
        initializeViews()
        setMentorDetails(currentMentor)
        setUpAvailability()
        setUpOnClickListeners()
    }

    private fun setMentorDetails(mentor: Mentor) {
        mentorName.text = mentor.name
        currentMentor = mentor
        Log.d("MentorDetails", "Mentor name: ${mentor.getprofilePictureUrl()}")
        if (mentor.getprofilePictureUrl().isNotEmpty()) {
            Picasso.get().load(mentor.getprofilePictureUrl()).into(mentorImage)
        }
        salary.text = mentor.salary
    }

    private fun setUpAvailability() {
        // Set up badges
        val badges = arrayListOf(
            Badge("11:00 AM", false),
            Badge("12:00 PM", false),
            Badge("10:00 AM", true) // This badge is selected
        )

        badgeAdapter = BadgeAdapter(badges)
        badgesRecycler.adapter = badgeAdapter

        // Set the badge click listener
        badgeAdapter.setOnBadgeClickListener(object : BadgeAdapter.OnBadgeClickListener {
            override fun onBadgeClick(position: Int) {
                // Handle badge click
                Log.d("CalendarPage", "Badge clicked at position: $position")
                selectedTime= badges[position].name
            }
        })
    }

    private fun initializeViews() {
        badgesRecycler = findViewById(R.id.badgesRecycler)
        badgesRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mentorName= findViewById(R.id.mentorName)
        mentorImage = findViewById(R.id.imageView9)
        calenderView = findViewById(R.id.calendarView)
        salary = findViewById(R.id.salary)

    }




    private fun registerForMentorChat() {
        val webserviceHelper= WebserviceHelper(this)
        UserManager.getCurrentUser()
            ?.let { webserviceHelper.registerForMentorChat(it.id,currentMentor.id) }
    }

    private fun navigateToChatPage(mentor: Mentor) {
        val intent = Intent(this, MentorChatActivity::class.java)
        registerForMentorChat()
        intent.putExtra("mentor",mentor)
        startActivity(intent)
    }

    private fun checkFiels(): Boolean{
        if(selectedTime.isEmpty()){
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show()
            return false
        }
        //check if calender is selected

        if(calenderView.date == 0L){
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }


    private fun setUpOnClickListeners()
    {
        findViewById<ImageView>(R.id.chatButton).setOnClickListener {
            navigateToChatPage(currentMentor)
        }

        findViewById<ImageView>(R.id.phoneButton).setOnClickListener {
            val intent = Intent(this, PhoneCallActivity::class.java)
            intent.putExtra("mentor", currentMentor)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.cameraButton).setOnClickListener {
            val intent = Intent(this, VideoCallActivity::class.java)
            intent.putExtra("mentor", currentMentor)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.imageView4).setOnClickListener {
            onBackPressed()
        }

        calenderView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val selectedDate = calendar.time
            Log.d("CalendarPage", "Selected Date: $selectedDate")

            // Convert selectedDate to a string if needed
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateString = dateFormat.format(selectedDate)
            Log.d("CalendarPage", "Selected Date String: $dateString")

            // Now you can use the selectedDate or dateString as needed
        }

        findViewById<Button>(R.id.bookAnAppointmentButton).setOnClickListener {

            if(checkFiels()){
                val intent = Intent(this, homePageActivity::class.java)
                val webserviceHelper= WebserviceHelper(this)
                UserManager.getCurrentUser()?.let { it1 ->webserviceHelper.addBooking(it1.id,currentMentor.id,dateString,selectedTime) }
                showNotification(
                    applicationContext,
                    "Appointment Booked with ${currentMentor.name} for $dateString at $selectedTime",
                )
                UserManager.getCurrentUser()?.let { it1 -> webserviceHelper.addNotification(it1.id,"Appointment Booked with ${currentMentor.name} for $dateString at $selectedTime","Appointment") }
                val firebaseManager = FirebaseManager()

                firebaseManager.addNotificationToUser(
                    getInstance().getCurrentUser()!!.id,
                    "Mentor " + currentMentor.getName() + " has been added to your favorites",
                    "Favourites",
                    this
                )

                startActivity(intent)
            }
        }

    }
}
