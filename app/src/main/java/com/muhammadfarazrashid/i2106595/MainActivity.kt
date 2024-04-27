package com.muhammadfarazrashid.i2106595


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.muhammadfarazrashid.i2106595.Mentor.OnMentorListener
import com.muhammadfarazrashid.i2106595.dataclasses.NotificationsManager
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper

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

            val webserviceHelper = WebserviceHelper(this)

            if(chatType=="mentor_chats")
            {
                if (userId != null) {
                    webserviceHelper.getMentorById(userId) {Mentor->
                        // Set mentor when fetched successfully
                        val intent = Intent(
                            this@MainActivity,
                            MentorChatActivity::class.java

                        )
                        val intentMain=Intent(this@MainActivity,mainChatActivity::class.java)
                        intentMain.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intentMain)
                        Log.d("MainActivity", "Mentor: $Mentor")
                        intent.putExtra("mentor", Mentor)
                        startActivity(intent)


                    }
                }

            }

            else if(chatType=="community_chats")
            {
                if (chatId != null) {
                    webserviceHelper.getMentorById(chatId) {

                        // Set mentor when fetched successfully
                        val intent = Intent(
                            this@MainActivity,
                            communityChatActivity::class.java
                        )
                        val intentMain=Intent(this@MainActivity,mainChatActivity::class.java)
                        intentMain.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intentMain)
                        intent.putExtra("mentor", it)
                        startActivity(intent)


                    }
                }

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
