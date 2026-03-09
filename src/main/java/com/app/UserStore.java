package com.app;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStore {

    public Map<String, User> users = new ConcurrentHashMap<>();

}