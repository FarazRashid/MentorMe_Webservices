package com.muhammadfarazrashid.i2106595;

import android.graphics.drawable.Drawable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    private String message="";
    private String time;
    private boolean isUser; // true if the message is sent by the user, false otherwise

    private String imageUrl;

    private String messageImageUrl="";

    private String videoImageUrl="";

    private String voiceMemoUrl="";

    private String documentUrl="";

    private String id;

    public ChatMessage(String message, boolean isUser, String imageUrl){
        this.message = message;
        this.isUser = isUser;
        this.imageUrl = imageUrl;
    }

    public ChatMessage(String message,String time, boolean isUser, String imageUrl){
        this.message = message;
        this.time = time;
        this.isUser = isUser;
        this.imageUrl = imageUrl;
    }

    public ChatMessage(String id,String message, String time, boolean isUser, String imageUrl){
        this.message = message;
        this.time = time;
        this.isUser = isUser;
        this.imageUrl = imageUrl;
        this.id = id;
    }

    public ChatMessage(String id,String message, String time, boolean isUser, String imageUrl,String messageImageUrl){
        this.message = message;
        this.time = time;
        this.isUser = isUser;
        this.imageUrl = imageUrl;
        this.id = id;
        this.messageImageUrl = messageImageUrl;
    }

    public ChatMessage(String id,String message, String time, boolean isUser, String imageUrl,String messageImageUrl,String videoImageUrl){
        this.message = message;
        this.time = time;
        this.isUser = isUser;
        this.imageUrl = imageUrl;
        this.id = id;
        this.messageImageUrl = messageImageUrl;
        this.videoImageUrl = videoImageUrl;
    }

    public ChatMessage(String id,String message, String time, boolean isUser, String imageUrl,String messageImageUrl,String videoImageUrl, String voiceMemoUrl){
        this.message = message;
        this.time = time;
        this.isUser = isUser;
        this.imageUrl = imageUrl;
        this.id = id;
        this.messageImageUrl = messageImageUrl;
        this.videoImageUrl = videoImageUrl;
        this.voiceMemoUrl = voiceMemoUrl;
    }

    public ChatMessage(String id,String message, String time, boolean isUser, String imageUrl,String messageImageUrl,String videoImageUrl, String voiceMemoUrl, String documentUrl){
        this.message = message;
        this.time = time;
        this.isUser = isUser;
        this.imageUrl = imageUrl;
        this.id = id;
        this.messageImageUrl = messageImageUrl;
        this.videoImageUrl = videoImageUrl;
        this.voiceMemoUrl = voiceMemoUrl;
        this.documentUrl = documentUrl;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getMessageImageUrl() {
        return messageImageUrl;
    }

    public void setMessageImageUrl(String messageImageUrl) {
        this.messageImageUrl = messageImageUrl;
    }

    public String getVideoImageUrl() {
        return videoImageUrl;
    }

    public void setVideoImageUrl(String videoImageUrl) {
        this.videoImageUrl = videoImageUrl;
    }

    public String getVoiceMemoUrl() {
        return voiceMemoUrl;
    }

    public void setVoiceMemoUrl(String voiceMemoUrl) {
        this.voiceMemoUrl = voiceMemoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public boolean isUser() {
        return isUser;
    }

    public String getOtherPersonImage() {
        return imageUrl;
    }

    public String getTimeStampFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM 'at' hh:mm a", Locale.getDefault());

        try {
            // Parse time to Date object
            Date messageDate = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(time);

            // Check if message time is before midnight
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(messageDate);
            int messageHour = calendar.get(Calendar.HOUR_OF_DAY);

            // If message time is before midnight, show only time
            if (messageHour < 24) {
                sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                return sdf.format(messageDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.time = sdf.format(new Date());
        return sdf.format(new Date());
    }
}
