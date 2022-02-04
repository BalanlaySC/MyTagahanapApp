package com.example.mytagahanap;

public class UEPAddInfoModel {
    private String titleInfo, expandedText;
    private int imgId;
    private boolean isExpanded;

    public UEPAddInfoModel(String titleInfo, String expandedText, int imgId) {
        this.titleInfo = titleInfo;
        this.expandedText = expandedText;
        this.imgId = imgId;
        this.isExpanded = false;
    }

    public String getTitleInfo() {
        return titleInfo;
    }

    public String getExpandedText() {
        return expandedText;
    }

    public int getImgId() {
        return imgId;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public boolean isExpanded() {
        return isExpanded;
    }
}
