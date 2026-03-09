package com.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Collection;

@RestController
@RequestMapping("/api")
public class CoinController {

    @Autowired
    UserStore store;

    // Add coin using session
    @PostMapping("/addCoin")
    public User addCoin(HttpSession session) {
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Not logged in");
        }

        User user = store.users.get(userId);
        if (user != null) {
            user.coins++;
        }

        return user;
    }

    @GetMapping("/users")
    public Collection<User> users() {
        return store.users.values();
    }

}