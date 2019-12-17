package com.example.myapplication;

public class ListLogElement {

    private String date;
    private String login;
    private String mylogin;
    private String message;

    public ListLogElement(String date, String login, String message, String mylogin) {
        this.date = date;
        this.login = login;
        this.message = message;
        this.mylogin = mylogin;
    }

    public String getMylogin() {
        return mylogin;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
