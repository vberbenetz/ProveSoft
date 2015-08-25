package com.provesoft.resource.utils;

public class ProfilePicturePkg {

    public ProfilePicturePkg(Long userId, String picData) {
        this.userId = userId;
        this.picData = picData;
    }

    private String picData;
    private Long userId;

    public String getPicData() {
        return picData;
    }

    public void setPicData(String picData) {
        this.picData = picData;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
