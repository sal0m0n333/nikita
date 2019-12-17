package com.example.myapplication;

import java.util.ArrayList;

public class HistoryChat {

    private String nameChat;
    private int groupID;
    private String typeChat;
    private ArrayList<String> history;

    public HistoryChat(String nameChat, String typeChat) {


        if (typeChat.equals("public")){
            String groupName = "";
            String groupID = "";

            for (int i = 0; i != nameChat.length(); i++) {
                if (nameChat.charAt(i) == '-') {
                    groupName = nameChat.substring(0, i);
                    groupID = nameChat.substring(i + 1);
                    break;
                }
            }
            this.nameChat = groupName;
            this.groupID = Integer.parseInt(groupID);
            this.typeChat = typeChat;
            history = new ArrayList<String>();
        }else {
            this.nameChat = nameChat;
            this.groupID = -1;
            this.typeChat = typeChat;
            history = new ArrayList<String>();
        }

    }

    public int getGroupID() {
        return groupID;
    }

    public String getNameChat() {
        return nameChat;
    }

    public ArrayList<String> getHistory() {
        return history;
    }

    public String getTypeChat() {
        return typeChat;
    }

    public void addHistory(String message) {
        this.history.add(message);
    }
}
