package com.example.vihaan.whatsappclone.ui.groupscreen;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GroupChatMessage {

    @SerializedName("date")
    @Expose
    private String stringDate;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("time")
    @Expose
    private String stringTime;
    @SerializedName("type")
    @Expose
    private String messageType;
    @SerializedName("local-uri")
    @Expose
    private String localUri;
    @SerializedName("storage-uri")
    @Expose
    private String storageUri;

    public GroupChatMessage() {
    }


    public String getStringDate() {
        return stringDate;
    }

    public void setStringDate(String stringDate) {
        this.stringDate = stringDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStorageUri() {
        return storageUri;
    }

    public void setStorageUri(String storageUri) {
        this.storageUri = storageUri;
    }

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getStringTime() {
        return stringTime;
    }

    public void setStringTime(String stringTime) {
        this.stringTime = stringTime;
    }
}
