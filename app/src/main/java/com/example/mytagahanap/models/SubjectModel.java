package com.example.mytagahanap.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class SubjectModel implements Parcelable {
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

    protected SubjectModel(Parcel in) {
        mClassID = in.readString();
        mSubjectCode = in.readString();
        mDescription = in.readString();
        mTime = in.readString();
        mDay = in.readString();
        mRoom = in.readString();
    }

    public static final Creator<SubjectModel> CREATOR = new Creator<SubjectModel>() {
        @Override
        public SubjectModel createFromParcel(Parcel in) {
            return new SubjectModel(in);
        }

        @Override
        public SubjectModel[] newArray(int size) {
            return new SubjectModel[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mClassID);
        parcel.writeString(mSubjectCode);
        parcel.writeString(mDescription);
        parcel.writeString(mTime);
        parcel.writeString(mDay);
        parcel.writeString(mRoom);
    }
}
