package com.example.mytagahanap;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class EnlargedImageModel implements Parcelable {
    private String imgUrl;
    private String imgThumbUrl;

    public EnlargedImageModel(String imgUrl, String imgThumbUrl) {
        this.imgUrl = imgUrl;
        this.imgThumbUrl = imgThumbUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return "imgModel{" +
                "imgUrl='" + imgUrl + '\'' +
                ", imgThumbUrl='" + imgThumbUrl + '\'' +
                '}';
    }

    protected EnlargedImageModel(Parcel in) {
        imgUrl = in.readString();
        imgThumbUrl = in.readString();
    }

    public static final Creator<EnlargedImageModel> CREATOR = new Creator<EnlargedImageModel>() {
        @Override
        public EnlargedImageModel createFromParcel(Parcel in) {
            return new EnlargedImageModel(in);
        }

        @Override
        public EnlargedImageModel[] newArray(int size) {
            return new EnlargedImageModel[size];
        }
    };

    public String getImgUrl() {
        return imgUrl;
    }

    public String getImgThumbUrl() {
        return imgThumbUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imgUrl);
        parcel.writeString(imgThumbUrl);
    }
}
