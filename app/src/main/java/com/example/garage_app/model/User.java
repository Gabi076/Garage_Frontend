package com.example.garage_app.model;

public class User {
    private Long id;
    private String mail;
    private String username;

    public User(String mail, String username) {
        this.mail = mail;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getMail() {
        return mail;
    }

    public String getUsername() {
        return username;
    }
}

