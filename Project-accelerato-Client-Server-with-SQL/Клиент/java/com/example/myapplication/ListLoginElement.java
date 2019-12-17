package com.example.myapplication;

public class ListLoginElement {

    private String login;
    private int resId;

    public ListLoginElement(String login, int resId) {
        this.login = login;
        this.resId = resId;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
