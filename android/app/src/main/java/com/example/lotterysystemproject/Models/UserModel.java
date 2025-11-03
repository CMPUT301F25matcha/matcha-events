package com.example.lotterysystemproject.Models;

public class UserModel {

    private String userName;
    private String userEmail;
    private String userPhone;
    private boolean detailsProvided;

    public UserModel(String userName, String userEmail, String userPhone) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.detailsProvided = false;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public boolean isDetailsProvided() {
        return detailsProvided;
    }

    public void setDetailsProvided(boolean detailsProvided) {
        this.detailsProvided = detailsProvided;
    }
}


