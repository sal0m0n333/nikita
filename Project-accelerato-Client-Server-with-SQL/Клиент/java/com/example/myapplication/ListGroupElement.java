package com.example.myapplication;

public class ListGroupElement {

    private String groupName;
    private int groupID;
    private String subInfo;
    private String typeChat;
    private int resId;

    public ListGroupElement(String groupName,int groupID, String subInfo, String typeChat, int resId) {
        this.groupName = groupName;
        this.groupID = groupID;
        this.subInfo = subInfo;
        this.typeChat = typeChat;
        this.resId = resId;
    }

    public int getGroupID() {
        return groupID;
    }

    public int getResId() {
        return resId;
    }

    public String getTypeChat() {
        return typeChat;
    }

    public void setTypeChat(String typeChat) {
        this.typeChat = typeChat;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getSubInfo() {
        return subInfo;
    }

    public void setSubInfo(String subInfo) {
        this.subInfo = subInfo;
    }
}
