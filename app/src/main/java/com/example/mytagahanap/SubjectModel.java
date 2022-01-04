package com.example.mytagahanap;

import androidx.annotation.NonNull;

public class SubjectModel {
    private String mClassID, mSubjectCode,
            mDescription,
            mTime, mDay, mRoom;

    public SubjectModel(String mClassID, String mSubjectCode,
                        String mDescription,
                        String mTime, String mDay, String mRoom) {

        this.mClassID = mClassID;
        this.mSubjectCode = mSubjectCode;
        this.mDescription = mDescription;
        this.mTime = mTime;
        this.mDay = mDay;
        this.mRoom = mRoom;
    }

    @NonNull
    @Override
    public String toString() {
        return "SubjectModel{" +
                "mClassID='" + mClassID + '\'' +
                ", mSubjectCode='" + mSubjectCode + '\'' +
                ", mDescription='" + mDescription + '\'' +
                ", mTime='" + mTime + '\'' +
                ", mDay='" + mDay + '\'' +
                ", mRoom='" + mRoom + '\'' +
                '}';
    }

    public String getmClassID() {
        return mClassID;
    }

    public String getmDescription() {
        return mDescription;
    }

    public String getmSubjectCode() {
        return mSubjectCode;
    }

    public String getmRoom() {
        return mRoom;
    }

    public String getmTime() {
        return mTime;
    }

    public String getmDay() {
        return mDay;
    }
}
