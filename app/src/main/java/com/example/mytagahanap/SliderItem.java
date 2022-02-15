package com.example.mytagahanap;

import android.os.Parcel;
import android.os.Parcelable;

public class SliderItem implements Parcelable {
    private String imgUrl;

    public SliderItem(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    protected SliderItem(Parcel in) {
        imgUrl = in.readString();
    }

    public static final Creator<SliderItem> CREATOR = new Creator<SliderItem>() {
        @Override
        public SliderItem createFromParcel(Parcel in) {
            return new SliderItem(in);
        }

        @Override
        public SliderItem[] newArray(int size) {
            return new SliderItem[size];
        }
    };

    public String getImgUrl() {
        return imgUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imgUrl);
    }
}
