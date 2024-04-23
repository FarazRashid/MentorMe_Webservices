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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

    private fun isAlreadyRegisteredForChat(callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val currentUser = UserManager.getCurrentUser()
        val userChatRef = database.getReference("users/${currentUser?.id}/chats/mentor_chats")
        userChatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var isRegistered = false
                    for (snapshot in dataSnapshot.children) {
                        val mentorId = snapshot.value as String
                        if (mentorId == currentMentor.id) {
                            Log.d("CalendarPage", "Is already registered for chat: $isRegistered")
                            isRegistered = true
                            break
                        }
                    }
                    Log.d("CalendarPage", "Is already registered for chat: $isRegistered")
                    callback(isRegistered)
                } else {
                    callback(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Log.e("CalendarPage", "Error fetching user chat data: ${databaseError.message}")
                callback(false)
            }
        })
    }


    private fun registerForMentorChat() {
        val database = FirebaseDatabase.getInstance()
        val chatRef = database.getReference("chat").push()
        val chatKey = chatRef.key
        val currentUser = UserManager.getCurrentUser()

        // Save chat reference under user's chats
        currentUser?.let { user ->
            val userChatRef = database.getReference("users/${user.id}/chats/mentor_chats")
            chatKey?.let { key ->
                userChatRef.child(key).setValue(currentMentor.id)
            }
        }

        // Save chat reference under mentor's chats
        val mentorChatRef = database.getReference("Mentors/${currentMentor.id}/chats/mentor_chats")
        chatKey?.let { key ->
            mentorChatRef.child(key).setValue(currentUser?.id)
        }

        // Optionally, save chat details under the chat node
        chatKey?.let { key ->
            val chatDetailsRef = database.getReference("chat/mentor_chats/$key/details")
            chatDetailsRef.child("mentor_id").setValue(currentMentor.id)
            currentUser?.id?.let { userId ->
                chatDetailsRef.child("user_id").setValue(userId)
            }
        }
    }

    private fun navigateToChatPage(mentor: Mentor) {
        val intent = Intent(this, MentorChatActivity::class.java)
        isAlreadyRegisteredForChat { isRegistered ->
            if (isRegistered) {
                // User is already registered for chat with the current mentor
                Log.d("CalendarPage", "User is already registered for chat with mentor")
            } else {
                // Register the user for chat with the current mentor
                registerForMentorChat()
                Log.d("CalendarPage", "User is now registered for chat with mentor")
            }
            intent.putExtra("mentor", mentor)
            startActivity(intent)
        }
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

                startActivity(intent)
            }
        }

    }
}
