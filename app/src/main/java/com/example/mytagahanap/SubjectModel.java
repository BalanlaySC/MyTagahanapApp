package com.example.mytagahanap;

public class SubjectModel {
    private String mClassID,
            mDescription, mSubjectCode,
            mRoom, mTime, mDay;

    public SubjectModel(String mClassID,
                        String mDescription, String mSubjectCode,
                        String mRoom, String mTime, String mDay) {

        this.mClassID = mClassID;
        this.mDescription = mDescription;
        this.mSubjectCode = mSubjectCode;
        this.mRoom = mRoom;
        this.mTime = mTime;
        this.mDay = mDay;
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
