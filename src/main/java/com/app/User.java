package com.app;

public class User {
    public String id;
    public String username;
    public String avatar; // cached at login
    public int coins = 0;

    public User(String id, String username, String avatar) {
        this.id = id;
        this.username = username;
        this.avatar = avatar;
    }
}