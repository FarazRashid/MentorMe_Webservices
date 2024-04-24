package com.muhammadfarazrashid.i2106595;

import android.util.Log;

import com.muhammadfarazrashid.i2106595.dataclasses.FirebaseManager;

import java.util.List;

public class AllMessagesChat {

    private Mentor mentor;
    private String id;
    private String userName;
    private int unreadMessages;
    private String otherUserImage;

    private String chatId;

    public void setMentor(Mentor mentor){
        this.mentor = mentor;
    }
    public Mentor getMentor(){
        return mentor;
    }


    public AllMessagesChat(String id, int unreadMessages) {
        this.id = id;
        this.unreadMessages = unreadMessages;

        // Fetch mentor details asynchronously
    }

    public AllMessagesChat(String id, String chatId) {
        this.id = id;
        this.chatId = chatId;
    }

    public AllMessagesChat(Mentor mentor,String id, String chatId, String userName, String otherUserImage) {

        this.mentor = mentor;
        this.chatId = chatId;
        this.id = id;
        this.userName = userName;
        this.otherUserImage = otherUserImage;
    }

    public void setOtherUserImage(String otherUserImage) {
        this.otherUserImage = otherUserImage;
    }

    public String getId() {
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public interface UnreadMessagesCallback {
        void onUnreadMessagesCount(int unreadCount);
    }

    public void getUnreadMessages(UnreadMessagesCallback callback) {
        FirebaseManager firebaseManager = new FirebaseManager();
        Log.d("AllMessagesChat", "chatId: " + chatId);
        firebaseManager.getUnreadChatMessagesCount("mentor_chats", chatId, unreadCount -> {
            // Handle unreadCount here
            callback.onUnreadMessagesCount(unreadCount);
            return null;
        });
    }

    public void setUnreadMessages(int unreadMessages){
        this.unreadMessages = unreadMessages;
    }

    public String getOtherUserImage() {
        return otherUserImage;
    }
}
