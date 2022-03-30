package com.example.mytagahanap.models;

public class ProponentModel {
    private String firstName, middleInitial, lastName, address, courseYrSect;
    private int age, imgId;
    private boolean isExpanded;

    public ProponentModel(String firstName, String middleInitial, String lastName,
                          String address, String courseYrSect, int age, int imgId) {
        this.firstName = firstName;
        this.middleInitial = middleInitial;
        this.lastName = lastName;
        this.address = address;
        this.courseYrSect = courseYrSect;
        this.age = age;
        this.imgId = imgId;
        this.isExpanded = false;
    }

    public String getFullNameFL() {
        return firstName + " " + middleInitial + " " + lastName;
    }

    public String getFullNameLF() {
        return lastName + ", " + firstName + " " + middleInitial;
    }

    public int getAge() {
        return age;
    }

    public int getImgId() {
        return imgId;
    }

    public String getAddress() {
        return address;
    }

    public String getCourse() {
        return courseYrSect;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    @Override
    public String toString() {
        return "ProponentModel{" +
                "name='" + getFullNameFL() + '\'' +
                ", age=" + age +
                ", address='" + address + '\'' +
                ", course='" + courseYrSect + '\'' +
                '}';
    }
}
