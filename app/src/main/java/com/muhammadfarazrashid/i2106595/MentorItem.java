package com.muhammadfarazrashid.i2106595;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class MentorItem {

    private Mentor mentor;

    private String id;
    private String profileImageUrl;
    private boolean isMentorAvailable;

    public MentorItem(String id, boolean isMentorAvailable) {
        this.id=id;
        this.isMentorAvailable = isMentorAvailable;
    }

    public MentorItem(Mentor mentor, String id, String profileImageUrl, boolean isMentorAvailable) {
        this.mentor = mentor;
        this.id = id;
        this.profileImageUrl = profileImageUrl;
        this.isMentorAvailable = isMentorAvailable;
    }

    public void setMentor(Mentor mentor){
        this.mentor = mentor;
    }

    public Mentor getMentor(){
        return mentor;
    }


    public String  getProfileImageUrl() {
        return profileImageUrl;
    }


    public void setProfileImageUrl(String  profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }


    public boolean isMentorAvailable() {
        return isMentorAvailable;
    }
}
