package com.muhammadfarazrashid.i2106595


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.muhammadfarazrashid.i2106595.Mentor.OnMentorListener
import com.muhammadfarazrashid.i2106595.dataclasses.NotificationsManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.entrancepage)

        //create notification channel


        if(UserManager.getInstance().getUserLoggedInSP(getSharedPreferences("USER_LOGIN", MODE_PRIVATE)) && getIntent().getExtras()!=null){
            var userId = getIntent().getExtras()?.getString("userId")
            var chatType=getIntent().getExtras()?.getString("chatType")
            var chatId=getIntent().getExtras()?.getString("chatId")

            Log.d("MainActivity", "Extras: $userId, $chatType, $chatId")


            if(chatType=="mentor_chats")
            {
                Mentor.getMentorById(userId, object : OnMentorListener {
                    override fun onSuccess(fetchedMentor: Mentor) {
                        // Set mentor when fetched successfully
                        val intent = Intent(
                            this@MainActivity,
                            MentorChatActivity::class.java

                        )
                        val intentMain=Intent(this@MainActivity,mainChatActivity::class.java)
                        intentMain.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intentMain)
                        Log.d("MainActivity", "Mentor: $fetchedMentor")
                        intent.putExtra("mentor", fetchedMentor)
                        startActivity(intent)

                    }

                    override fun onFailure(errorMessage: String) {
                        // Handle failure to fetch mentor details
                        Log.e("AllMessagesChat", "Failed to fetch mentor details: $errorMessage")
                    }
                })

            }

            else if(chatType=="community_chats")
            {
                Mentor.getMentorById(chatId, object : OnMentorListener {
                    override fun onSuccess(fetchedMentor: Mentor) {
                        // Set mentor when fetched successfully
                        val intent = Intent(
                            this@MainActivity,
                            communityChatActivity::class.java
                        )
                        val intentMain=Intent(this@MainActivity,mainChatActivity::class.java)
                        intentMain.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intentMain)
                        intent.putExtra("mentor", fetchedMentor)
                        startActivity(intent)

                    }

                    override fun onFailure(errorMessage: String) {
                        // Handle failure to fetch mentor details
                        Log.e("AllMessagesChat", "Failed to fetch mentor details: $errorMessage")
                    }
                })

            }
        }
        else{
            Log.d("MainActivity", "No extras")
        }


        //create notification channel
        NotificationsManager.getInstance().createNotificationChannel(this)

        //if user logged in, go to home page
        if (UserManager.getInstance().getUserLoggedInSP(getSharedPreferences("USER_LOGIN", MODE_PRIVATE))) {

            UserManager.getInstance().getUserEmailSP(getSharedPreferences("USER_LOGIN", MODE_PRIVATE))?.let {

                val intent = Intent(this, homePageActivity::class.java)

                UserManager.getInstance().fetchAndSetCurrentUser(it,this)
                {
                    startActivity(intent)
                    finish()
                }

            }
        } else {
            // Delay for 5000 milliseconds (5 seconds)
            Handler().postDelayed({
                val intent = Intent(this, loginActivity::class.java)
                startActivity(intent)
                finish()
            }, 5000)
        }
    }


}
