package com.provesoft.resource.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class ProfilePicture {

    public ProfilePicture(String companyName, Long userId, byte[] image) {
        this.key = new ProfilePictureKey(companyName, userId);
        this.image = image;
    }

    public ProfilePicture() {
        // Default Constructor
    }

    @EmbeddedId
    private ProfilePictureKey key;

    @Lob
    private byte[] image;

    public ProfilePictureKey getKey() {
        return key;
    }

    public void setKey(ProfilePictureKey key) {
        this.key = key;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
