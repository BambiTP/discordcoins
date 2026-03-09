package com.app;

public class User {

    public String id;
    public String username;
    public String avatar;
    public int coins;

    public User(String id, String username, String avatar) {
        this.id = id;
        this.username = username;
        this.avatar = avatar;
        this.coins = 0;
    }

}