package com.muhammadfarazrashid.i2106595.managers

import CommunityChatMessagesDBHelper
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.muhammadfarazrashid.i2106595.ChatMessage

class NetworkChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (isOnline(context)) {
            // Fetch unsent messages and send them to the server
            val dbHelper = CommunityChatMessagesDBHelper(context)
            val pair = dbHelper.fetchUnsentMessages(false)
            val unsentMessages = pair.first
            val chatMessageId = pair.second
            val webserviceHelper = WebserviceHelper(context)
            for (message in unsentMessages) {
                webserviceHelper.sendMessageInCommunityChat(message,chatMessageId)
            }
        }
    }
    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}