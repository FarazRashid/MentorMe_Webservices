package com.muhammadfarazrashid.i2106595;

import android.graphics.drawable.Drawable;

public class Session {


    private String mentorId;

    private String bookingDate;
    private String bookingTime;

    private Mentor mentor;



    public Session(String bookingDate, String bookingTime, String mentorId) {
        this.mentorId = mentorId;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
    }

    public void setMentor(Mentor mentor) {
        this.mentor = mentor;
    }

    public Mentor getMentor() {
        return mentor;
    }


    public String getMentorId() {
        return mentorId;
    }


    public String getDate() {
        return bookingDate;
    }

    public String getTime() {
        return bookingTime;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

}
